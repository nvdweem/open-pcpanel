package dev.niels.pcpanel;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.util.StringUtils;

import java.awt.Color;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.regex.Pattern;

@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = JsonColor.ColorSerializer.class)
@JsonDeserialize(using = JsonColor.ColorDeserializer.class)
public @interface JsonColor {

  class ColorSerializer extends StdSerializer<Color> {
    public ColorSerializer() {
      super(Color.class);
    }

    @Override public void serialize(Color value, JsonGenerator gen, SerializerProvider provider) throws IOException {
      gen.writeString(String.format("#%06x", value.getRGB() & 0x00FFFFFF));
    }
  }

  class ColorDeserializer extends StdDeserializer<Color> {
    private static final Pattern rgbPattern = Pattern.compile("rgb\\((\\d+), (\\d+), (\\d+)\\)");

    protected ColorDeserializer() {
      super(Color.class);
    }

    @Override public Color deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      return parseColor(p.getText());
    }

    private Color parseColor(String color) {
      if (StringUtils.hasText(color)) {
        var m = rgbPattern.matcher(color);
        if (m.find()) {
          return new Color(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
        }
        try {
          return Color.decode(color);
        } catch (Exception e) {
          // Not rgb
        }
      }
      return Color.white;
    }
  }
}
