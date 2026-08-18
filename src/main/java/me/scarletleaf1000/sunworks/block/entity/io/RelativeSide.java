package me.scarletleaf1000.sunworks.block.entity.io;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

/**
 * A side of a machine expressed relative to the direction it is facing, rather than an
 * absolute world {@link Direction}. Lets every machine share the same front/back/left/right/
 * up/down configuration UI regardless of which way it happens to be placed.
 *
 * <p>Assumes a horizontal-only facing (as with {@code HorizontalDirectionalBlock}) - up/down
 * are always the world's up/down.
 */
public enum RelativeSide {
    FRONT, BACK, LEFT, RIGHT, UP, DOWN;

    public Direction toAbsolute(Direction facing) {
        return switch (this) {
            case FRONT -> facing;
            case BACK -> facing.getOpposite();
            case LEFT -> facing.getCounterClockWise();
            case RIGHT -> facing.getClockWise();
            case UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
        };
    }

    public static RelativeSide fromAbsolute(Direction facing, Direction absolute) {
        for (RelativeSide side : values()) {
            if (side.toAbsolute(facing) == absolute) {
                return side;
            }
        }
        throw new IllegalStateException("Unreachable: every Direction maps to a RelativeSide");
    }

    public Component getDisplayName() {
        return Component.translatable("gui.sunworks.side." + name().toLowerCase());
    }
}
