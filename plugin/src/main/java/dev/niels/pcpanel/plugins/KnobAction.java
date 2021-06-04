package dev.niels.pcpanel.plugins;

public interface KnobAction<T> extends Action<T> {
  void triggerAction(Control control, T config, boolean down);
}
