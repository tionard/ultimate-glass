package com.github.tionard.ultimateglass.network;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.Permissions;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import com.github.tionard.ultimateglass.config.UltimateGlassServerConfig;
import com.github.tionard.ultimateglass.glass.GlassFamilyBlock;
import com.github.tionard.ultimateglass.glass.GlassForm;
import com.github.tionard.ultimateglass.rotation.RotationAxisState;
import com.github.tionard.ultimateglass.block.CompositePaneBlock;
import com.github.tionard.ultimateglass.block.entity.CompositePaneBlockEntity;
import com.github.tionard.ultimateglass.pane.PanePlane;
import com.github.tionard.ultimateglass.pane.UltimatePane;
import com.github.tionard.ultimateglass.registry.UltimateGlassItems;
import com.github.tionard.ultimateglass.seam.PaneSeamOverride;
import com.github.tionard.ultimateglass.seam.PaneSeamCounterpart;
import com.github.tionard.ultimateglass.seam.PaneSeamSource;
import com.github.tionard.ultimateglass.seam.PaneSeamTarget;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class UltimateGlassNetworking {
    private UltimateGlassNetworking() {
    }

    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(RotationAxisPayload.TYPE, RotationAxisPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ToolCraftingConfigPayload.TYPE, ToolCraftingConfigPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PaneSeamEditPayload.TYPE, PaneSeamEditPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ToolCraftingConfigPayload.TYPE, ToolCraftingConfigPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(
                RotationAxisPayload.TYPE,
                (payload, context) -> RotationAxisState.set(
                        context.player(),
                        RotationAxisState.fromOrdinal(payload.axisOrdinal())
                )
        );

        ServerPlayNetworking.registerGlobalReceiver(
                ToolCraftingConfigPayload.TYPE,
                (payload, context) -> {
                    MinecraftServer server = context.player().level().getServer();
                    boolean canEdit = context.player().permissions()
                            .hasPermission(Permissions.COMMANDS_GAMEMASTER)
                            || server.isSingleplayerOwner(context.player().nameAndId());
                    if (!canEdit) {
                        ServerPlayNetworking.send(context.player(), ToolCraftingConfigPayload.current());
                        return;
                    }

                    UltimateGlassServerConfig.apply(
                            payload.copperEnabled(),
                            payload.ironEnabled(),
                            payload.diamondEnabled(),
                            payload.experimentalCompositesEnabled(),
                            payload.temperedPanesAlwaysDrop(),
                            payload.temperedToVanillaRecipeEnabled(),
                            payload.glassChiselEnabled(),
                            true
                    );
                    ToolCraftingConfigPayload current = ToolCraftingConfigPayload.current();
                    server.getPlayerList().getPlayers().forEach(
                            player -> ServerPlayNetworking.send(player, current)
                    );
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(
                PaneSeamEditPayload.TYPE,
                (payload, context) -> applyPaneSeamEdit(payload, context.player())
        );

        ServerPlayConnectionEvents.JOIN.register((listener, sender, server) ->
                ServerPlayNetworking.send(listener.player, ToolCraftingConfigPayload.current())
        );
    }

    private static void applyPaneSeamEdit(
            PaneSeamEditPayload payload,
            net.minecraft.server.level.ServerPlayer player
    ) {
        if (player.isSpectator()
                || !player.getAbilities().mayBuild
                || !UltimateGlassServerConfig.glassChiselEnabled()
                || (!player.getMainHandItem().is(UltimateGlassItems.GLASS_CHISEL)
                        && !player.getOffhandItem().is(UltimateGlassItems.GLASS_CHISEL))
                || player.distanceToSqr(Vec3.atCenterOf(payload.pos())) > 64.0D) {
            return;
        }

        PanePlane[] planes = PanePlane.values();
        Direction[] directions = Direction.values();
        PaneSeamOverride[] overrides = PaneSeamOverride.values();
        if (payload.planeOrdinal() < 0 || payload.planeOrdinal() >= planes.length
                || payload.boundaryOrdinal() < 0
                || payload.boundaryOrdinal() >= directions.length
                || payload.overrideOrdinal() < 0
                || payload.overrideOrdinal() >= overrides.length) {
            return;
        }

        PanePlane plane = planes[payload.planeOrdinal()];
        Direction boundary = directions[payload.boundaryOrdinal()];
        PaneSeamOverride override = overrides[payload.overrideOrdinal()];
        if (boundary.getAxis() == plane.axis()) {
            return;
        }

        Level level = player.level();
        if (!supportsPlane(level, payload.pos(), plane)
                || !(level.getBlockEntity(payload.pos()) instanceof PaneSeamSource seams)) {
            return;
        }

        if (payload.resetAll()) {
            seams.resetSeamOverrides();
            return;
        }

        setSeamOverride(level, payload.pos(), seams, plane, boundary, override);
        if (!payload.singleEdge()) {
            PaneSeamCounterpart counterpart = PaneSeamCounterpart.of(
                    payload.pos(), new PaneSeamTarget(plane, boundary)
            );
            if (supportsPlane(level, counterpart.pos(), plane)
                    && level.getBlockEntity(counterpart.pos())
                            instanceof PaneSeamSource neighborSeams) {
                setSeamOverride(
                        level,
                        counterpart.pos(),
                        neighborSeams,
                        counterpart.target().plane(),
                        counterpart.target().boundary(),
                        override
                );
            }
        }
    }

    private static void setSeamOverride(
            Level level,
            net.minecraft.core.BlockPos pos,
            PaneSeamSource seams,
            PanePlane plane,
            Direction boundary,
            PaneSeamOverride override
    ) {
        seams.setSeamOverride(plane, boundary, override);
        if (isFramedFullBlock(level.getBlockState(pos)) && plane.isEdge()) {
            // A cube edge is visible on two adjoining faces. Keep both surface halves together.
            seams.setSeamOverride(
                    PanePlane.edge(boundary),
                    plane.edgeDirection(),
                    override
            );
        }
    }

    private static boolean supportsPlane(
            Level level,
            net.minecraft.core.BlockPos pos,
            PanePlane requestedPlane
    ) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof UltimatePane pane) {
            return pane.geometry(state).planes().contains(requestedPlane);
        }
        if (state.getBlock() instanceof CompositePaneBlock
                && level.getBlockEntity(pos) instanceof CompositePaneBlockEntity composite) {
            return composite.paneGeometry().planes().contains(requestedPlane);
        }
        return requestedPlane.isEdge() && isFramedFullBlock(state);
    }

    private static boolean isFramedFullBlock(BlockState state) {
        return state.getBlock() instanceof GlassFamilyBlock glass
                && glass.glassVariant().form() == GlassForm.BLOCK
                && glass.glassVariant().isFramed();
    }
}
