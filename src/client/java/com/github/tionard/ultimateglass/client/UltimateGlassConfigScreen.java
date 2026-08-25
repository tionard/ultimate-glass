package com.github.tionard.ultimateglass.client;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import com.github.tionard.ultimateglass.item.GlaziersToolTier;

public final class UltimateGlassConfigScreen extends Screen {
    private final Screen parent;
    private final Map<GlaziersToolTier, Button> craftingButtons = new EnumMap<>(GlaziersToolTier.class);
    private Button seamlessPanesButton;
    private Button manualSeamToolButton;
    private Button experimentalCompositesButton;
    private Button alwaysDropPanesButton;
    private Button reverseRecipeButton;

    public UltimateGlassConfigScreen(Screen parent) {
        super(Component.translatable("config.ultimateglass.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        craftingButtons.clear();

        int buttonWidth = 260;
        int left = this.width / 2 - buttonWidth / 2;
        int top = Math.max(28, this.height / 2 - 88);
        int step = 22;

        seamlessPanesButton = this.addRenderableWidget(
                Button.builder(seamlessPanesButtonText(), button -> {
                    UltimateGlassClient.toggleSeamlessConnectedPanes();
                    button.setMessage(seamlessPanesButtonText());
                })
                        .pos(left, top)
                        .size(buttonWidth, 20)
                        .build()
        );

        manualSeamToolButton = this.addRenderableWidget(
                Button.builder(manualSeamToolButtonText(), button -> {
                    UltimateGlassClient.requestManualSeamToolToggle();
                    button.setMessage(manualSeamToolButtonText());
                })
                        .pos(left, top + step)
                        .size(buttonWidth, 20)
                        .build()
        );

        experimentalCompositesButton = this.addRenderableWidget(
                Button.builder(experimentalCompositesButtonText(), button -> {
                    UltimateGlassClient.requestExperimentalCompositesToggle();
                    button.setMessage(experimentalCompositesButtonText());
                })
                        .pos(left, top + step * 2)
                        .size(buttonWidth, 20)
                        .build()
        );

        alwaysDropPanesButton = this.addRenderableWidget(
                Button.builder(alwaysDropPanesButtonText(), button -> {
                    UltimateGlassClient.requestTemperedPanesAlwaysDropToggle();
                    button.setMessage(alwaysDropPanesButtonText());
                })
                        .pos(left, top + step * 3)
                        .size(buttonWidth, 20)
                        .build()
        );

        reverseRecipeButton = this.addRenderableWidget(
                Button.builder(reverseRecipeButtonText(), button -> {
                    UltimateGlassClient.requestTemperedToVanillaRecipeToggle();
                    button.setMessage(reverseRecipeButtonText());
                })
                        .pos(left, top + step * 4)
                        .size(buttonWidth, 20)
                        .build()
        );

        addCraftingButton(left, top + step * 5, buttonWidth, GlaziersToolTier.COPPER);
        addCraftingButton(left, top + step * 6, buttonWidth, GlaziersToolTier.IRON);
        addCraftingButton(left, top + step * 7, buttonWidth, GlaziersToolTier.DIAMOND);

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
        if (experimentalCompositesButton != null) {
            experimentalCompositesButton.setMessage(experimentalCompositesButtonText());
        }
        if (seamlessPanesButton != null) {
            seamlessPanesButton.setMessage(seamlessPanesButtonText());
        }
        if (manualSeamToolButton != null) {
            manualSeamToolButton.setMessage(manualSeamToolButtonText());
        }
        if (alwaysDropPanesButton != null) {
            alwaysDropPanesButton.setMessage(alwaysDropPanesButtonText());
        }
        if (reverseRecipeButton != null) {
            reverseRecipeButton.setMessage(reverseRecipeButtonText());
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

    private Component experimentalCompositesButtonText() {
        return Component.translatable(
                UltimateGlassClientConfig.experimentalCompositesEnabled()
                        ? "config.ultimateglass.experimental_composites_enabled"
                        : "config.ultimateglass.experimental_composites_disabled"
        );
    }

    private Component seamlessPanesButtonText() {
        return Component.translatable(
                UltimateGlassClientConfig.seamlessConnectedPanes()
                        ? "config.ultimateglass.seamless_panes_enabled"
                        : "config.ultimateglass.seamless_panes_disabled"
        );
    }

    private Component manualSeamToolButtonText() {
        return Component.translatable(
                UltimateGlassClientConfig.manualSeamToolEnabled()
                        ? "config.ultimateglass.manual_seam_tool_enabled"
                        : "config.ultimateglass.manual_seam_tool_disabled"
        );
    }

    private Component alwaysDropPanesButtonText() {
        return Component.translatable(
                UltimateGlassClientConfig.temperedPanesAlwaysDrop()
                        ? "config.ultimateglass.always_drop_panes_enabled"
                        : "config.ultimateglass.always_drop_panes_disabled"
        );
    }

    private Component reverseRecipeButtonText() {
        return Component.translatable(
                UltimateGlassClientConfig.temperedToVanillaRecipeEnabled()
                        ? "config.ultimateglass.reverse_recipe_enabled"
                        : "config.ultimateglass.reverse_recipe_disabled"
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
