package me.scarletleaf1000.sunworks.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import me.scarletleaf1000.sunworks.Sunworks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * The always-present click target in the top-left corner of a machine's GUI that expands/
 * collapses the {@link ConfigurationPanelWidget}. Renders the single-frame {@code
 * configuration_tab.png} (32x26) while collapsed; renders nothing while expanded, since the
 * panel's own background texture visually takes over that same corner.
 */
public class ConfigurationTabButton extends AbstractWidget {
    public static final int WIDTH = 32;
    public static final int HEIGHT = 26;

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Sunworks.MOD_ID, "textures/gui/container/configuration_tab.png");

    private final Runnable onToggle;
    private boolean expanded;

    public ConfigurationTabButton(int x, int y, Runnable onToggle) {
        super(x, y, WIDTH, HEIGHT, Component.translatable("gui.sunworks.configuration_tab"));
        this.onToggle = onToggle;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        if (this.expanded == expanded) {
            return;
        }
        this.expanded = expanded;
        onToggle.run();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        setExpanded(!expanded);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (expanded) {
            return;
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(TEXTURE, getX(), getY(), 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}
