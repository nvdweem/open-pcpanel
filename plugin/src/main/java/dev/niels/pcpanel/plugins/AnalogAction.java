package dev.niels.pcpanel.plugins;

public interface AnalogAction<T> extends Action<T> {
  void triggerAction(Control control, T config, int sliderPos);
}
