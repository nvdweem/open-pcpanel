#pragma once
#include "helper.h"

#ifdef SNDCTRL_EXPORTS
#define SNDCTRL_API __declspec(dllexport)
#else
#define SNDCTRL_API __declspec(dllimport)
#endif

void init();
void deinit();

void ProcessIdToName(DWORD processId, LPWSTR buffer, DWORD bufferSize);

using StringCallback = void(const LPWSTR);
using DeviceChangedCb = void(const LPWSTR name, const LPWSTR id, int volume, int muted, int type);
using DeviceRemovedCb = void(const LPWSTR);
using SessionChangedCb = void(const DWORD pid, const LPWSTR name, const LPWSTR icon, int volume, int muted);
using SessionRemovedCb = void(const DWORD);
using DefaultDeviceChangedCb = void(const LPWSTR id, int type, int role);

extern "C" SNDCTRL_API void init(
  DeviceChangedCb deviceChanged, DeviceRemovedCb deviceRemoved,
  SessionChangedCb sessionChanged, SessionRemovedCb sessionRemoved,
  DefaultDeviceChangedCb defaultDeviceChanged,
  StringCallback debug, StringCallback info);


// Volume actions
extern "C" SNDCTRL_API void setDeviceVolume(const LPWSTR id, int volume, bool osd);
extern "C" SNDCTRL_API void setProcessVolume(const LPWSTR name, int volume, bool osd);
extern "C" SNDCTRL_API void setFgProcessVolume(int volume, bool osd);

// State actions
extern "C" SNDCTRL_API void setDeviceMute(const LPWSTR id, int muted, int osd);
extern "C" SNDCTRL_API void setProcessMute(const LPWSTR name, int muted, int osd);
extern "C" SNDCTRL_API void setActiveDevice(const LPWSTR id, int osd);

void DeviceAdded(CComPtr<IMMDevice> pDevice);


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

class DeviceListener : public Listener, public IMMNotificationClient {
private:
  CComPtr<IMMDeviceEnumerator> pEnumerator;
public:
  DeviceListener(CComPtr<IMMDeviceEnumerator> e);
  void Stop();

  virtual HRESULT STDMETHODCALLTYPE OnDeviceStateChanged(LPCWSTR pwstrDeviceId, DWORD dwNewState) { return S_OK; }
  virtual HRESULT STDMETHODCALLTYPE OnDefaultDeviceChanged(EDataFlow flow, ERole role, LPCWSTR pwstrDefaultDeviceId);
  virtual HRESULT STDMETHODCALLTYPE OnPropertyValueChanged(LPCWSTR pwstrDeviceId, const PROPERTYKEY key) { return S_OK; }
  virtual HRESULT STDMETHODCALLTYPE OnDeviceAdded(LPCWSTR pwstrDeviceId);
  virtual HRESULT STDMETHODCALLTYPE OnDeviceRemoved(LPCWSTR pwstrDeviceId);

  HRESULT STDMETHODCALLTYPE QueryInterface(REFIID riid, void** ppv) override { return Listener::QueryInterface(riid, ppv); }
  ULONG STDMETHODCALLTYPE AddRef() override { return Listener::AddRef(); }
  ULONG STDMETHODCALLTYPE Release() override { return Listener::Release(); }
};

class DeviceVolumeListener : public Listener, public IAudioEndpointVolumeCallback {
private:
  CComPtr<IMMDevice> pDevice;
  CComPtr<IAudioEndpointVolume> pVolume;
public:
  DeviceVolumeListener(CComPtr<IMMDevice> pDevice);
  void Stop();

  virtual HRESULT STDMETHODCALLTYPE OnNotify(PAUDIO_VOLUME_NOTIFICATION_DATA pNotify);

  HRESULT STDMETHODCALLTYPE QueryInterface(REFIID riid, void** ppv) override { return Listener::QueryInterface(riid, ppv); }
  ULONG STDMETHODCALLTYPE AddRef() override { return Listener::AddRef(); }
  ULONG STDMETHODCALLTYPE Release() override { return Listener::Release(); }
};


class SessionListener : public Listener, public IAudioSessionNotification {
private:
  CComPtr<IAudioSessionManager2> sessionManager;
  wstring deviceId;

public:
  SessionListener(CComPtr<IAudioSessionManager2> sessionManager, wstring deviceId) : sessionManager(sessionManager), deviceId(deviceId) {
    sessionManager->RegisterSessionNotification(this);
  }

  HRESULT STDMETHODCALLTYPE OnSessionCreated(IAudioSessionControl* pNewSession);
  void Stop() {
    sessionManager->UnregisterSessionNotification(this);
  }

  HRESULT STDMETHODCALLTYPE QueryInterface(REFIID riid, void** ppv) override { return Listener::QueryInterface(riid, ppv); }
  ULONG STDMETHODCALLTYPE AddRef() override { return Listener::AddRef(); }
  ULONG STDMETHODCALLTYPE Release() override { return Listener::Release(); }
};

class AudioSessionListener : public Listener, public IAudioSessionEvents {
private:
  CComPtr<IAudioSessionControl> sessionControl;
  DWORD pid;
  wstring processName;
  wstring icon;
  int volume;
  BOOL muted;

public:
  AudioSessionListener(CComPtr<IAudioSessionControl> sessionControl, DWORD pid, wstring processName);
  void Stop();

  virtual HRESULT STDMETHODCALLTYPE OnDisplayNameChanged(LPCWSTR NewDisplayName, LPCGUID EventContext) { return S_OK; }
  virtual HRESULT STDMETHODCALLTYPE OnIconPathChanged(LPCWSTR NewIconPath, LPCGUID EventContext);
  virtual HRESULT STDMETHODCALLTYPE OnChannelVolumeChanged(DWORD ChannelCount, float NewChannelVolumeArray[], DWORD ChangedChannel, LPCGUID EventContext) { return S_OK; }
  virtual HRESULT STDMETHODCALLTYPE OnGroupingParamChanged(LPCGUID NewGroupingParam, LPCGUID EventContext) { return S_OK; }
  virtual HRESULT STDMETHODCALLTYPE OnSessionDisconnected(AudioSessionDisconnectReason DisconnectReason) { return S_OK; }
  virtual HRESULT STDMETHODCALLTYPE OnSimpleVolumeChanged(float NewVolume, BOOL NewMute, LPCGUID EventContext);
  virtual HRESULT STDMETHODCALLTYPE OnStateChanged(AudioSessionState NewState);

  HRESULT STDMETHODCALLTYPE QueryInterface(REFIID riid, void** ppv) override { return Listener::QueryInterface(riid, ppv); }
  ULONG STDMETHODCALLTYPE AddRef() override { return Listener::AddRef(); }
  ULONG STDMETHODCALLTYPE Release() override { return Listener::Release(); }
private:
  void SendUpdate();
};
