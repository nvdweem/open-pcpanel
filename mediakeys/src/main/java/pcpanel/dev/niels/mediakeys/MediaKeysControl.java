package pcpanel.dev.niels.mediakeys;

import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import dev.niels.pcpanel.plugins.Action;
import dev.niels.pcpanel.plugins.Control;
import dev.niels.pcpanel.plugins.KnobAction;
import dev.niels.pcpanel.plugins.config.ActionConfig;
import dev.niels.pcpanel.plugins.config.ConfigElement;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class MediaKeysControl implements KnobAction<MediaKeysControl.MediaKeysConfig> {
  public static final int VK_VOLUME_MUTE = 0xAD;
  public static final int VK_MEDIA_NEXT_TRACK = 0xB0;
  public static final int VK_MEDIA_PREV_TRACK = 0xB1;
  public static final int VK_MEDIA_STOP = 0xB2;
  public static final int VK_MEDIA_PLAY_PAUSE = 0xB3;

  @Override public String getName() {
    return "Media key";
  }

  @Override public Class<MediaKeysConfig> getConfigurationClass() {
    return MediaKeysConfig.class;
  }

  @Override public void buttonDown(Control control, MediaKeysConfig config) {
    int key;
    switch (Optional.ofNullable(config.getKey()).orElse("Mute")) {
      case "Play/Pause":
        key = VK_MEDIA_PLAY_PAUSE;
        break;
      case "Stop":
        key = VK_MEDIA_STOP;
        break;
      case "Previous":
        key = VK_MEDIA_PREV_TRACK;
        break;
      case "Next":
        key = VK_MEDIA_NEXT_TRACK;
        break;
      case "Mute":
      default:
        key = VK_VOLUME_MUTE;
        break;
    }
    pressButton(key);
  }

  private void pressButton(int button) {
    WinUser.INPUT input = new WinUser.INPUT();
    input.type = new WinDef.DWORD(WinUser.INPUT.INPUT_KEYBOARD);
    input.input.setType("ki");
    input.input.ki.wScan = new WinDef.WORD(0);
    input.input.ki.time = new WinDef.DWORD(0);
    input.input.ki.dwExtraInfo = new BaseTSD.ULONG_PTR(0);
    input.input.ki.wVk = new WinDef.WORD(button);
    input.input.ki.dwFlags = new WinDef.DWORD(0);  // keydown

    User32.INSTANCE.SendInput(new WinDef.DWORD(1), (WinUser.INPUT[]) input.toArray(1), input.size());
    input.input.ki.dwFlags = new WinDef.DWORD(2);  // keyup
    User32.INSTANCE.SendInput(new WinDef.DWORD(1), (WinUser.INPUT[]) input.toArray(1), input.size());
  }

  @Data
  public static class MediaKeysConfig implements ActionConfig {
    @ConfigElement.Radio(label = "Key", horizontal = false, options = {"Play/Pause", "Stop", "Previous", "Next", "Mute"}, def = "Play/Pause") private String key;

    @Override public Class<? extends Action<?>> getActionClass() {
      return MediaKeysControl.class;
    }
  }
}
