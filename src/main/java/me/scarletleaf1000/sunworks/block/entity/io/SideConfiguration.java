package me.scarletleaf1000.sunworks.block.entity.io;

import net.minecraft.nbt.CompoundTag;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Per-machine mapping of {@link RelativeSide} to {@link IOType}. Defaults every side to
 * {@link IOType#NONE} and is cheap to persist (6 short strings).
 */
public class SideConfiguration {
    private final Map<RelativeSide, IOType> assignments = new EnumMap<>(RelativeSide.class);

    public SideConfiguration() {
        for (RelativeSide side : RelativeSide.values()) {
            assignments.put(side, IOType.NONE);
        }
    }

    public IOType get(RelativeSide side) {
        return assignments.getOrDefault(side, IOType.NONE);
    }

    public void set(RelativeSide side, IOType type) {
        assignments.put(side, type);
    }

    /**
     * Cycles the given side to the next {@code allowed} type (wrapping back to
     * {@link IOType#NONE}), skipping over any type the machine doesn't support.
     */
    public IOType cycle(RelativeSide side, Set<IOType> allowed) {
        IOType[] values = IOType.values();
        int index = get(side).ordinal();

        for (int i = 0; i < values.length; i++) {
            index = (index + 1) % values.length;
            IOType candidate = values[index];
            if (candidate == IOType.NONE || allowed.contains(candidate)) {
                set(side, candidate);
                return candidate;
            }
        }

        set(side, IOType.NONE);
        return IOType.NONE;
    }

    public void save(CompoundTag tag) {
        for (RelativeSide side : RelativeSide.values()) {
            tag.putString(side.name(), get(side).name());
        }
    }

    public void load(CompoundTag tag) {
        for (RelativeSide side : RelativeSide.values()) {
            if (tag.contains(side.name())) {
                assignments.put(side, IOType.valueOf(tag.getString(side.name())));
            }
        }
    }
}
