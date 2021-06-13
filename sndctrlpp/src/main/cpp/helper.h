#pragma once
#include "pch.h"

struct CoRelease {
  void operator()(LPVOID itm) {
    if (itm != NULL) {
      CoTaskMemFree(itm);
    }
  }
};

template<typename T> using co_ptr = std::unique_ptr<T, CoRelease>;

struct SDeviceNameId {
  co_ptr<WCHAR> name;
  co_ptr<WCHAR> id;
};


CComPtr<IMMDeviceCollection>        EnumAudioEndpoints(IMMDeviceEnumerator& enumerator);
UINT                                GetCount(IMMDeviceCollection& collection);
CComPtr<IMMDevice>                  DeviceFromCollection(IMMDeviceCollection& collection, UINT idx);
SDeviceNameId                       DeviceNameId(IMMDevice& device);

CComPtr<IAudioSessionManager2>      Activate(IMMDevice& device);
CComPtr<IAudioSessionControl>       GetAudioSessionControl(IAudioSessionManager2& sessionManager);

CComPtr<IAudioSessionEnumerator>    GetSessionEnumerator(IAudioSessionManager2& sessionManager);
int                                 GetCount(IAudioSessionEnumerator& collection);
CComPtr<IAudioSessionControl>       GetSession(IAudioSessionEnumerator& collection, int idx);

CComPtr<IAudioSessionControl2>      GetSession2(IAudioSessionControl& control);
DWORD                               GetProcessId(IAudioSessionControl2& control2);
wstring                             ProcessIdToName(DWORD processId);

CComPtr<IAudioEndpointVolume>       GetVolumeControl(IMMDevice& device);
CComPtr<ISimpleAudioVolume>         GetVolumeControl(IAudioSessionControl& session);

wstring                             GetProcessName(DWORD procId);
EDataFlow                           getDataFlow(IMMDevice& device);
