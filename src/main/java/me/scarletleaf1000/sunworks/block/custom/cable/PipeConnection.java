package me.scarletleaf1000.sunworks.block.custom.cable;

import net.minecraft.util.StringRepresentable;

public enum PipeConnection implements StringRepresentable {
    NONE("none"),
    PIPE("pipe"),
    EXTRACT("extract");

    private final String name;

    PipeConnection(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
