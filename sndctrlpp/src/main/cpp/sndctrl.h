#pragma once

#ifdef SNDCTRL_EXPORTS
#define SNDCTRL_API __declspec(dllexport)
#else
#define SNDCTRL_API __declspec(dllimport)
#endif

void init();
void deinit();

void ProcessIdToName(DWORD processId, LPWSTR buffer, DWORD bufferSize);

using StringCallback = void(const LPWSTR);
using DeviceAddedCb = void(const LPWSTR, const LPWSTR);
using DeviceRemovedCb = void(const LPWSTR);
using SessionAddedCb = void(const DWORD, const LPWSTR);
using SessionRemovedCb = void(const DWORD);

extern "C" SNDCTRL_API void init(
	DeviceAddedCb deviceAdded, DeviceRemovedCb deviceRemoved, 
	SessionAddedCb sessionAdded, SessionRemovedCb sessionRemoved);

extern "C" SNDCTRL_API bool toggleDeviceMute(const LPWSTR id, bool osd);
extern "C" SNDCTRL_API void setDeviceVolume(const LPWSTR id, int volume, bool osd);
extern "C" SNDCTRL_API void setProcessVolume(const LPWSTR name, int volume, bool osd);
extern "C" SNDCTRL_API void setFgProcessVolume(int volume, bool osd);


extern "C" SNDCTRL_API void getForegroundProcess(StringCallback cb);



/// <summary>
///   Listener classes
/// </summary>

class Listener {
public:
  LONG m_cRefAll;

public:
  Listener() : m_cRefAll(0) {}
  virtual ~Listener() {}

  // IUnknown
  HRESULT STDMETHODCALLTYPE QueryInterface(REFIID riid, void** ppv) {
    if (IID_IUnknown == riid) {
      AddRef();
      *ppv = (IUnknown*)this;
    }
    else if (__uuidof(IAudioSessionNotification) == riid) {
      AddRef();
      *ppv = (IAudioSessionNotification*)this;
    }
    else {
      *ppv = NULL;
      return E_NOINTERFACE;
    }
    return S_OK;
  }

  ULONG STDMETHODCALLTYPE AddRef() {
    return InterlockedIncrement(&m_cRefAll);
  }

  ULONG STDMETHODCALLTYPE Release() {
    ULONG ulRef = InterlockedDecrement(&m_cRefAll);
    if (0 == ulRef) {
      delete this;
    }
    return ulRef;
  }
};

class SessionListener : public Listener, public IAudioSessionNotification {
private:
  CComPtr<IAudioSessionManager2> sessionManager;
  wstring deviceId;

public:
  SessionListener(CComPtr<IAudioSessionManager2> sessionManager, wstring deviceId) : sessionManager(sessionManager), deviceId(deviceId) {}

  HRESULT STDMETHODCALLTYPE OnSessionCreated(IAudioSessionControl* pNewSession);

  HRESULT STDMETHODCALLTYPE QueryInterface(REFIID riid, void** ppv) override { return Listener::QueryInterface(riid, ppv); }
  ULONG STDMETHODCALLTYPE AddRef() override { return Listener::AddRef(); }
  ULONG STDMETHODCALLTYPE Release() override { return Listener::Release(); }
};

class AudioSessionListener : public Listener, public IAudioSessionEvents {
private:
  CComPtr<IAudioSessionControl> sessionControl;
  DWORD pid;
  wstring processName;

public:
  AudioSessionListener(CComPtr<IAudioSessionControl> sessionControl, DWORD pid, wstring processName) : sessionControl(sessionControl), pid(pid), processName(processName) {}

  virtual HRESULT STDMETHODCALLTYPE OnDisplayNameChanged(LPCWSTR NewDisplayName, LPCGUID EventContext) { return S_OK; }
  virtual HRESULT STDMETHODCALLTYPE OnIconPathChanged(LPCWSTR NewIconPath, LPCGUID EventContext) { return S_OK; }
  virtual HRESULT STDMETHODCALLTYPE OnSimpleVolumeChanged(float NewVolume, BOOL NewMute, LPCGUID EventContext) { return S_OK; }
  virtual HRESULT STDMETHODCALLTYPE OnChannelVolumeChanged(DWORD ChannelCount, float NewChannelVolumeArray[], DWORD ChangedChannel, LPCGUID EventContext) { return S_OK; }
  virtual HRESULT STDMETHODCALLTYPE OnGroupingParamChanged(LPCGUID NewGroupingParam, LPCGUID EventContext) { return S_OK; }
  virtual HRESULT STDMETHODCALLTYPE OnSessionDisconnected(AudioSessionDisconnectReason DisconnectReason) { return S_OK; }
  virtual HRESULT STDMETHODCALLTYPE OnStateChanged(AudioSessionState NewState);

  HRESULT STDMETHODCALLTYPE QueryInterface(REFIID riid, void** ppv) override { return Listener::QueryInterface(riid, ppv); }
  ULONG STDMETHODCALLTYPE AddRef() override { return Listener::AddRef(); }
  ULONG STDMETHODCALLTYPE Release() override { return Listener::Release(); }
};
