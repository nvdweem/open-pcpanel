package dev.niels.pcpanel.core;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class SpringContext {
  private static ApplicationContext context;

  public SpringContext(ApplicationContext context) {
    SpringContext.context = context;
  }

  public static <T> T getBean(Class<T> clazz) {
    return context.getBean(clazz);
  }
}
