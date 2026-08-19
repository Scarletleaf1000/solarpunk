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

    /**
     * @return whether this machine offers the "eject" toggle in the configuration panel at all -
     * machines with nothing to proactively push (e.g. no energy output) should leave this false
     * so the button doesn't show up for them.
     */
    default boolean supportsEject() {
        return false;
    }

    /**
     * @return whether eject mode is currently on. While on, the machine actively pushes its
     * output resource out through every side configured as an output - but only into neighbors
     * that are <b>not</b> pipes, since pipes already pull from connected machines themselves via
     * their own network sweep (see {@code EnergyPipeBlockEntity}); pushing into a pipe as well
     * would double up on that same per-tick budget.
     */
    default boolean isEjectEnabled() {
        return false;
    }

    default void setEjectEnabled(boolean enabled) {
    }
}
