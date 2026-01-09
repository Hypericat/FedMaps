package me.hypericats.fedmaps.mixin;

import me.hypericats.fedmaps.map.DungeonScan;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class WorldMixin {
    @Inject(at = @At("HEAD"), method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z")
    private void onLoadChunk(BlockPos pos, BlockState state, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() == Blocks.TRAPPED_CHEST && MinecraftClient.getInstance().player != null) {
            if (DungeonScan.getMimicPos() == null || !DungeonScan.getMimicPos().equals(pos))
                throw new RuntimeException("Found non found chest!");
            MinecraftClient.getInstance().player.sendMessage(Text.of("Trapped Chest : " + pos), false);
        }
    }
}
