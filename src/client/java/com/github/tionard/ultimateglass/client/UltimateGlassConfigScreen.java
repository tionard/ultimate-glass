package com.github.tionard.ultimateglass.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class UltimateGlassConfigScreen extends Screen {
    private final Screen parent;

    public UltimateGlassConfigScreen(Screen parent) {
        super(Component.translatable("config.ultimateglass.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = 220;
        int left = this.width / 2 - buttonWidth / 2;

        this.addRenderableWidget(
                Button.builder(toolButtonText(), button -> {
                    UltimateGlassClientConfig.toggleToolEnabled();
                    button.setMessage(toolButtonText());
                })
                .pos(left, this.height / 2 - 20)
                .size(buttonWidth, 20)
                .build()
        );

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

    private Component toolButtonText() {
        return Component.translatable(
                UltimateGlassClientConfig.isToolEnabled()
                        ? "config.ultimateglass.tool_enabled"
                        : "config.ultimateglass.tool_disabled"
        );
    }

    private void returnToParent() {
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(parent);
        }
    }
}
