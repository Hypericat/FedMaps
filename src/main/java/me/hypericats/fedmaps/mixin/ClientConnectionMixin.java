package me.hypericats.fedmaps.mixin;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import me.hypericats.fedmaps.feature.Esp;
import me.hypericats.fedmaps.feature.FeatureHandler;
import net.fabricmc.fabric.mixin.attachment.CustomPayloadC2SPacketAccessor;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.BrandCustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    @Inject(at = @At("HEAD"), method = "send(Lnet/minecraft/network/packet/Packet;Lio/netty/channel/ChannelFutureListener;Z)V", cancellable = true)
    private void onSendPacket(Packet<?> packet, @Nullable ChannelFutureListener channelFutureListener, boolean flush, CallbackInfo ci) {

    }

    @Inject(at = @At("HEAD"), method = "sendImmediately", cancellable = true)
    private void onSendImmediately(Packet<?> packet, @Nullable ChannelFutureListener channelFutureListener, boolean flush, CallbackInfo ci) {

    }

    @Inject(at = @At("HEAD"), method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", cancellable = true)
    private void onReceivePacket(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        FeatureHandler.getByClass(Esp.class).onReceivePacket(packet);
    }



//    @Unique
//    private void checkModList(Packet<?> packet, CallbackInfo ci) {
//        if (!(packet instanceof CustomPayloadC2SPacket payload)) return;
//        System.out.println(payload.payload().getId().id().getNamespace());
//        //if ( instanceof BrandCustomPayload)
//    }
}
