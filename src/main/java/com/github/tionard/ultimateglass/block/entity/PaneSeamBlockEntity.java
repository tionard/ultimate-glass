package com.github.tionard.ultimateglass.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.github.tionard.ultimateglass.registry.UltimateGlassBlockEntities;
import com.github.tionard.ultimateglass.seam.PaneSeamData;
import com.github.tionard.ultimateglass.seam.PaneSeamSource;

/** Non-ticking storage for independent manual seam choices on ordinary panes. */
public final class PaneSeamBlockEntity extends BlockEntity implements PaneSeamSource {
    private final PaneSeamData seamData = new PaneSeamData();

    public PaneSeamBlockEntity(BlockPos pos, BlockState state) {
        super(UltimateGlassBlockEntities.PANE_SEAMS, pos, state);
    }

    @Override
    public PaneSeamData seamData() {
        return seamData;
    }

    @Override
    public void markSeamsChanged() {
        setChanged();
        requestRemesh();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        long previousVisible = seamData.visibleMask();
        long previousSeamless = seamData.seamlessMask();
        seamData.load(input);
        if (previousVisible != seamData.visibleMask()
                || previousSeamless != seamData.seamlessMask()) {
            if (level == null || !level.isClientSide()) {
                return;
            }
            requestRemesh();
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        seamData.save(output);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    private void requestRemesh() {
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        }
    }
}
