#include "pch.h"
#include "helper.h"

CComPtr<IMMDeviceCollection> EnumAudioEndpoints(IMMDeviceEnumerator& enumerator) {
	IMMDeviceCollection* pDeviceCol = NULL;
	enumerator.EnumAudioEndpoints(eAll, DEVICE_STATE_ACTIVE, &pDeviceCol);
	return CComPtr<IMMDeviceCollection>(pDeviceCol);
}

UINT GetCount(IMMDeviceCollection& collection) {
	UINT count;
	collection.GetCount(&count);
	return count;;
}

CComPtr<IMMDevice> DeviceFromCollection(IMMDeviceCollection& collection, UINT idx) {
	CComPtr<IMMDevice> pDevice;
	collection.Item(idx, &pDevice);
	return pDevice;
}

SDeviceNameId DeviceNameId(IMMDevice& device) {
	LPWSTR pwszID = NULL;

	device.GetId(&pwszID);

	IPropertyStore* pProps = NULL;
	device.OpenPropertyStore(STGM_READ, &pProps);
	auto props = CComPtr<IPropertyStore>(pProps);

	PROPVARIANT varName;
	PropVariantInit(&varName);

	// Get the endpoint's friendly-name property.
	pProps->GetValue(PKEY_Device_FriendlyName, &varName);

	return SDeviceNameId{ co_ptr<WCHAR>(varName.pwszVal), co_ptr<WCHAR>(pwszID) };
}

CComPtr<IAudioSessionManager2> Activate(IMMDevice& device) {
	CComPtr<IAudioSessionManager2> pSessionManager;
	device.Activate(__uuidof(IAudioSessionManager2), CLSCTX_ALL, NULL, (void**)&pSessionManager);
	return pSessionManager;
}

CComPtr<IAudioSessionControl> GetAudioSessionControl(IAudioSessionManager2& sessionManager) {
	CComPtr<IAudioSessionControl> pSessionControl;
	sessionManager.GetAudioSessionControl(0, FALSE, &pSessionControl);
	return pSessionControl;
}

CComPtr<IAudioSessionEnumerator> GetSessionEnumerator(IAudioSessionManager2& sessionManager) {
	CComPtr<IAudioSessionEnumerator> pSessionList;
	sessionManager.GetSessionEnumerator(&pSessionList);
	return pSessionList;
}

int GetCount(IAudioSessionEnumerator& collection) {
	int sessionCount;
	collection.GetCount(&sessionCount);
	return sessionCount;
}

CComPtr<IAudioSessionControl> GetSession(IAudioSessionEnumerator& collection, int idx) {
	CComPtr<IAudioSessionControl> pSessionControl;
	collection.GetSession(idx, &pSessionControl);
	return pSessionControl;
}

CComPtr<IAudioSessionControl2> GetSession2(IAudioSessionControl& control) {
	CComPtr<IAudioSessionControl2> pSessionControl2 = NULL;
	control.QueryInterface(__uuidof(IAudioSessionControl2), (void**)&pSessionControl2);
	return pSessionControl2;
}

DWORD GetProcessId(IAudioSessionControl2& control2) {
	DWORD procID;
	control2.GetProcessId(&procID);
	return procID;
}

wstring ProcessIdToName(DWORD processId) {
	HANDLE handle = OpenProcess(PROCESS_QUERY_LIMITED_INFORMATION, FALSE, processId);
	if (handle) {
		WCHAR buffer[1024];
		DWORD bufferSize = 1024;
		if (!QueryFullProcessImageName(handle, 0, buffer, &bufferSize)) {
			printf("Error GetModuleBaseNameA : %lu", GetLastError());
		}
		CloseHandle(handle);
		return wstring(buffer);
	} else {
		printf("Error OpenProcess : %lu", GetLastError());
		return wstring();
	}
}

CComPtr<IAudioEndpointVolume> GetVolumeControl(IMMDevice& device) {
	CComPtr<IAudioEndpointVolume> pVol;
	device.Activate(__uuidof(IAudioEndpointVolume), CLSCTX_ALL, NULL, (void**)&pVol);
	return pVol;
}

CComPtr<ISimpleAudioVolume> GetVolumeControl(IAudioSessionControl& session) {
	CComPtr<ISimpleAudioVolume> pControl;
	auto hr = session.QueryInterface(__uuidof(ISimpleAudioVolume), (void**)&pControl);
	return pControl;
}
