#include "pch.h"
#include "sndctrl.h"
#include "helper.h"
#include "volumeosd.h"
#include "policyconfig.h"

DeviceChangedCb* deviceChanged;
DeviceRemovedCb* deviceRemoved;
SessionChangedCb* sessionChanged;
SessionRemovedCb* sessionRemoved;
DefaultDeviceChangedCb* defaultDeviceChanged;
StringCallback* debug;
StringCallback* info;

CComPtr<IMMDeviceEnumerator> pEnumerator;
unordered_map<wstring, CComPtr<IMMDevice>> devices;
unordered_map<wstring, pair<CComPtr<DeviceVolumeListener>, CComPtr<SessionListener>>> deviceSessionManagers;
unordered_map<DWORD, CComPtr<IAudioSessionControl>> pid2control;
unordered_map<DWORD, CComPtr<AudioSessionListener>> pid2listener;
unordered_map<wstring, list<CComPtr<IAudioSessionControl>>> name2control;
CComPtr<DeviceListener> pDeviceListener;

void init() {
  if (CoInitialize(nullptr) != S_OK) {
    cerr << "Unable to CoInitialize" << endl;
  }
	const CLSID CLSID_MMDeviceEnumerator = __uuidof(MMDeviceEnumerator);
	const IID IID_IMMDeviceEnumerator = __uuidof(IMMDeviceEnumerator);

  IMMDeviceEnumerator* enumerator = NULL;
  if (FAILED(CoCreateInstance(CLSID_MMDeviceEnumerator, NULL, CLSCTX_ALL, IID_IMMDeviceEnumerator, (void**)&enumerator))) {
    cerr << "Unable to create device enumerator, more will fail later :(" << endl;
  }
  pEnumerator = enumerator;
  pDeviceListener = new DeviceListener(pEnumerator);

	cout << "Device enumerator created" << endl;
}

void deinit() {
  pDeviceListener->Stop();
  for (auto& it : pid2listener) {
    it.second->Stop();
  }

  for (auto& it : deviceSessionManagers) {
    it.second.first->Stop();
    it.second.second->Stop();
  }

  devices.clear();
  name2control.clear();
  pid2control.clear();
  deviceSessionManagers.clear();

  pEnumerator->UnregisterEndpointNotificationCallback(pDeviceListener);
  pDeviceListener.Release();
}

void SessionAdded(CComPtr<IAudioSessionControl> session) {
  auto session2 = GetSession2(*session);

  auto processId = GetProcessId(*session2);
  if (processId > 0) {
    auto processName = ProcessIdToName(processId);
    if (name2control.find(processName) == name2control.end()) {
      name2control.insert({ processName, list<CComPtr<IAudioSessionControl>>() });
    }
    name2control[processName].push_back(session);
    pid2control.insert({ processId, session });
    pid2listener.insert({ processId, new AudioSessionListener(session, processId, processName) });
  }
}

void SessionRemoved(CComPtr<IAudioSessionControl>& ctrl, DWORD pid, wstring& name) {
  pid2control.erase(pid);
  if (name2control.find(name) != name2control.end()) {
    auto& list = name2control[name];
    list.remove(ctrl);
    if (list.empty()) {
      name2control.erase(name);
    }
  }

  auto listener = pid2listener.find(pid);
  if (listener != pid2listener.end()) {
    listener->second->Stop();
    pid2listener.erase(listener);
  }

  sessionRemoved(pid);
}

extern "C" SNDCTRL_API void init(
  DeviceChangedCb deviceChangedCb, DeviceRemovedCb deviceRemovedCb,
  SessionChangedCb sessionChangedCb, SessionRemovedCb sessionRemovedCb,
  DefaultDeviceChangedCb defaultDeviceChangedCb,
  StringCallback debugCb, StringCallback infoCb) {
  deviceChanged = deviceChangedCb;
  deviceRemoved = deviceRemovedCb;
  sessionChanged = sessionChangedCb;
  sessionRemoved = sessionRemovedCb;
  defaultDeviceChanged = defaultDeviceChangedCb;
  debug = debugCb;
  info = infoCb;

  auto pDevices = EnumAudioEndpoints(*pEnumerator);
  auto count = GetCount(*pDevices);
	for (UINT idx = 0; idx < count; idx++) {
    auto pDevice = DeviceFromCollection(*pDevices, idx);
    DeviceAdded(pDevice);
	}

  for (int dataflow = eRender; dataflow < eAll; dataflow++) {
    EDataFlow df = (EDataFlow) dataflow;
    for (int role = 0; role < ERole_enum_count; role++) {
      CComPtr<IMMDevice> pDevice = nullptr;
      ERole rl = (ERole) role;
      pEnumerator->GetDefaultAudioEndpoint(df, rl, &pDevice);

      if (pDevice) {
        LPWSTR id = nullptr;
        pDevice->GetId(&id);

        co_ptr<WCHAR> pId(id);
        defaultDeviceChanged(id, dataflow, role);
      }
    }
  }
}

void DeviceAdded(CComPtr<IMMDevice> pDevice) {
  auto nameAndId = DeviceNameId(*pDevice);
  wstring deviceId(nameAndId.id.get());
  devices.insert({ deviceId, pDevice });

  float volume = 0;
  BOOL muted = 0;
  auto volumeCtrl = GetVolumeControl(*pDevice);
  volumeCtrl->GetMasterVolumeLevelScalar(&volume);
  volumeCtrl->GetMute(&muted);

  deviceChanged(nameAndId.name.get(), nameAndId.id.get(), volume, muted, getDataFlow(*pDevice));

  auto pSessionManager = Activate(*pDevice);

  // Register Session listener
  deviceSessionManagers.insert({ deviceId, {new DeviceVolumeListener(pDevice), new SessionListener(pSessionManager, wstring(nameAndId.id.get()))} });

  // Get current sessions
  auto pSessionList = GetSessionEnumerator(*pSessionManager);
  auto sessionCount = GetCount(*pSessionList);
  for (int index = 0; index < sessionCount; index++) {
    auto session = GetSession(*pSessionList, index);
    SessionAdded(session);
  }
}




void ShowVolume(int volume, bool muted, bool osd) {
  if (osd) {
    ShowVolumeOsd(volume, muted);
  }
}

void ShowVolume(CComPtr<IAudioEndpointVolume>& volume, bool osd) {
  if (osd) {
    BOOL isMute = false;
    float volumeLevel = 0;
    volume->GetMute(&isMute);
    volume->GetMasterVolumeLevelScalar(&volumeLevel);
    ShowVolume((int) (volumeLevel * 100), isMute ? true : false, true);
  }
}

extern "C" SNDCTRL_API void setDeviceVolume(const LPWSTR id, int volume, bool osd) {
  if (devices.find(id) != devices.end()) {
    auto control = GetVolumeControl(*devices[id]);
    control->SetMasterVolumeLevelScalar(volume / 100.0f, nullptr);
    ShowVolume(volume, false, osd);
  }
}

extern "C" SNDCTRL_API void setProcessVolume(const LPWSTR name, int volume, bool osd) {
  for (auto& p : name2control) {
    if (p.first.find(name) != -1) {
      for (CComPtr<IAudioSessionControl>& session : p.second) {
        CComQIPtr<ISimpleAudioVolume> cc = session.p;
        if (cc) {
          cc->SetMasterVolume(volume / 100.0f, nullptr);
          ShowVolume(volume, false, osd);
        }
      }
    }
  }
}

extern "C" SNDCTRL_API void setFgProcessVolume(int volume, bool osd) {
  DWORD procId;
  GetWindowThreadProcessId(GetForegroundWindow(), &procId);

  auto fromId = pid2control.find(procId);
  if (fromId != pid2control.end()) {
    CComQIPtr<ISimpleAudioVolume> cc(fromId->second.p);
    if (cc) {
      cc->SetMasterVolume(volume / 100.0f, nullptr);
      ShowVolume(volume, false, osd);
      return;
    }
  }

  // Not found by pid, find by name
  auto name = GetProcessName(procId);
  auto found = name2control.find(name);
  if (found != name2control.end()) {
    auto ps = *found;
    for (auto& ctrl : ps.second) {
      CComQIPtr<ISimpleAudioVolume> cc(ctrl.p);
      if (cc) {
        cc->SetMasterVolume(volume / 100.0f, nullptr);
        ShowVolume(volume, false, osd);
      }
    }
  }
}

extern "C" SNDCTRL_API void setDeviceMute(const LPWSTR id, int muted, int osd) {
  if (devices.find(id) != devices.end()) {
    auto control = GetVolumeControl(*devices[id]);
    BOOL NewMuted = muted != 0 ? 1 : 0;
    control->SetMute(NewMuted, nullptr);
    ShowVolume(control, osd != 0);
  }
}

extern "C" SNDCTRL_API void setProcessMute(const LPWSTR name, int muted, int osd) {
  BOOL NewMute = muted != 0 ? 1 : 0;
  float volume = 0;
  for (auto& p : name2control) {
    if (p.first.find(name) != -1) {
      for (auto& sess : p.second) {
        CComQIPtr<ISimpleAudioVolume> cc(sess.p);
        cc->SetMute(NewMute, nullptr);
        cc->GetMasterVolume(&volume);
      }
    }
  }
  ShowVolume(volume * 100, muted != 0, osd != 0);
}

extern "C" SNDCTRL_API void setActiveDevice(const LPWSTR id, int role, int osd) {
  CComPtr<IPolicyConfigVista> pPolicyConfig;
  ERole reserved = (ERole) role;

  HRESULT hr = CoCreateInstance(__uuidof(CPolicyConfigVistaClient), NULL, CLSCTX_ALL, __uuidof(IPolicyConfigVista), (LPVOID*)&pPolicyConfig);
  if (SUCCEEDED(hr)) {
    pPolicyConfig->SetDefaultEndpoint(id, reserved);
  }
}


DeviceListener::DeviceListener(CComPtr<IMMDeviceEnumerator> e) : pEnumerator(e) {
  e->RegisterEndpointNotificationCallback(this);
}

void DeviceListener::Stop() {
  pEnumerator->UnregisterEndpointNotificationCallback(this);
}

HRESULT STDMETHODCALLTYPE DeviceListener::OnDefaultDeviceChanged(EDataFlow flow, ERole role, LPCWSTR pwstrDefaultDeviceId) {
  wstring tempStr(pwstrDefaultDeviceId);
  defaultDeviceChanged(&tempStr[0], flow, role);
  return S_OK;
}



HRESULT STDMETHODCALLTYPE DeviceListener::OnDeviceAdded(LPCWSTR pwstrDeviceId) {
  CComPtr<IMMDevice> pDevice;
  pEnumerator->GetDevice(pwstrDeviceId, &pDevice);
  DeviceAdded(pDevice);
  return S_OK;
}

HRESULT STDMETHODCALLTYPE DeviceListener::OnDeviceRemoved(LPCWSTR pwstrDeviceId) {
  auto entry = deviceSessionManagers.find(pwstrDeviceId);
  if (entry != deviceSessionManagers.end()) {
    entry->second.first->Stop();
    entry->second.second->Stop();
    deviceSessionManagers.erase(entry);
  }
  return S_OK;
}

DeviceVolumeListener::DeviceVolumeListener(CComPtr<IMMDevice> pDevice)
  : pDevice(pDevice), pVolume(GetVolumeControl(*pDevice)) {
  pVolume->RegisterControlChangeNotify(this);
}

void DeviceVolumeListener::Stop() {
  pVolume->UnregisterControlChangeNotify(this);
}

HRESULT STDMETHODCALLTYPE DeviceVolumeListener::OnNotify(PAUDIO_VOLUME_NOTIFICATION_DATA pNotify) {
  LPWSTR pId = nullptr;
  pDevice->GetId(&pId);
  co_ptr<WCHAR> ppId(pId);
  deviceChanged(L"", pId, pNotify->fMasterVolume * 100, pNotify->bMuted, getDataFlow(*pDevice));
  return S_OK;
}


HRESULT SessionListener::OnSessionCreated(IAudioSessionControl* pNewSession) {
  if (pNewSession) {
    CComPtr<IAudioSessionControl> ptr(pNewSession);
    SessionAdded(ptr);
  }
  return S_OK;
}

AudioSessionListener::AudioSessionListener(CComPtr<IAudioSessionControl> sessionControl, DWORD pid, wstring processName)
  : sessionControl(sessionControl), pid(pid), processName(processName), icon(), volume(0), muted(0) {
  LPWSTR icon = NULL;
  float level = 0;
  sessionControl->GetIconPath(&icon);
  this->icon = icon;
  co_ptr<WCHAR> pIcon(icon);

  CComQIPtr<ISimpleAudioVolume> cc = sessionControl.p;
  cc->GetMasterVolume(&level);
  cc->GetMute(&muted);
  volume = level * 100;
  SendUpdate();

  sessionControl->RegisterAudioSessionNotification(this);
}

void AudioSessionListener::Stop() {
  sessionControl->UnregisterAudioSessionNotification(this);
}

HRESULT STDMETHODCALLTYPE AudioSessionListener::OnIconPathChanged(LPCWSTR NewIconPath, LPCGUID EventContext) {
  icon = NewIconPath;
  SendUpdate();
  return S_OK;
}

HRESULT STDMETHODCALLTYPE AudioSessionListener::OnSimpleVolumeChanged(float NewVolume, BOOL NewMute, LPCGUID EventContext) {
  volume = NewVolume * 100;
  muted = NewMute;
  SendUpdate();
  return S_OK;
}

void AudioSessionListener::SendUpdate() {
  sessionChanged(pid, &processName[0], &icon[0], volume, muted);
}

HRESULT __stdcall AudioSessionListener::OnStateChanged(AudioSessionState NewState) {
  if (NewState == AudioSessionStateExpired) {
    SessionRemoved(sessionControl, pid, processName);
    sessionControl->UnregisterAudioSessionNotification(this);
  }
  return S_OK;
}
