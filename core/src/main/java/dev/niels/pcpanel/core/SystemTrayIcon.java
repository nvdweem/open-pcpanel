package dev.niels.pcpanel.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemTrayIcon {
  private final Environment environment;

  @PostConstruct
  public void init() throws IOException, AWTException {
    if (!SystemTray.isSupported()) {
      log.warn("System tray icon not supported");
    }

    var icon = new TrayIcon(ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/icon.png"))));
    icon.addActionListener(e -> openBrowser());
    SystemTray.getSystemTray().add(icon);

    var pp = new PopupMenu();
    pp.add(mkMenu("Configure", e -> openBrowser()));
    pp.addSeparator();
    pp.add(mkMenu("Exit", e -> System.exit(0)));
    icon.setPopupMenu(pp);
  }

  private void openBrowser() {
    log.debug("Open browser");
    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
      try {
        Desktop.getDesktop().browse(new URI("http://localhost:" + environment.getProperty("local.server.port")));
      } catch (Exception e) {
        log.error("Unable to browse");
      }
    }
  }

  private MenuItem mkMenu(String label, ActionListener action) {
    var result = new MenuItem(label);
    result.addActionListener(action);
    return result;
  }
}
