package dev.niels.pcpanel.core.helper;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@NoArgsConstructor
public class ByteArrayBuilder {
  private final ByteArrayOutputStream stream = new ByteArrayOutputStream(64);
  private int afterMark = 0;

  public byte[] getBytes() {
    return stream.toByteArray();
  }

  public ByteArrayBuilder(int... initial) {
    append(initial);
  }

  public ByteArrayBuilder append(Color c) {
    if (c == null) {
      return append(0, 0, 0);
    } else {
      return append(c.getRed(), c.getGreen(), c.getBlue());
    }
  }

  public ByteArrayBuilder append(int... nrs) {
    for (var nr : nrs) {
      stream.write(nr);
    }
    afterMark += nrs.length;
    return this;
  }

  public ByteArrayBuilder append(byte[] buff) {
    try {
      stream.write(buff);
      afterMark += buff.length;
    } catch (IOException e) {
      log.error("Unable to write buff", e);
    }
    return this;
  }

  public ByteArrayBuilder mark() {
    afterMark = 0;
    return this;
  }

  public ByteArrayBuilder pad(int lengthAfterMark) {
    while (afterMark != lengthAfterMark) {
      append(0);
    }
    return this;
  }
}
