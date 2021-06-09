// dllmain.cpp : Defines the entry point for the DLL application.
#include "pch.h"
#include "sndctrl.h"
#include "volumeosd.h"

BOOL APIENTRY DllMain( HMODULE hModule,
                       DWORD  ul_reason_for_call,
                       LPVOID lpReserved
                     )
{
    switch (ul_reason_for_call)
    {
    case DLL_PROCESS_ATTACH:
      init();
      initializeOSD(hModule);
      break;
    case DLL_PROCESS_DETACH:
      deinit();
      deInitializeOSD();
      break;
    case DLL_THREAD_ATTACH:
    case DLL_THREAD_DETACH:
        break;
    }
    return TRUE;
}
