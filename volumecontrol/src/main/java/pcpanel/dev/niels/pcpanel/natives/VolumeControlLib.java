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

  // Volume actions
  void setDeviceVolume(WString id, int volume, int osd);

  void setProcessVolume(WString name, int volume, int osd);

  void setFgProcessVolume(int volume, int osd);

  // State actions
  void setDeviceMute(WString id, int muted, int osd);

  void setProcessMute(WString name, int muted, int osd);

  void setActiveDevice(WString id, int osd);

  interface DeviceChanged extends Callback {
    void invoke(WString name, WString id, int volume, int muted);
  }

  interface StringCallback extends Callback {
    void invoke(WString id);
  }

  interface SessionChanged extends Callback {
    void invoke(long processId, WString name, WString icon, int volume, int muted);
  }

  interface SessionRemoved extends Callback {
    void invoke(long processId);
  }
}
