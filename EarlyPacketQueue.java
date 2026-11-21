package dev.bedrockfix.replay;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.exception.CancelException;
import dev.bedrockfix.BedrockFixClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * ViaBedrock drops packets that arrive before the Java client reaches the PLAY state
 * ("Received packet X outside PLAY state. Ignoring it."). The Bedrock server sends the
 * player's inventory right after StartGame, so it is lost. This class keeps those packets
 * and replays them through the normal ViaBedrock pipeline once the client has joined.
 */
public final class EarlyPacketQueue {
    private static final int MAX_PACKETS = 512;

    /** Only packets that are safe and useful to replay late. */
    public static final Set<ClientboundBedrockPackets> REPLAY = EnumSet.of(
            ClientboundBedrockPackets.INVENTORY_CONTENT,
            ClientboundBedrockPackets.INVENTORY_SLOT,
            ClientboundBedrockPackets.PLAYER_HOTBAR,
            ClientboundBedrockPackets.MOB_EQUIPMENT,
            ClientboundBedrockPackets.CREATIVE_CONTENT,
            ClientboundBedrockPackets.PLAYER_LIST,
            ClientboundBedrockPackets.SET_TIME
    );

    private record Entry(int id, String name, ByteBuf payload) {}

    private static final Map<UserConnection, List<Entry>> QUEUES =
            Collections.synchronizedMap(new WeakHashMap<>());

    private EarlyPacketQueue() {}

    public static void enqueue(UserConnection user, int id, ClientboundBedrockPackets type, ByteBuf input) {
        List<Entry> list = QUEUES.computeIfAbsent(user, u -> new ArrayList<>());
        synchronized (list) {
            if (list.size() >= MAX_PACKETS) return;
            ByteBuf copy = Unpooled.buffer(input.readableBytes());
            copy.writeBytes(input, input.readerIndex(), input.readableBytes());
            list.add(new Entry(id, type.name(), copy));
        }
        BedrockFixClient.LOG.info("Queued early packet {} until PLAY state", type.name());
    }

    public static void replay(UserConnection user) {
        List<Entry> list = QUEUES.remove(user);
        if (list == null || list.isEmpty()) return;
        BedrockFixClient.LOG.info("Replaying {} early packets", list.size());
        int ok = 0;
        for (Entry e : list) {
            ByteBuf buf = Unpooled.buffer(e.payload().readableBytes() + 5);
            Types.VAR_INT.writePrimitive(buf, e.id());
            buf.writeBytes(e.payload());
            e.payload().release();
            try {
                user.transformClientbound(buf, t -> new DecoderException(t));
                user.sendRawPacket(buf);
                ok++;
            } catch (Throwable t) {
                buf.release();
                if (!isCancel(t)) {
                    BedrockFixClient.LOG.warn("Failed to replay {}: {}", e.name(), t.toString());
                }
            }
        }
        BedrockFixClient.LOG.info("Replay finished ({} forwarded, rest handled internally)", ok);
    }

    private static boolean isCancel(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            if (c instanceof CancelException) return true;
        }
        return false;
    }
}
