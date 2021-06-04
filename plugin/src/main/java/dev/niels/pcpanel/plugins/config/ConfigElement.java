package dev.niels.pcpanel.plugins.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface ConfigElement {
  @Retention(RetentionPolicy.RUNTIME) @interface Text {
    String type() default "text";

    String label();
  }

  @Retention(RetentionPolicy.RUNTIME) @interface Number {
    String type() default "number";

    String label();
  }

  @Retention(RetentionPolicy.RUNTIME) @interface Checkbox {
    String type() default "checkbox";

    String label();
  }

  @Retention(RetentionPolicy.RUNTIME) @interface TextArea {
    String type() default "textarea";

    String label();
  }

  @Retention(RetentionPolicy.RUNTIME) @interface Slider {
    String type() default "slider";

    String label();

    int min() default 0;

    int max() default 100;
  }

  @Retention(RetentionPolicy.RUNTIME) @interface FilePicker {
    String type() default "filepicker";

    String label();

    String extension() default "";
  }

  @Retention(RetentionPolicy.RUNTIME) @interface Label {
    String type() default "label";

    String value();
  }
}