package dev.bedrockfix.mixin;

import com.viaversion.viaversion.api.connection.UserConnection;
import dev.bedrockfix.replay.EarlyPacketQueue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.raphimc.viabedrock.protocol.packet.JoinPackets", remap = false)
public abstract class JoinPacketsMixin {

    @Inject(
            method = "handleJavaClientGameJoin(Lcom/viaversion/viaversion/api/connection/UserConnection;)V",
            at = @At("TAIL"),
            remap = false
    )
    private static void bedrockfix$replayEarlyPackets(UserConnection user, CallbackInfo ci) {
        EarlyPacketQueue.replay(user);
    }
}
