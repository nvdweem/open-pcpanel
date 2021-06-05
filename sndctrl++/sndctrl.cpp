#include "pch.h"
#include "sndctrl.h"
#include "helper.h"
#include "volumeosd.h"

DeviceAddedCb* deviceAdded;
DeviceRemovedCb* deviceRemoved;
SessionAddedCb* sessionAdded;
SessionRemovedCb* sessionRemoved;

CComPtr<IMMDeviceEnumerator> pEnumerator;
unordered_map<wstring, CComPtr<IMMDevice>> devices;
unordered_map<wstring, CComPtr<IMMDevice>> deviceSessionManagers;
unordered_map<DWORD, CComPtr<IAudioSessionControl>> pid2control;
unordered_map<wstring, list<CComPtr<IAudioSessionControl>>> name2control;

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
	cout << "Device enumerator created" << endl;
}

void deinit() {
  devices.clear();
  name2control.clear();
  pid2control.clear();
  deviceSessionManagers.clear();
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
    sessionAdded(processId, &processName[0]);

    CComPtr<AudioSessionListener> pListener(new AudioSessionListener(session, processId, processName));
    session->RegisterAudioSessionNotification(pListener);
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

  sessionRemoved(pid);
}

SNDCTRL_API void GetForegroundProcess(StringCallback cb) {
  DWORD procId;
  GetWindowThreadProcessId(GetForegroundWindow(), &procId);
  cb(&ProcessIdToName(procId)[0]);
}

extern "C" SNDCTRL_API void init(
  DeviceAddedCb deviceAddedCb, DeviceRemovedCb deviceRemovedCb,
  SessionAddedCb sessionAddedCb, SessionRemovedCb sessionRemovedCb){
  deviceAdded = deviceAddedCb;
  deviceRemoved = deviceRemovedCb;
  sessionAdded = sessionAddedCb;
  sessionRemoved = sessionRemovedCb;


  auto pDevices = EnumAudioEndpoints(*pEnumerator);
  auto count = GetCount(*pDevices);
	for (UINT idx = 0; idx < count; idx++) {
    auto pDevice = DeviceFromCollection(*pDevices, idx);
    auto nameAndId = DeviceNameId(*pDevice);
    wstring deviceId(nameAndId.id.get());
    devices.insert({ deviceId, pDevice });

    deviceAdded(nameAndId.name.get(), nameAndId.id.get());

    auto pSessionManager = Activate(*pDevice);

    // Register listener
    CComPtr<SessionListener> pSessionListener(new SessionListener(pSessionManager, wstring(nameAndId.id.get())));
    pSessionManager->RegisterSessionNotification(pSessionListener);
    deviceSessionManagers.insert({ deviceId, pDevice });

    // Get current sessions
    auto pSessionList = GetSessionEnumerator(*pSessionManager);
    auto sessionCount = GetCount(*pSessionList);
    for (int index = 0; index < sessionCount; index++) {
      auto session = GetSession(*pSessionList, index);
      SessionAdded(session);
    }
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

extern "C" SNDCTRL_API bool toggleDeviceMute(const LPWSTR id, bool osd) {
  BOOL isMute = false;
  if (devices.find(id) != devices.end()) {
    auto control = GetVolumeControl(*devices[id]);
    control->GetMute(&isMute);
    control->SetMute(!isMute, nullptr);

    ShowVolume(control, osd);
  }
  return false;
}

void setDeviceVolume(const LPWSTR id, int volume, bool osd) {
  if (devices.find(id) != devices.end()) {
    auto control = GetVolumeControl(*devices[id]);
    control->SetMasterVolumeLevelScalar(volume / 100.0f, nullptr);
    ShowVolume(volume, false, osd);
  }
}

void setProcessVolume(const LPWSTR name, int volume, bool osd) {
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

wstring GetProcessName(DWORD procId) {
  DWORD buffSize = MAX_PATH;
  WCHAR buffer[MAX_PATH] = { 0 };
  HANDLE hProc = OpenProcess(PROCESS_QUERY_INFORMATION, FALSE, procId);
  QueryFullProcessImageNameW(hProc, NULL, buffer, &buffSize);
  CloseHandle(hProc);
  return wstring(buffer);
}

void setFgProcessVolume(int volume, bool osd) {
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

HRESULT SessionListener::OnSessionCreated(IAudioSessionControl* pNewSession) {
  if (pNewSession) {
    CComPtr<IAudioSessionControl> ptr(pNewSession);
    SessionAdded(ptr);
  }
  return S_OK;
}

HRESULT __stdcall AudioSessionListener::OnStateChanged(AudioSessionState NewState) {
  if (NewState == AudioSessionStateExpired) {
    SessionRemoved(sessionControl, pid, processName);
    sessionControl->UnregisterAudioSessionNotification(this);
  }
  return S_OK;
}
