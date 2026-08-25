package com.github.tionard.ultimateglass.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.github.tionard.ultimateglass.block.CompositePaneBlock;
import com.github.tionard.ultimateglass.block.entity.CompositePaneBlockEntity;
import com.github.tionard.ultimateglass.config.UltimateGlassServerConfig;
import com.github.tionard.ultimateglass.pane.PaneGeometry;
import com.github.tionard.ultimateglass.pane.UltimatePane;
import com.github.tionard.ultimateglass.seam.PaneSeamOverride;
import com.github.tionard.ultimateglass.seam.PaneSeamSource;
import com.github.tionard.ultimateglass.seam.PaneSeamTarget;
import com.github.tionard.ultimateglass.seam.PaneSeamTargetResolver;

/** Toggles player-selected pane boundaries or clears the clicked pane's manual choices. */
public final class GlaziersScriberItem extends Item {
    private static ClientUseHandler clientUseHandler = (context, state, target, seams) -> {
    };

    public GlaziersScriberItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || player.isSpectator() || !player.getAbilities().mayBuild) {
            return InteractionResult.PASS;
        }
        if (!UltimateGlassServerConfig.manualSeamToolEnabled()) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockState state = level.getBlockState(context.getClickedPos());
        PaneGeometry geometry = geometry(level, context, state);
        if (geometry == null
                || !(level.getBlockEntity(context.getClickedPos()) instanceof PaneSeamSource seams)) {
            return InteractionResult.PASS;
        }

        PaneSeamTarget target = PaneSeamTargetResolver.resolve(
                geometry,
                context.getClickedPos(),
                context.getClickLocation(),
                context.getClickedFace()
        );
        if (level.isClientSide()) {
            clientUseHandler.handle(context, state, target, seams);
        }
        return InteractionResult.SUCCESS;
    }

    public static void setClientUseHandler(ClientUseHandler handler) {
        clientUseHandler = handler == null ? (context, state, target, seams) -> {
        } : handler;
    }

    private static PaneGeometry geometry(
            Level level,
            UseOnContext context,
            BlockState state
    ) {
        if (state.getBlock() instanceof UltimatePane pane) {
            return pane.geometry(state);
        }
        if (state.getBlock() instanceof CompositePaneBlock
                && level.getBlockEntity(context.getClickedPos())
                        instanceof CompositePaneBlockEntity composite) {
            return composite.paneGeometry();
        }
        return null;
    }

    public static String messageKey(PaneSeamOverride override) {
        return switch (override) {
            case AUTOMATIC -> "message.ultimateglass.seam_automatic";
            case VISIBLE -> "message.ultimateglass.seam_visible";
            case SEAMLESS -> "message.ultimateglass.seam_seamless";
        };
    }

    @FunctionalInterface
    public interface ClientUseHandler {
        void handle(
                UseOnContext context,
                BlockState state,
                PaneSeamTarget target,
                PaneSeamSource seams
        );
    }
}
