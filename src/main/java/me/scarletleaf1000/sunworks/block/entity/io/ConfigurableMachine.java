package me.scarletleaf1000.sunworks.block.entity.io;

import net.minecraft.core.Direction;

import java.util.Set;

/**
 * Implemented by any block entity - a machine today, and eventually an item/fluid pipe - that
 * exposes a per-side {@link IOType} configuration through the configuration tab GUI widget.
 */
public interface ConfigurableMachine {
    SideConfiguration getSideConfiguration();

    /**
     * @return the set of {@link IOType}s this machine can ever be configured to use - the
     * configuration tab skips over any type not in this set when cycling.
     */
    Set<IOType> getSupportedTypes();

    /**
     * @return whether the given side can be configured at all. Sides that return {@code false}
     * are permanently locked to {@link IOType#NONE} and their button is disabled in the
     * configuration tab - useful for machines where only specific faces make sense (e.g. a
     * solar panel that should only ever output power from its bottom).
     */
    default boolean isSideConfigurable(RelativeSide side) {
        return true;
    }

    /**
     * @return the direction this machine is facing, used to translate {@link RelativeSide}
     * into an absolute world {@link Direction}. Defaults to north for machines with no facing.
     */
    default Direction getFacing() {
        return Direction.NORTH;
    }
}
