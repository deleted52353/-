package dev.bedrockfix.mixin;

import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.exception.CancelException;
import com.viaversion.viaversion.protocol.packet.PacketWrapperImpl;
import dev.bedrockfix.replay.EarlyPacketQueue;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.raphimc.viabedrock.protocol.BedrockProtocol", remap = false)
public abstract class BedrockProtocolMixin {

    @Inject(
            method = "transform(Lcom/viaversion/viaversion/api/protocol/packet/Direction;Lcom/viaversion/viaversion/api/protocol/packet/State;Lcom/viaversion/viaversion/api/protocol/packet/PacketWrapper;)V",
            at = @At("HEAD"),
            remap = false
    )
    private void bedrockfix$queueEarlyPackets(Direction direction, State state, PacketWrapper wrapper, CallbackInfo ci) throws CancelException {
        if (direction != Direction.CLIENTBOUND) return;
        if (wrapper.user().getProtocolInfo().getServerState() != State.CONFIGURATION) return;
        ClientboundBedrockPackets type = ClientboundBedrockPackets.getPacket(wrapper.getId());
        if (type == null || !EarlyPacketQueue.REPLAY.contains(type)) return;
        if (!(wrapper instanceof PacketWrapperImpl impl)) return;

        EarlyPacketQueue.enqueue(wrapper.user(), wrapper.getId(), type, impl.getInputBuffer());
        throw CancelException.generate();
    }
}
