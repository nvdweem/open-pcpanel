#pragma once
#include "pch.h"

extern HWND g_hwndOSD;


void __stdcall initializeOSD(HINSTANCE hInstance);
void __stdcall deInitializeOSD();

#define WM_VOLUMECHANGE     (WM_USER + 12)

void ShowVolumeOsd(int volume, bool muted);
