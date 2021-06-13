package dev.niels.pcpanel.plugins.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface ConfigElement {
  @Retention(RetentionPolicy.RUNTIME) @interface Text {
    String type() default "text";

    String def() default "";

    String label();
  }

  @Retention(RetentionPolicy.RUNTIME) @interface Number {
    String type() default "number";

    String def() default "";

    String label();
  }

  @Retention(RetentionPolicy.RUNTIME) @interface Checkbox {
    String type() default "checkbox";

    boolean def() default false;

    String label();
  }

  @Retention(RetentionPolicy.RUNTIME) @interface TextArea {
    String type() default "textarea";

    String def() default "";

    String label();
  }

  @Retention(RetentionPolicy.RUNTIME) @interface Slider {
    String type() default "slider";

    int def() default 0;

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

  @Retention(RetentionPolicy.RUNTIME) @interface Dropdown {
    String type() default "list";

    String label();

    String listSource();
  }

  @Retention(RetentionPolicy.RUNTIME) @interface Radio {
    String type() default "radio";

    String def() default "";

    String label();

    String[] options();

    boolean horizontal() default true;
  }


  @Retention(RetentionPolicy.RUNTIME) @interface Color {
    String type() default "color";

    String def() default "";

    String label();
  }
}
