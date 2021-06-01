package dev.niels.pcpanel.plugins;

public interface AnalogAction<T> extends Action<T> {
  void triggerAction(T config, int sliderPos);
}
