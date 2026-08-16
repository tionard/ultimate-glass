package com.github.tionard.ultimateglass.block.entity;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.github.tionard.ultimateglass.pane.PaneAppearance;
import com.github.tionard.ultimateglass.pane.PaneFrame;
import com.github.tionard.ultimateglass.pane.PaneMaterial;
import com.github.tionard.ultimateglass.registry.UltimateGlassBlockEntities;
import com.github.tionard.ultimateglass.registry.UltimateGlassComponents;
import com.github.tionard.ultimateglass.registry.UltimateGlassItems;

/** Data-only backing state for a pane installed in the open volume of a stair or slab. */
public final class CompositePaneBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity
        implements PaneFrameSource {
    private static final String HOST_STATE_KEY = "host_state";
    private static final String MATERIAL_KEY = "pane_material";
    private static final String FRAME_KEY = "pane_frame";
    private static final String AXIS_KEY = "pane_axis";
    private static final String FRAME_BLOCK_KEY = "frame_block";

    private static final Codec<PaneMaterial> MATERIAL_CODEC = Codec.STRING.xmap(
            PaneMaterial::valueOf,
            PaneMaterial::name
    );
    private static final Codec<PaneFrame> FRAME_CODEC = Codec.STRING.xmap(
            PaneFrame::valueOf,
            PaneFrame::name
    );
    private static final Codec<Direction.Axis> AXIS_CODEC = Codec.STRING.xmap(
            name -> Direction.Axis.valueOf(name.toUpperCase(java.util.Locale.ROOT)),
            axis -> axis.getName().toUpperCase(java.util.Locale.ROOT)
    );

    private BlockState hostState = Blocks.AIR.defaultBlockState();
    private PaneAppearance appearance = new PaneAppearance(PaneMaterial.CLEAR);
    private Direction.Axis paneAxis = Direction.Axis.Z;
    private Identifier frameBlockId = DynamicFrameBlockEntity.DEFAULT_FRAME;

    public CompositePaneBlockEntity(BlockPos pos, BlockState state) {
        super(UltimateGlassBlockEntities.COMPOSITE_PANE, pos, state);
    }

    public BlockState hostState() {
        return hostState;
    }

    public PaneAppearance appearance() {
        return appearance;
    }

    public Direction.Axis paneAxis() {
        return paneAxis;
    }

    @Override
    public Identifier frameBlockId() {
        return frameBlockId;
    }

    public void setComposite(
            BlockState hostState,
            PaneAppearance appearance,
            Direction.Axis paneAxis,
            Identifier frameBlockId
    ) {
        if (paneAxis == Direction.Axis.Y) {
            throw new IllegalArgumentException("Composite stair/slab panes must be vertical");
        }
        this.hostState = hostState;
        this.appearance = appearance;
        this.paneAxis = paneAxis;
        this.frameBlockId = frameBlockId == null
                ? DynamicFrameBlockEntity.DEFAULT_FRAME
                : frameBlockId;
        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        }
    }

    public BlockState restoredHostState() {
        if (hostState.hasProperty(BlockStateProperties.WATERLOGGED)
                && getBlockState().hasProperty(BlockStateProperties.WATERLOGGED)) {
            return hostState.setValue(
                    BlockStateProperties.WATERLOGGED,
                    getBlockState().getValue(BlockStateProperties.WATERLOGGED)
            );
        }
        return hostState;
    }

    public ItemStack paneStack() {
        ItemStack stack = new ItemStack(UltimateGlassItems.paneItemFor(
                com.github.tionard.ultimateglass.registry.UltimateGlassBlocks.familyFor(appearance)
        ));
        if (appearance.frame().isDynamic()) {
            stack.set(UltimateGlassComponents.FRAME_BLOCK, frameBlockId);
        }
        return stack;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        hostState = input.read(HOST_STATE_KEY, BlockState.CODEC)
                .orElse(Blocks.AIR.defaultBlockState());
        PaneMaterial material = input.read(MATERIAL_KEY, MATERIAL_CODEC)
                .orElse(PaneMaterial.CLEAR);
        PaneFrame frame = input.read(FRAME_KEY, FRAME_CODEC).orElse(PaneFrame.NONE);
        appearance = new PaneAppearance(material, frame);
        paneAxis = input.read(AXIS_KEY, AXIS_CODEC).orElse(Direction.Axis.Z);
        if (paneAxis == Direction.Axis.Y) {
            paneAxis = Direction.Axis.Z;
        }
        frameBlockId = input.read(FRAME_BLOCK_KEY, Identifier.CODEC)
                .orElse(DynamicFrameBlockEntity.DEFAULT_FRAME);
        requestClientRemesh();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store(HOST_STATE_KEY, BlockState.CODEC, hostState);
        output.store(MATERIAL_KEY, MATERIAL_CODEC, appearance.material());
        output.store(FRAME_KEY, FRAME_CODEC, appearance.frame());
        output.store(AXIS_KEY, AXIS_CODEC, paneAxis);
        output.store(FRAME_BLOCK_KEY, Identifier.CODEC, frameBlockId);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    private void requestClientRemesh() {
        if (level != null && level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        }
    }
}
