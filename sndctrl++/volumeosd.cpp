#include "pch.h"
#include "volumeosd.h"
#include <uxtheme.h>    // for dbl-buffered painting
#include <thread>

typedef struct {
  UINT    nStep;
  UINT    cSteps;
  BOOL    bMuted;
} VOLUME_INFO;

HINSTANCE g_hInstance = NULL;
wchar_t const g_szWindowClass[] = L"csl_osd";

HWND g_hwndOSD = NULL;
BOOL g_bDblBuffered = FALSE;
VOLUME_INFO g_currentVolume = { 100, 100, 0 };

LRESULT CALLBACK WndProc(HWND, UINT, WPARAM, LPARAM);
float g_fDPIScale = 1.0f;

void ShowVolumeOsd(int volume, bool muted) {
  PostMessage(g_hwndOSD, WM_VOLUMECHANGE, volume, muted);
}

void InitializeDPIScale(HWND hWnd) {
  HDC hdc = GetDC(hWnd);
  g_fDPIScale = GetDeviceCaps(hdc, LOGPIXELSX) / 96.0f;
  ReleaseDC(hWnd, hdc);
}

int DPIScale(int iValue) {
  return static_cast<int>(iValue * g_fDPIScale);
}

void __stdcall initializeOSD(HINSTANCE hInstance) {
  g_hInstance = hInstance;

  auto pThread = new std::thread([] {
    SetProcessDPIAware();

    if (!SUCCEEDED(CoInitializeEx(NULL, COINIT_APARTMENTTHREADED | COINIT_DISABLE_OLE1DDE))) return;
    g_bDblBuffered = SUCCEEDED(BufferedPaintInit());
    g_currentVolume = { 100, 100, false };

    // Init volume monitor
    WNDCLASSEX wcex = { sizeof(wcex) };
    wcex.style = CS_HREDRAW | CS_VREDRAW;
    wcex.lpfnWndProc = WndProc;
    wcex.hInstance = g_hInstance;
    wcex.hCursor = LoadCursor(NULL, IDC_ARROW);
    wcex.hbrBackground = (HBRUSH)(COLOR_WINDOW + 1);
    wcex.lpszClassName = g_szWindowClass;
    auto result = RegisterClassEx(&wcex);
    if (result == 0) {
      cerr << "Unable to register class :( " << GetLastError();
    }

    DWORD const dwStyle = WS_POPUP;
    DWORD const dwStyleEx = WS_EX_LAYERED | WS_EX_TOPMOST | WS_EX_NOACTIVATE;   // transparent, topmost, with no taskbar item
    g_hwndOSD = CreateWindowEx(dwStyleEx, g_szWindowClass, NULL, dwStyle, 0, 0, 0, 0, NULL, NULL, g_hInstance, NULL);
    if (g_hwndOSD) {
      ShowWindow(g_hwndOSD, SW_HIDE);

      MSG msg;
      while (GetMessage(&msg, g_hwndOSD, 0, 0)) {
        TranslateMessage(&msg);
        DispatchMessage(&msg);
      }
    } else {
      cerr << "Unable to create window :(" << GetLastError() << endl;
    }
    });
}

void __stdcall deInitializeOSD() {
  if (g_bDblBuffered) {
    BufferedPaintUnInit();
  }
}

LRESULT CALLBACK WndProc(HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam) {
  static HBRUSH   hbrLit = NULL;
  static HBRUSH   hbrUnlit = NULL;
  static HFONT    hFont = NULL;
  static UINT_PTR nTimerId = 101;

  switch (message) {
  case WM_CREATE: {
    // Make BLACK the transparency color
    SetLayeredWindowAttributes(hWnd, RGB(0, 0, 0), 0, LWA_COLORKEY);

    // Initialize the DPI scalar.
    InitializeDPIScale(hWnd);

    // Create brushes and font that will be used in WM_PAINT
    hbrLit = CreateSolidBrush(RGB(0, 128, 255));
    hbrUnlit = CreateSolidBrush(RGB(0, 64, 128));
    hFont = CreateFont(DPIScale(64), 0, 0, 0, FW_BOLD, FALSE, FALSE, FALSE, 0, 0, 0, 0, 0, L"Segoe UI");

    // Position top right
    POINT const ptZeroZero = {};
    HMONITOR hMonitor = MonitorFromPoint(ptZeroZero, MONITOR_DEFAULTTOPRIMARY);
    MONITORINFO mi = { sizeof(mi) };
    GetMonitorInfo(hMonitor, &mi);

    SIZE const size = { g_currentVolume.cSteps * DPIScale(3), DPIScale(60) };

    auto winWidth = mi.rcMonitor.right - mi.rcMonitor.left;
    auto winHeight = mi.rcMonitor.bottom - mi.rcMonitor.top;
    POINT const pt = {
        mi.rcMonitor.left + winWidth / 40,
        mi.rcMonitor.top + winHeight / 30
    };

    SetWindowPos(hWnd, HWND_TOPMOST, pt.x, pt.y, size.cx, size.cy, SWP_SHOWWINDOW);
    break;
  }
  case WM_PAINT: {
    PAINTSTRUCT     ps;
    HPAINTBUFFER    hBufferedPaint = NULL;
    RECT            rc;

    GetClientRect(hWnd, &rc);
    HDC hdc = BeginPaint(hWnd, &ps);

    if (g_bDblBuffered) {
      HDC hdcMem;
      hBufferedPaint = BeginBufferedPaint(hdc, &rc, BPBF_COMPOSITED, NULL, &hdcMem);
      if (hBufferedPaint) {
        hdc = hdcMem;
      }
    }

    // black background (transparency color)
    FillRect(hdc, &rc, (HBRUSH)GetStockObject(BLACK_BRUSH));

    // Draw LEDs
    for (UINT i = 0; i < (g_currentVolume.cSteps - 1); i++) {
      RECT const rcLed = { DPIScale(i * 10), DPIScale(10), DPIScale(i * 10 + 8), rc.bottom - DPIScale(15) };

      if ((i < g_currentVolume.nStep) && (!g_currentVolume.bMuted)) {
        FillRect(hdc, &rcLed, hbrLit);
      } else {
        FillRect(hdc, &rcLed, hbrUnlit);
      }
    }

    if (g_currentVolume.bMuted) {
      HGDIOBJ hof = SelectObject(hdc, hFont);
      SetBkMode(hdc, TRANSPARENT);
      SetTextColor(hdc, RGB(255, 64, 64));
      RECT rcText = rc;
      rcText.bottom -= DPIScale(11);
      DrawText(hdc, L"MUTED", -1, &rcText, DT_CENTER | DT_SINGLELINE | DT_VCENTER);
      SelectObject(hdc, hof);
    }

    if (hBufferedPaint) {
      // end painting
      BufferedPaintMakeOpaque(hBufferedPaint, NULL);
      EndBufferedPaint(hBufferedPaint, TRUE);
    }

    EndPaint(hWnd, &ps);
    return 0;
  }

  case WM_VOLUMECHANGE:
    g_currentVolume.bMuted = (bool)lParam;
    g_currentVolume.cSteps = 100;
    g_currentVolume.nStep = (int)wParam;

    // make window visible for 2 seconds
    ShowWindow(hWnd, SW_SHOW);
    InvalidateRect(hWnd, NULL, TRUE);
    nTimerId = SetTimer(hWnd, 101, 2000, NULL);
    return 0;
  case WM_TIMER:
    // make the window go away
    ShowWindow(hWnd, SW_HIDE);
    KillTimer(hWnd, nTimerId);
    return 0;
  case WM_DESTROY:
    DeleteObject(hbrLit);
    DeleteObject(hbrUnlit);
    DeleteObject(hFont);
    PostQuitMessage(0);
    return 0;
  case WM_ERASEBKGND:
    return 1;
  }

  return DefWindowProc(hWnd, message, wParam, lParam);
}
