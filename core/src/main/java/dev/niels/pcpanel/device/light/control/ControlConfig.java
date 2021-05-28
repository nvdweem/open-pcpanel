package dev.niels.pcpanel.device.light.control;

import dev.niels.pcpanel.helper.ByteArrayBuilder;

public abstract class ControlConfig {
    private static final int COMMAND_LENGTH = 7;

    public void appendToBuilder(ByteArrayBuilder builder) {
        builder.mark();
        doAppend(builder);
        builder.pad(COMMAND_LENGTH);
    }

    protected abstract void doAppend(ByteArrayBuilder builder);
}
