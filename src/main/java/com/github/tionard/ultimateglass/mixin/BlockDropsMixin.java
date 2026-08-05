package com.github.tionard.ultimateglass.mixin;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.github.tionard.ultimateglass.item.GlaziersToolItem;
import com.github.tionard.ultimateglass.registry.UltimateGlassItems;

@Mixin(Block.class)
abstract class BlockDropsMixin {
    @Inject(
            method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void ultimateGlass$dropGlassIntact(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            @Nullable BlockEntity blockEntity,
            @Nullable Entity entity,
            ItemStack tool,
            CallbackInfoReturnable<List<ItemStack>> callback
    ) {
        if (!tool.is(UltimateGlassItems.GLAZIERS_TOOL)) {
            return;
        }

        ItemStack drop = GlaziersToolItem.collectedStack(state.getBlock());
        if (!drop.isEmpty()) {
            callback.setReturnValue(List.of(drop));
        }
    }
}
