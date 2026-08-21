package me.scarletleaf1000.sunworks.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.block.entity.io.ConfigurableMachine;
import me.scarletleaf1000.sunworks.block.entity.io.IOType;
import me.scarletleaf1000.sunworks.block.entity.io.RelativeSide;
import me.scarletleaf1000.sunworks.network.EjectTogglePayload;
import me.scarletleaf1000.sunworks.network.SideConfigCyclePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The expandable side-configuration panel shown as a popup centered over a machine's GUI. Lays
 * its 6 face buttons out in an unfolded-cube cross pattern:
 * <pre>
 *  . U .
 *  R F L
 *  . D B
 * </pre>
 * where U/D/L/R/B/F are up/down/left/right/back/front respectively. Buttons are plain vanilla
 * {@link Button}s. They are added as children (not renderables) so the screen can draw the
 * panel background over the inventory slots/items first, then render the buttons on top of it
 * (see {@link #renderButtons}). This class owns the background texture and the {@link IOType}
 * icon overlays drawn on top of the buttons (see {@link #renderIcons}).
 *
 * <p>The background texture is a standard 256x256 GUI sheet, but the actual visible popup
 * content only occupies the top-left {@link #CONTENT_WIDTH}x{@link #CONTENT_HEIGHT} corner of
 * it - see {@link #renderBackground}.
 */
public class ConfigurationPanelWidget {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(Sunworks.MOD_ID, "textures/gui/container/io_configuration_panel.png");
    private static final ResourceLocation ICONS =
            ResourceLocation.fromNamespaceAndPath(Sunworks.MOD_ID, "textures/gui/container/io_icons.png");

    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;

    public static final int CONTENT_WIDTH = 140;
    public static final int CONTENT_HEIGHT = 70;

    private static final int BUTTON_SIZE = 16;
    private static final int GAP = 3;
    private static final int CELL = BUTTON_SIZE + GAP;
    private static final int GRID_SIZE = BUTTON_SIZE * 3 + GAP * 2;
    private static final int GRID_LEFT = (CONTENT_WIDTH - GRID_SIZE) / 2;
    private static final int GRID_TOP = 4;
    private static final int ICON_SIZE = 8;

    private static final int EJECT_BUTTON_WIDTH = 110;
    private static final int EJECT_BUTTON_HEIGHT = 16;
    private static final int EJECT_BUTTON_LEFT = (CONTENT_WIDTH - EJECT_BUTTON_WIDTH) / 2;
    private static final int EJECT_BUTTON_TOP = 58;
    private static final int ICON_SHEET_WIDTH = ICON_SIZE * IOType.values().length;

    private static final Map<RelativeSide, int[]> LAYOUT = new EnumMap<>(RelativeSide.class);

    static {
        LAYOUT.put(RelativeSide.UP, new int[]{1, 0});
        LAYOUT.put(RelativeSide.RIGHT, new int[]{0, 1});
        LAYOUT.put(RelativeSide.FRONT, new int[]{1, 1});
        LAYOUT.put(RelativeSide.LEFT, new int[]{2, 1});
        LAYOUT.put(RelativeSide.DOWN, new int[]{1, 2});
        LAYOUT.put(RelativeSide.BACK, new int[]{2, 2});
    }

    private int x;
    private int y;
    private final ConfigurableMachine machine;
    private final Map<RelativeSide, Button> buttons = new EnumMap<>(RelativeSide.class);
    @Nullable
    private final Button ejectButton;

    public ConfigurationPanelWidget(int screenWidth, int screenHeight, BlockPos pos, ConfigurableMachine machine) {
        this.machine = machine;

        for (RelativeSide side : RelativeSide.values()) {
            Button button = Button.builder(Component.empty(),
                        btn -> PacketDistributor.sendToServer(new SideConfigCyclePayload(pos, side)))
                    .bounds(0, 0, BUTTON_SIZE, BUTTON_SIZE)
                    .build();
            button.active = machine.isSideConfigurable(side);

            buttons.put(side, button);
        }

        this.ejectButton = machine.supportsEject()
                ? Button.builder(ejectLabel(machine.isEjectEnabled()),
                        btn -> PacketDistributor.sendToServer(new EjectTogglePayload(pos)))
                    .bounds(0, 0, EJECT_BUTTON_WIDTH, EJECT_BUTTON_HEIGHT)
                    .build()
                : null;

        updateLayout(screenWidth, screenHeight);
    }

    private static Component ejectLabel(boolean enabled) {
        return Component.translatable(enabled ? "gui.sunworks.eject_on" : "gui.sunworks.eject_off");
    }

    /**
     * Recomputes the popup's centered position (and every button's position) for the given
     * screen size - call this from the screen's {@code init()}, since the screen may be resized.
     */
    public void updateLayout(int screenWidth, int screenHeight) {
        this.x = (screenWidth - CONTENT_WIDTH) / 2;
        this.y = (screenHeight - CONTENT_HEIGHT) / 2;

        for (Map.Entry<RelativeSide, Button> entry : buttons.entrySet()) {
            int[] cell = LAYOUT.get(entry.getKey());
            Button button = entry.getValue();
            button.setX(x + GRID_LEFT + cell[0] * CELL);
            button.setY(y + GRID_TOP + cell[1] * CELL);
        }

        if (ejectButton != null) {
            ejectButton.setX(x + EJECT_BUTTON_LEFT);
            ejectButton.setY(y + EJECT_BUTTON_TOP);
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    /**
     * @return true if the given mouse position is within the popup's visible content box -
     * used by the screen to close the popup on an outside click.
     */
    public boolean isInsideContent(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + CONTENT_WIDTH && mouseY >= y && mouseY < y + CONTENT_HEIGHT;
    }

    /**
     * @return the 6 face buttons plus the eject toggle (if the machine supports it) - add each
     * of these via {@code Screen#addWidget} so they receive input without being drawn until the
     * panel background has been rendered.
     */
    public Collection<Button> getButtons() {
        if (ejectButton == null) {
            return buttons.values();
        }
        List<Button> all = new ArrayList<>(buttons.values());
        all.add(ejectButton);
        return all;
    }

    public void setVisible(boolean visible) {
        buttons.values().forEach(button -> button.visible = visible);
        if (ejectButton != null) {
            ejectButton.visible = visible;
        }
    }

    /**
     * Refreshes the eject button's label to reflect the machine's current state - call this
     * once per frame while the panel is expanded, same as {@link #updateTooltips()}.
     */
    public void updateEjectButton() {
        if (ejectButton != null) {
            ejectButton.setMessage(ejectLabel(machine.isEjectEnabled()));
        }
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
            RelativeSide displaySide = switch (side) {
                case LEFT -> RelativeSide.RIGHT;
                case RIGHT -> RelativeSide.LEFT;
                default -> side;
            };
            Component tooltip = displaySide.getDisplayName().copy().append("\n").append(type.getDisplayName());
            entry.getValue().setTooltip(Tooltip.create(tooltip));
        }
    }

    public void renderBackground(GuiGraphics guiGraphics) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 300);
        guiGraphics.fill(x, y, x + CONTENT_WIDTH, y + CONTENT_HEIGHT - 6, 0xFFD0D0D0);
        guiGraphics.blit(BACKGROUND, x, y, 0, 0, CONTENT_WIDTH, CONTENT_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        guiGraphics.pose().popPose();
    }

    /**
     * Renders every panel button on top of the background - call this from the screen's
     * {@code render()} method after {@link #renderBackground}.
     */
    public void renderButtons(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 300);
        for (Button button : getButtons()) {
            button.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        guiGraphics.pose().popPose();
    }

    /**
     * Draws the small {@link IOType} icon over each button - call this after the buttons
     * themselves have rendered.
     */
    public void renderIcons(GuiGraphics guiGraphics) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 300);

        for (Map.Entry<RelativeSide, Button> entry : buttons.entrySet()) {
            Button button = entry.getValue();
            IOType type = machine.getSideConfiguration().get(entry.getKey());
            int iconU = type.getIconIndex() * ICON_SIZE;
            int iconX = button.getX() + (button.getWidth() - ICON_SIZE) / 2;
            int iconY = button.getY() + (button.getHeight() - ICON_SIZE) / 2;

            guiGraphics.blit(ICONS, iconX, iconY, iconU, 0, ICON_SIZE, ICON_SIZE, ICON_SHEET_WIDTH, ICON_SIZE);
        }

        guiGraphics.pose().popPose();
    }
}
