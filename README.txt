BedrockFix 0.2.0 (for Minecraft 1.21.4 + ViaFabricPlus 3.6.1, Bedrock 1.21.50)

What it does:
- Packets that the Bedrock server sends before the Java client is in PLAY state
  (inventory, hotbar, held item, player list, time) used to be thrown away by ViaBedrock.
  The mod queues them and replays them right after the client joins.
- Logs which screen receives mouse clicks (diagnostic for the broken buttons).

Look for these lines in latest.log:
  [BedrockFix] Queued early packet INVENTORY_SLOT until PLAY state
  [BedrockFix] Replaying N early packets
  [BedrockFix] Replay finished ...

Build: upload everything (including the hidden .github folder and the libs folder)
to a GitHub repository, open the Actions tab, download the bedrockfix-jar artifact.
Put the jar into mods together with Fabric API and ViaFabricPlus 3.6.1.
