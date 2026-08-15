package com.github.tionard.ultimateglass.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.github.tionard.ultimateglass.registry.UltimateGlassBlockEntities;
import com.github.tionard.ultimateglass.registry.UltimateGlassComponents;

/** Stores only the selected plank block; the pane itself is still rendered in the chunk mesh. */
public final class DynamicFrameBlockEntity extends BlockEntity {
    public static final Identifier DEFAULT_FRAME = Identifier.withDefaultNamespace("oak_planks");
    private static final String FRAME_KEY = "frame_block";

    private Identifier frameBlockId = DEFAULT_FRAME;

    public DynamicFrameBlockEntity(BlockPos pos, BlockState state) {
        super(UltimateGlassBlockEntities.DYNAMIC_FRAME, pos, state);
    }

    public Identifier frameBlockId() {
        return frameBlockId;
    }

    public Block frameBlock() {
        return BuiltInRegistries.BLOCK.getOptional(frameBlockId).orElse(Blocks.OAK_PLANKS);
    }

    public void setFrameBlockId(Identifier frameBlockId) {
        Identifier resolved = frameBlockId == null ? DEFAULT_FRAME : frameBlockId;
        if (resolved.equals(this.frameBlockId)) {
            return;
        }
        this.frameBlockId = resolved;
        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        frameBlockId = input.read(FRAME_KEY, Identifier.CODEC).orElse(DEFAULT_FRAME);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store(FRAME_KEY, Identifier.CODEC, frameBlockId);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        frameBlockId = components.getOrDefault(UltimateGlassComponents.FRAME_BLOCK, DEFAULT_FRAME);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(UltimateGlassComponents.FRAME_BLOCK, frameBlockId);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
