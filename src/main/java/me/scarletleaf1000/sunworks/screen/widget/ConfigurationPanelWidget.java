package me.scarletleaf1000.sunworks.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.block.entity.io.ConfigurableMachine;
import me.scarletleaf1000.sunworks.block.entity.io.IOType;
import me.scarletleaf1000.sunworks.block.entity.io.RelativeSide;
import me.scarletleaf1000.sunworks.network.SideConfigCyclePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

/**
 * The expandable side-configuration panel shown next to a machine's GUI. Lays its 6 face
 * buttons out in an unfolded-cube cross pattern:
 * <pre>
 *  . U .
 *  L B R
 *  . D F
 * </pre>
 * where U/D/L/R/B/F are up/down/left/right/back/front respectively. Buttons are plain vanilla
 * {@link Button}s (so the screen can add them via {@code addRenderableWidget} normally); this
 * class only owns the background texture and the {@link IOType} icon overlays drawn on top of
 * them, which must be rendered after the buttons (see {@link #renderIcons}).
 */
public class ConfigurationPanelWidget {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(Sunworks.MOD_ID, "textures/gui/container/configuration_panel.png");
    private static final ResourceLocation ICONS =
            ResourceLocation.fromNamespaceAndPath(Sunworks.MOD_ID, "textures/gui/container/io_icons.png");

    public static final int WIDTH = 64;
    public static final int HEIGHT = 84;

    private static final int BUTTON_SIZE = 16;
    private static final int GAP = 3;
    private static final int CELL = BUTTON_SIZE + GAP;
    private static final int GRID_SIZE = BUTTON_SIZE * 3 + GAP * 2;
    private static final int GRID_LEFT = (WIDTH - GRID_SIZE) / 2;
    private static final int GRID_TOP = ConfigurationTabButton.HEIGHT + GAP;
    private static final int ICON_SIZE = 8;
    private static final int ICON_SHEET_WIDTH = ICON_SIZE * IOType.values().length;

    private static final Map<RelativeSide, int[]> LAYOUT = new EnumMap<>(RelativeSide.class);

    static {
        LAYOUT.put(RelativeSide.UP, new int[]{1, 0});
        LAYOUT.put(RelativeSide.LEFT, new int[]{0, 1});
        LAYOUT.put(RelativeSide.BACK, new int[]{1, 1});
        LAYOUT.put(RelativeSide.RIGHT, new int[]{2, 1});
        LAYOUT.put(RelativeSide.DOWN, new int[]{1, 2});
        LAYOUT.put(RelativeSide.FRONT, new int[]{2, 2});
    }

    private final int x;
    private final int y;
    private final ConfigurableMachine machine;
    private final Map<RelativeSide, Button> buttons = new EnumMap<>(RelativeSide.class);

    public ConfigurationPanelWidget(int x, int y, BlockPos pos, ConfigurableMachine machine) {
        this.x = x;
        this.y = y;
        this.machine = machine;

        for (RelativeSide side : RelativeSide.values()) {
            int[] cell = LAYOUT.get(side);
            int buttonX = x + GRID_LEFT + cell[0] * CELL;
            int buttonY = y + GRID_TOP + cell[1] * CELL;

            Button button = Button.builder(Component.empty(),
                        btn -> PacketDistributor.sendToServer(new SideConfigCyclePayload(pos, side)))
                    .bounds(buttonX, buttonY, BUTTON_SIZE, BUTTON_SIZE)
                    .build();
            button.active = machine.isSideConfigurable(side);

            buttons.put(side, button);
        }
    }

    /**
     * @return the 6 face buttons - add each of these via {@code Screen#addRenderableWidget}.
     */
    public Collection<Button> getButtons() {
        return buttons.values();
    }

    public void setVisible(boolean visible) {
        buttons.values().forEach(button -> button.visible = visible);
    }

    /**
     * Refreshes each button's hover tooltip to reflect its current {@link IOType} - call this
     * once per frame before {@code super.render()} (i.e. before widget tooltips are captured)
     * while the panel is expanded, since the configured type can change from server sync.
     */
    public void updateTooltips() {
        for (Map.Entry<RelativeSide, Button> entry : buttons.entrySet()) {
            RelativeSide side = entry.getKey();
            IOType type = machine.getSideConfiguration().get(side);
            Component tooltip = side.getDisplayName().copy().append("\n").append(type.getDisplayName());
            entry.getValue().setTooltip(Tooltip.create(tooltip));
        }
    }

    public void renderBackground(GuiGraphics guiGraphics) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(BACKGROUND, x, y, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);
    }

    /**
     * Draws the small {@link IOType} icon over each button - call this after the buttons
     * themselves have rendered (i.e. after {@code super.render()} in the screen).
     */
    public void renderIcons(GuiGraphics guiGraphics) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        for (Map.Entry<RelativeSide, Button> entry : buttons.entrySet()) {
            Button button = entry.getValue();
            IOType type = machine.getSideConfiguration().get(entry.getKey());
            int iconU = type.getIconIndex() * ICON_SIZE;
            int iconX = button.getX() + (button.getWidth() - ICON_SIZE) / 2;
            int iconY = button.getY() + (button.getHeight() - ICON_SIZE) / 2;

            guiGraphics.blit(ICONS, iconX, iconY, iconU, 0, ICON_SIZE, ICON_SIZE, ICON_SHEET_WIDTH, ICON_SIZE);
        }
    }
}
