package me.hypericats.fedmaps.map;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class StateManager {
    private static boolean skyblock = false;
    private static boolean boss = false;
    private static int floor = -1;
    private static Location location = Location.Unknown;


    private static final List<String> ENTRY_MESSAGES = List.of(
            "[BOSS] Bonzo: Gratz for making it this far, but I'm basically unbeatable.",
            "[BOSS] Scarf: This is where the journey ends for you, Adventurers.",
            "[BOSS] The Professor: I was burdened with terrible news recently...",
            "[BOSS] Thorn: Welcome Adventurers! I am Thorn, the Spirit! And host of the Vegan Trials!",
            "[BOSS] Livid: Welcome, you've arrived right on time. I am Livid, the Master of Shadows.",
            "[BOSS] Sadan: So you made it all the way here... Now you wish to defy me? Sadan?!"
    );

    public static boolean isSkyblock() {
        return skyblock;
    }

    public static Location getLocation() {
        return location;
    }


    public static void onWorldLoad(World world) {
        skyblock = false;
        boss = false;
        floor = -1;
        location = MinecraftClient.getInstance().isInSingleplayer() ? Location.SinglePlayer : Location.Unknown;
    }

    public static void onReceivePacket(Packet<?> packet) {
        switch (packet) {
            case PlayerListS2CPacket p -> onPlayerList(p);
            case ScoreboardObjectiveUpdateS2CPacket p -> onScoreboardObjective(p);
            case GameMessageS2CPacket p -> onGameMessage(p);
            case TeamS2CPacket p -> findFloor(p);
            default -> {
                return;
            }
        }
    }

    private static void onGameMessage(GameMessageS2CPacket packet) {
        if (location != Location.Dungeon) return;
        //System.out.println(packet.content().getString());
    }

    private static void onPlayerList(PlayerListS2CPacket packet) {
        if (location != Location.Unknown || !packet.getActions().contains(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME)) {
            return;
        }

        String area = null;

        for (PlayerListS2CPacket.Entry entry : packet.getEntries()) {
            if (entry == null) continue;
            if (entry.displayName() == null) continue;

            String text = entry.displayName().getString();
            if (text == null) continue;

            if (text.startsWith("Area: ") || text.startsWith("Dungeon: ")) {
                area = text;
                break;
            }
        }

        if (area == null) return;

        for (Location island : Location.values()) {
            if (area.toLowerCase().contains(island.getName().toLowerCase())) {
                if (location == island) return;
                location = island;
                updateEvents();
                return;
            }
        }

        location = Location.Unknown;
        updateEvents();
    }

    private static void findFloor(TeamS2CPacket packet) {
        if (location != Location.Dungeon || floor != -1 || packet.getTeam().isEmpty()) return;
        TeamS2CPacket.SerializableTeam team = packet.getTeam().get();
        String line = team.getSuffix().getString();
        int end = line.indexOf(')');
        if (end == -1) return;

        String beforeParen = line.substring(0, end);
        char last = beforeParen.charAt(beforeParen.length() - 1);
        floor = Character.isDigit(last) ? Character.digit(last, 10): 0;
        System.out.println("Found floor : " + floor);

        // Master Mode: "(M7)"
//                masterMode = beforeParen.length() >= 2
//                        && beforeParen.charAt(beforeParen.length() - 2) == 'M';
        return;
    }

    public static int getFloor() {
        return floor;
    }

    private static void updateEvents() {
        DungeonScan.onPostLocationUpdate();
    }

    private static void onScoreboardObjective(ScoreboardObjectiveUpdateS2CPacket packet) {
        if (!skyblock) {
            skyblock = "SBScoreboard".equals(packet.getName());
        }
    }
}
