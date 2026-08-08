package com.github.tionard.ultimateglass.item;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import com.github.tionard.ultimateglass.block.CenteredPaneBlock;
import com.github.tionard.ultimateglass.block.EdgePaneBlock;
import com.github.tionard.ultimateglass.registry.UltimateGlassBlocks;
import com.github.tionard.ultimateglass.registry.UltimateGlassBlocks.PaneFamily;
import com.github.tionard.ultimateglass.registry.UltimateGlassItems;
import com.github.tionard.ultimateglass.rotation.RotationAxisState;

public final class GlaziersToolItem extends Item {
    private static final float DIAMOND_GLASS_MINING_SPEED = 6.0F;

    private final GlaziersToolTier tier;

    public GlaziersToolItem(Properties properties, GlaziersToolTier tier) {
        super(properties);
        this.tier = tier;
    }

    public GlaziersToolTier tier() {
        return tier;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || player.isSpectator() || !player.getAbilities().mayBuild) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockState state = level.getBlockState(context.getClickedPos());
        Block block = state.getBlock();

        if (player.isShiftKeyDown() && tier.canTogglePanePosition()) {
            InteractionResult toggleResult = togglePanePosition(context, state, block);
            if (toggleResult != InteractionResult.PASS) {
                return toggleResult;
            }
        }

        if (block instanceof EdgePaneBlock) {
            if (!level.isClientSide()) {
                Direction rotated = EdgePaneBlock.rotateAround(
                        state.getValue(EdgePaneBlock.FACING),
                        RotationAxisState.get(player)
                );
                level.setBlockAndUpdate(
                        context.getClickedPos(),
                        state.setValue(EdgePaneBlock.FACING, rotated)
                );
                EdgePaneBlock.refreshConnectionsAround(level, context.getClickedPos());
            }
            return InteractionResult.SUCCESS;
        }

        if (block instanceof CenteredPaneBlock) {
            if (!level.isClientSide()) {
                Direction.Axis rotated = CenteredPaneBlock.rotateAround(
                        state.getValue(CenteredPaneBlock.AXIS),
                        RotationAxisState.get(player)
                );
                level.setBlockAndUpdate(
                        context.getClickedPos(),
                        state.setValue(CenteredPaneBlock.AXIS, rotated)
                );
                EdgePaneBlock.refreshConnectionsAround(level, context.getClickedPos());
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (!tier.silkTouchesGlass() || collectedStack(state.getBlock()).isEmpty()) {
            return 1.0F;
        }
        return DIAMOND_GLASS_MINING_SPEED;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return tier.silkTouchesGlass() && !collectedStack(state.getBlock()).isEmpty();
    }

    private static InteractionResult togglePanePosition(
            UseOnContext context,
            BlockState state,
            Block block
    ) {
        Level level = context.getLevel();
        PaneFamily family = UltimateGlassBlocks.familyFor(block);
        if (family == null || block == family.vanillaPane()) {
            return InteractionResult.PASS;
        }

        boolean waterlogged = state.getValue(BlockStateProperties.WATERLOGGED);

        if (block == family.edgePane()) {
            Direction facing = state.getValue(EdgePaneBlock.FACING);
            if (!level.isClientSide()) {
                BlockState target = family.centeredPane().defaultBlockState()
                        .setValue(CenteredPaneBlock.AXIS, facing.getAxis())
                        .setValue(CenteredPaneBlock.WATERLOGGED, waterlogged);
                level.setBlockAndUpdate(context.getClickedPos(), target);
                EdgePaneBlock.refreshConnectionsAround(level, context.getClickedPos());
            }
            return InteractionResult.SUCCESS;
        }

        if (block == family.centeredPane()) {
            if (!level.isClientSide()) {
                Direction.Axis axis = state.getValue(CenteredPaneBlock.AXIS);
                level.setBlockAndUpdate(
                        context.getClickedPos(),
                        family.edgePane().defaultBlockState()
                                .setValue(EdgePaneBlock.FACING, edgeFacing(context, axis))
                                .setValue(EdgePaneBlock.WATERLOGGED, waterlogged)
                );
                EdgePaneBlock.refreshConnectionsAround(level, context.getClickedPos());
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    public static ItemStack collectedStack(Block block) {
        PaneFamily family = UltimateGlassBlocks.familyFor(block);
        if (family != null) {
            Item customPaneItem = UltimateGlassItems.paneItemFor(block);
            if (customPaneItem != null) {
                return new ItemStack(customPaneItem);
            }
            return new ItemStack(family.vanillaPane().asItem());
        }

        if (isVanillaGlassBlock(block)) {
            return new ItemStack(block.asItem());
        }

        return ItemStack.EMPTY;
    }

    private static Direction edgeFacing(UseOnContext context, Direction.Axis axis) {
        Direction clickedFace = context.getClickedFace();
        if (clickedFace.getAxis() == axis) {
            return clickedFace;
        }

        Vec3 center = Vec3.atCenterOf(context.getClickedPos());
        Player player = context.getPlayer();
        Vec3 reference = player == null ? context.getClickLocation() : player.getEyePosition();
        double component = switch (axis) {
            case X -> reference.x - center.x;
            case Y -> reference.y - center.y;
            case Z -> reference.z - center.z;
        };

        return switch (axis) {
            case X -> component >= 0.0 ? Direction.EAST : Direction.WEST;
            case Y -> component >= 0.0 ? Direction.UP : Direction.DOWN;
            case Z -> component >= 0.0 ? Direction.SOUTH : Direction.NORTH;
        };
    }

    private static boolean isVanillaGlassBlock(Block block) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        if (!"minecraft".equals(id.getNamespace())) {
            return false;
        }

        String path = id.getPath();
        return "glass".equals(path) || path.endsWith("_stained_glass");
    }
}
