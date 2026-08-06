package com.github.tionard.ultimateglass.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import com.github.tionard.ultimateglass.item.GlaziersToolTier;
import com.github.tionard.ultimateglass.placement.ShiftPlacementMode;

public final class UltimateGlassConfigScreen extends Screen {
    private final Screen parent;

    public UltimateGlassConfigScreen(Screen parent) {
        super(Component.translatable("config.ultimateglass.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = 260;
        int left = this.width / 2 - buttonWidth / 2;
        int top = this.height / 2 - 54;

        this.addRenderableWidget(
                Button.builder(shiftModeButtonText(), button -> {
                    UltimateGlassClientConfig.toggleShiftPlacementMode();
                    UltimateGlassClient.syncShiftPlacementMode();
                    button.setMessage(shiftModeButtonText());
                })
                        .pos(left, top)
                        .size(buttonWidth, 20)
                        .build()
        );

        addCraftingButton(left, top + 24, buttonWidth, GlaziersToolTier.COPPER);
        addCraftingButton(left, top + 48, buttonWidth, GlaziersToolTier.IRON);
        addCraftingButton(left, top + 72, buttonWidth, GlaziersToolTier.DIAMOND);

        this.addRenderableWidget(
                Button.builder(CommonComponents.GUI_DONE, button -> returnToParent())
                        .pos(left, this.height - 28)
                        .size(buttonWidth, 20)
                        .build()
        );
    }

    @Override
    public void onClose() {
        returnToParent();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(this.font, this.title, this.width / 2, 20, 0xFFFFFFFF);
    }

    private void addCraftingButton(int left, int top, int width, GlaziersToolTier tier) {
        this.addRenderableWidget(
                Button.builder(craftingButtonText(tier), button -> {
                    UltimateGlassClient.requestCraftingToggle(tier);
                    button.setMessage(craftingButtonText(tier));
                })
                        .pos(left, top)
                        .size(width, 20)
                        .build()
        );
    }

    private Component shiftModeButtonText() {
        return Component.translatable(
                UltimateGlassClientConfig.shiftPlacementMode() == ShiftPlacementMode.FACE
                        ? "config.ultimateglass.shift_mode_face"
                        : "config.ultimateglass.shift_mode_near"
        );
    }

    private Component craftingButtonText(GlaziersToolTier tier) {
        String tierName = tier.name().toLowerCase();
        String state = UltimateGlassClientConfig.isCraftingEnabled(tier) ? "enabled" : "disabled";
        return Component.translatable("config.ultimateglass.crafting_" + tierName + "_" + state);
    }

    private void returnToParent() {
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(parent);
        }
    }
}
