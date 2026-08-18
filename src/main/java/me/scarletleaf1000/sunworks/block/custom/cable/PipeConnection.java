package me.scarletleaf1000.sunworks.block.custom.cable;

import net.minecraft.util.StringRepresentable;

/**
 * The visual/topological connection state of a single pipe face - purely automatic, computed
 * from whatever block is adjacent. {@link #PIPE} means another pipe segment; {@link #MACHINE}
 * means any other block exposing an energy capability (a machine, or another mod's cable).
 * Whether energy actually flows in or out of a {@link #MACHINE} face each tick is decided
 * dynamically from that neighbor's own {@code canExtract()}/{@code canReceive()} flags - there
 * is no separate "extractor" pipe or per-side toggle.
 */
public enum PipeConnection implements StringRepresentable {
    NONE("none"),
    PIPE("pipe"),
    MACHINE("machine");

    private final String name;

    PipeConnection(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
