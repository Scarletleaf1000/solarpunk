package me.scarletleaf1000.sunworks.screen.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.screen.widget.ConfigurationPanelWidget;
import me.scarletleaf1000.sunworks.screen.widget.ConfigurationTabButton;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

public class SolarAlloySmelterScreen extends AbstractContainerScreen<SolarAlloySmelterMenu> {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Sunworks.MOD_ID, "textures/gui/container/solar_alloy_smelter.png");
    private static final ResourceLocation ARROW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Sunworks.MOD_ID, "textures/gui/container/alloy_progress.png");

    private ConfigurationTabButton configTab;
    private ConfigurationPanelWidget configPanel;

    public SolarAlloySmelterScreen(SolarAlloySmelterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        int tabX = x - ConfigurationTabButton.WIDTH;
        int tabY = y + 4;

        configPanel = new ConfigurationPanelWidget(width, height, menu.blockEntity.getBlockPos(), menu.blockEntity);
        configTab = new ConfigurationTabButton(tabX, tabY, () -> configPanel.setVisible(configTab.isExpanded()));

        addRenderableWidget(configTab);
        for (var button : configPanel.getButtons()) {
            addWidget(button);
        }
        configPanel.setVisible(false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (configTab.isExpanded()) {
            if (configTab.isMouseOver(mouseX, mouseY)) {
                return configTab.mouseClicked(mouseX, mouseY, button);
            }
            if (configPanel.isInsideContent(mouseX, mouseY)) {
                for (var panelButton : configPanel.getButtons()) {
                    if (panelButton.isMouseOver(mouseX, mouseY)
                            && panelButton.mouseClicked(mouseX, mouseY, button)) {
                        setDragging(true);
                        setFocused(panelButton);
                        return true;
                    }
                }
                return true;
            }
            configTab.setExpanded(false);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (configTab.isExpanded() && configPanel.isInsideContent(mouseX, mouseY)) {
            return;
        }
        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (configTab.isExpanded() && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            configTab.setExpanded(false);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        renderProgressArrow(guiGraphics, x, y);
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if(menu.isCrafting()) {
            guiGraphics.blit(ARROW_TEXTURE, x + 67, y + 42, 0, 0, 8, menu.getScaledArrowProgress(), 8, 18);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics, mouseX, mouseY, delta);
        if (configTab.isExpanded()) {
            configPanel.updateTooltips();
            configPanel.updateEjectButton();
        }
        super.render(guiGraphics, mouseX, mouseY, delta);
        if (configTab.isExpanded()) {
            configPanel.renderBackground(guiGraphics);
            configPanel.renderButtons(guiGraphics, mouseX, mouseY, delta);
            configPanel.renderIcons(guiGraphics);
        }
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
