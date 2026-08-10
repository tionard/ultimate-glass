package com.github.tionard.ultimateglass.client;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import com.github.tionard.ultimateglass.item.GlaziersToolTier;
import com.github.tionard.ultimateglass.placement.ShiftPlacementMode;

public final class UltimateGlassConfigScreen extends Screen {
    private final Screen parent;
    private final Map<GlaziersToolTier, Button> craftingButtons = new EnumMap<>(GlaziersToolTier.class);
    private Button shiftModeButton;
    private Button seamlessPanesButton;

    public UltimateGlassConfigScreen(Screen parent) {
        super(Component.translatable("config.ultimateglass.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        craftingButtons.clear();

        int buttonWidth = 260;
        int left = this.width / 2 - buttonWidth / 2;
        int top = this.height / 2 - 66;

        seamlessPanesButton = this.addRenderableWidget(
                Button.builder(seamlessPanesButtonText(), button -> {
                    UltimateGlassClient.toggleSeamlessConnectedPanes();
                    button.setMessage(seamlessPanesButtonText());
                })
                        .pos(left, top)
                        .size(buttonWidth, 20)
                        .build()
        );

        shiftModeButton = this.addRenderableWidget(
                Button.builder(shiftModeButtonText(), button -> {
                    UltimateGlassClientConfig.toggleShiftPlacementMode();
                    UltimateGlassClient.syncShiftPlacementMode();
                    button.setMessage(shiftModeButtonText());
                })
                        .pos(left, top + 24)
                        .size(buttonWidth, 20)
                        .build()
        );

        addCraftingButton(left, top + 48, buttonWidth, GlaziersToolTier.COPPER);
        addCraftingButton(left, top + 72, buttonWidth, GlaziersToolTier.IRON);
        addCraftingButton(left, top + 96, buttonWidth, GlaziersToolTier.DIAMOND);

        this.addRenderableWidget(
                Button.builder(CommonComponents.GUI_DONE, button -> returnToParent())
                        .pos(left, this.height - 28)
                        .size(buttonWidth, 20)
                        .build()
        );
    }

    @Override
    public void tick() {
        super.tick();
        if (shiftModeButton != null) {
            shiftModeButton.setMessage(shiftModeButtonText());
        }
        if (seamlessPanesButton != null) {
            seamlessPanesButton.setMessage(seamlessPanesButtonText());
        }
        craftingButtons.forEach((tier, button) -> button.setMessage(craftingButtonText(tier)));
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
        Button button = this.addRenderableWidget(
                Button.builder(craftingButtonText(tier), clicked ->
                        UltimateGlassClient.requestCraftingToggle(tier)
                )
                        .pos(left, top)
                        .size(width, 20)
                        .build()
        );
        craftingButtons.put(tier, button);
    }

    private Component shiftModeButtonText() {
        return Component.translatable(
                UltimateGlassClientConfig.shiftPlacementMode() == ShiftPlacementMode.FACE
                        ? "config.ultimateglass.shift_mode_face"
                        : "config.ultimateglass.shift_mode_near"
        );
    }

    private Component seamlessPanesButtonText() {
        return Component.translatable(
                UltimateGlassClientConfig.seamlessConnectedPanes()
                        ? "config.ultimateglass.seamless_panes_enabled"
                        : "config.ultimateglass.seamless_panes_disabled"
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
