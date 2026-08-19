package me.scarletleaf1000.sunworks.screen.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.screen.renderer.EnergyDisplayTooltipArea;
import me.scarletleaf1000.sunworks.screen.widget.ConfigurationPanelWidget;
import me.scarletleaf1000.sunworks.screen.widget.ConfigurationTabButton;
import me.scarletleaf1000.sunworks.util.MouseUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

public class SolarPanelScreen extends AbstractContainerScreen<SolarPanelMenu> {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Sunworks.MOD_ID,"textures/gui/container/solar_panel.png");

    private EnergyDisplayTooltipArea energyInfoArea;
    private ConfigurationTabButton configTab;
    private ConfigurationPanelWidget configPanel;

    public SolarPanelScreen(SolarPanelMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();

        assignEnergyInfoArea();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        int tabX = x - ConfigurationTabButton.WIDTH;
        int tabY = y + 4;

        configPanel = new ConfigurationPanelWidget(width, height, menu.blockEntity.getBlockPos(), menu.blockEntity);
        configTab = new ConfigurationTabButton(tabX, tabY, () -> configPanel.setVisible(configTab.isExpanded()));

        addRenderableWidget(configTab);
        for (var button : configPanel.getButtons()) {
            addRenderableWidget(button);
        }
        configPanel.setVisible(false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (configTab.isExpanded() && !configPanel.isInsideContent(mouseX, mouseY)
                && !configTab.isMouseOver(mouseX, mouseY)) {
            configTab.setExpanded(false);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (configTab.isExpanded() && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            configTab.setExpanded(false);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void renderEnergyAreaTooltip(GuiGraphics guiGraphics, int pMouseX, int pMouseY, int x, int y) {
        if(isMouseAboveArea(pMouseX, pMouseY, x, y, 83, 11, 8, 64)) {
            guiGraphics.renderTooltip(this.font, energyInfoArea.getTooltips(),
                    Optional.empty(), pMouseX - x, pMouseY - y);
        }
    }

    private void assignEnergyInfoArea() {
        energyInfoArea = new EnergyDisplayTooltipArea(((width - imageWidth) / 2) + 83,
                ((height - imageHeight) / 2) + 11, menu.blockEntity.getEnergyStorage(null));
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int pMouseX, int pMouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        renderEnergyAreaTooltip(guiGraphics, pMouseX, pMouseY, x, y);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        energyInfoArea.render(guiGraphics);

        if (configTab.isExpanded()) {
            configPanel.renderBackground(guiGraphics);
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
            configPanel.renderIcons(guiGraphics);
        }
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    public static boolean isMouseAboveArea(int mouseX, int mouseY, int x, int y, int offsetX, int offsetY, int width, int height) {
        return MouseUtil.isMouseOver(mouseX, mouseY, x + offsetX, y + offsetY, width, height);
    }
}
