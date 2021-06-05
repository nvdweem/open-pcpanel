package pcpanel.dev.niels.pcpanel.natives;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.WString;

public interface WindowsSndLibrary extends Library {
  WindowsSndLibrary INSTANCE = Native.load("SndCtrl", WindowsSndLibrary.class);

  void init(DeviceAdded added, StringCallback removed, SessionAdded sessAdded, SessionRemoved sessRemoved);

  void setDeviceVolume(WString id, int volume, boolean osd);

  void setProcessVolume(WString name, int volume, boolean osd);

  void setFgProcessVolume(int volume, boolean osd);

  boolean toggleDeviceMute(WString volume, boolean osd);

  void getForegroundProcess(StringCallback cb);

  interface DeviceAdded extends Callback {
    void invoke(WString name, WString id);
  }

  interface StringCallback extends Callback {
    void invoke(WString id);
  }

  interface SessionAdded extends Callback {
    void invoke(long processId, WString name);
  }

  interface SessionRemoved extends Callback {
    void invoke(long processId);
  }
}
