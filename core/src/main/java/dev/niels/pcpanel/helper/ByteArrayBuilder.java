package dev.niels.pcpanel.helper;

import lombok.NoArgsConstructor;

import java.awt.Color;
import java.io.ByteArrayOutputStream;

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
        return append(c.getRed(), c.getGreen(), c.getBlue());
    }

    public ByteArrayBuilder append(int... nrs) {
        for (var nr : nrs) {
            stream.write(nr);
        }
        afterMark += nrs.length;
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
