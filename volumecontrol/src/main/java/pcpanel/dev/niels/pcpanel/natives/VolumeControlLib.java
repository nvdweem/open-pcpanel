package pcpanel.dev.niels.pcpanel.natives;

import com.sun.jna.Callback;
import com.sun.jna.CallbackThreadInitializer;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.WString;
import one.util.streamex.StreamEx;

public interface VolumeControlLib extends Library {
  VolumeControlLib INSTANCE = Native.load("sndctrlpp", VolumeControlLib.class);

  default void initialize(DeviceChanged changed, StringCallback removed, SessionChanged sessChanged, SessionRemoved sessRemoved, StringCallback debug, StringCallback info) {
    CallbackThreadInitializer thread = new CallbackThreadInitializer(true, false, "VolumeControlLibCB");
    StreamEx.of(changed, removed, sessChanged, sessRemoved, debug, info).forEach(cb -> Native.setCallbackThreadInitializer(cb, thread));
    init(changed, removed, sessChanged, sessRemoved, debug, info);
  }

  void init(DeviceChanged changed, StringCallback removed, SessionChanged sessChanged, SessionRemoved sessRemoved, StringCallback debug, StringCallback info);

  void setDeviceVolume(WString id, int volume, boolean osd);

  void setProcessVolume(WString name, int volume, boolean osd);

  void setFgProcessVolume(int volume, boolean osd);

  boolean toggleDeviceMute(WString volume, boolean osd);

  interface DeviceChanged extends Callback {
    void invoke(WString name, WString id, int volume, boolean muted);
  }

  interface StringCallback extends Callback {
    void invoke(WString id);
  }

  interface SessionChanged extends Callback {
    void invoke(long processId, WString name, WString icon, int volume, boolean muted);
  }

  interface SessionRemoved extends Callback {
    void invoke(long processId);
  }
}
