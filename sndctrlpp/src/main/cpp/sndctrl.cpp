#include "pch.h"
#include "sndctrl.h"
#include "helper.h"
#include "volumeosd.h"

DeviceChangedCb* deviceChanged;
DeviceRemovedCb* deviceRemoved;
SessionChangedCb* sessionChanged;
SessionRemovedCb* sessionRemoved;
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
  StringCallback debugCb, StringCallback infoCb) {
  deviceChanged = deviceChangedCb;
  deviceRemoved = deviceRemovedCb;
  sessionChanged = sessionChangedCb;
  sessionRemoved = sessionRemovedCb;
  debug = debugCb;
  info = infoCb;

  auto pDevices = EnumAudioEndpoints(*pEnumerator);
  auto count = GetCount(*pDevices);
	for (UINT idx = 0; idx < count; idx++) {
    auto pDevice = DeviceFromCollection(*pDevices, idx);
    DeviceAdded(pDevice);
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
  deviceChanged(nameAndId.name.get(), nameAndId.id.get(), volume, muted);

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
  wstring wname(name);
  if (name2control.find(wname) != name2control.end()) {
    for (CComPtr<IAudioSessionControl>& session : name2control[wname]) {
      CComQIPtr<ISimpleAudioVolume> cc = session.p;
      if (cc) {
        cc->SetMasterVolume(volume / 100.0f, nullptr);
        ShowVolume(volume, false, osd);
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

extern "C" SNDCTRL_API void setDeviceMute(const LPWSTR id, bool muted, bool osd) {
  if (devices.find(id) != devices.end()) {
    auto control = GetVolumeControl(*devices[id]);
    control->SetMute(muted, nullptr);
    ShowVolume(control, osd);
  }
}

extern "C" SNDCTRL_API void setProcessMute(const LPWSTR name, bool muted, bool osd) {
  cout << "Set process mute " << name << muted << endl;
}

extern "C" SNDCTRL_API void setActiveDevice(const LPWSTR id, bool osd) {
  cout << "Set active device " << id << endl;
}


DeviceListener::DeviceListener(CComPtr<IMMDeviceEnumerator> e) : pEnumerator(e) {
  e->RegisterEndpointNotificationCallback(this);
}

void DeviceListener::Stop() {
  pEnumerator->UnregisterEndpointNotificationCallback(this);
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
  deviceChanged(L"", pId, pNotify->fMasterVolume * 100, pNotify->bMuted);
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
