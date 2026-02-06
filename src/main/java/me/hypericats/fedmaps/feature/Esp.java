package me.hypericats.fedmaps.feature;

import me.hypericats.fedmaps.config.SettingHandler;
import me.hypericats.fedmaps.config.settings.MapSetting;
import me.hypericats.fedmaps.map.Location;
import me.hypericats.fedmaps.map.StateManager;
import me.hypericats.fedmaps.render.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.GiantEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.util.math.Box;

import java.util.*;

public class Esp extends Feature {

    public boolean drawBoxes = true;
    public boolean drawFilledBox = true;

    public boolean showStarredMobs = true;
    public boolean onlyShowInCurrentRoom = true;
    public boolean drawBloodMobs = true;
    public boolean withers = true;
    public boolean depthCheck = false;
    public int outlineColor = 0xFFD600FF;
    public int filledBoxColor = 0x1A790091;
    public int bloodOutline = 0xFFFF0000 ;
    public int bloodFill = 0x1A720000;
    public int witherOutline = 0xFF0066FF;
    public int witherFill = 0x1A003688;
    public int tracerCount = 10;

    public float updateInterval = 10;

    private final Set<Integer> starredMobs = new HashSet<>();
    private final Set<Integer> bloodMobs = new HashSet<>();
    private final Set<Integer> bloodNames = new HashSet<>();

    private int wither = -1;
    private double witherDistance = Double.MAX_VALUE;

    private int tick = 0;

    public Esp() {
        addName("Revoker");
        addName("Psycho");
        addName("Reaper");
        addName("Cannibal");
        addName("Mute");
        addName("Ooze");
        addName("Putrid");
        addName("Freak");
        addName("Leech");
        addName("Tear");
        addName("Parasite");
        addName("Flamer");
        addName("Skull");
        addName("Mr. Dead");
        addName("Vader");
        addName("Frost");
        addName("Walker");
        addName("Wandering Soul");
        addName("Bonzo");
        addName("Scarf");
        addName("Livid");
        addName("Spirit Bear");
    }

    private void addName(String name) {
        bloodNames.add(name.hashCode());
    }

    public void onWorldLoad() {
        starredMobs.clear();
        bloodMobs.clear();
        tick = 0;
    }

    public void onRenderWorld(MatrixStack matrices, VertexConsumerProvider vertices, float partialTicks) {
        if (!SettingHandler.fromName("esp", MapSetting.class).getValue()) return;
        if (!drawBoxes || StateManager.getLocation() != Location.Dungeon) return; // || !inDungeons

        if (showStarredMobs && !starredMobs.isEmpty()) { // && !BossEventDispatcher.inBoss
            handleRender(matrices, vertices, new ArrayList<>(starredMobs), outlineColor, filledBoxColor, partialTicks, starredMobs.size() <= tracerCount);
        }

        if (drawBloodMobs && !bloodMobs.isEmpty()) { // && !BossEventDispatcher.inBoss
            handleRender(matrices, vertices, new ArrayList<>(bloodMobs), bloodOutline, bloodFill, partialTicks, false);
        }

        if (withers && wither != -1) { // && BossEventDispatcher.inBoss
            Entity entity = MinecraftClient.getInstance().world.getEntityById(wither);
            if (entity == null) {
                wither = -1;
                return;
            }

            Box boundingBox = RenderUtils.getPartialEntityBoundingBox(entity, partialTicks);
            if (drawFilledBox)
                RenderUtils.drawFilledAABB(matrices, vertices, boundingBox, witherFill);
            if (drawBoxes)
                RenderUtils.drawOutlinedAABB(matrices, vertices, boundingBox, witherOutline, 1f);
        }
    }

    private void handleRender(MatrixStack matrices, VertexConsumerProvider vertices, List<Integer> ids, int outline, int fill,  float partialTicks, boolean tracer) {
        for (int i = 0; i < ids.size(); i++) {
            int entityID = ids.get(i);
            Entity entity = MinecraftClient.getInstance().world.getEntityById(entityID);
            if (entity == null) {
                ids.remove(i);
                i--;
                continue;
            }

            Box boundingBox = RenderUtils.getPartialEntityBoundingBox(entity, partialTicks);
            if (drawFilledBox)
                RenderUtils.drawFilledAABB(matrices, vertices, boundingBox, fill);
            if (drawBoxes)
                RenderUtils.drawOutlinedAABB(matrices, vertices, boundingBox, outline, 1f);
            if (tracer) {
                RenderUtils.drawLines(matrices, vertices, List.of(MinecraftClient.getInstance().gameRenderer.getCamera().getCameraPos().add(MinecraftClient.getInstance().player.getRotationVec(partialTicks).multiply(10)), boundingBox.getCenter()), outline, 2.0F, false);
            }
        }
    }

    private boolean isValidEntity(ArmorStandEntity entity) {
        if (!entity.hasCustomName() || !Objects.requireNonNull(entity.getCustomName()).getString().contains("✯ ") || !entity.getCustomName().getString().endsWith("❤")) return false;

//        if (onlyShowInCurrentRoom
//                && DungeonUtils.currentRoom != null
//                && !DungeonUtils.currentRoom.contains(entity.positionVector)) {
//            return false;
//        }

        return true;
    }
    public void onReceivePacket(Packet<?> packet) {
        if (StateManager.getLocation() != Location.Dungeon) return;
        //if (!DungeonUtils.inDungeons || BossEventDispatcher.inBoss) return;

        if (packet instanceof EntitiesDestroyS2CPacket) {
            for (int id : ((EntitiesDestroyS2CPacket) packet).getEntityIds()) starredMobs.remove(id);
        }
    }

    public void onClientTick() {
        if (!SettingHandler.fromName("esp", MapSetting.class).getValue()) return;
        if (MinecraftClient.getInstance().world == null || MinecraftClient.getInstance().player == null || StateManager.getLocation() != Location.Dungeon) return;
        tick++;
        if (tick % updateInterval != 0) return;

        starredMobs.clear();
        bloodMobs.clear();
        wither = -1;
        witherDistance = Double.MAX_VALUE;

        for (Entity e : MinecraftClient.getInstance().world.getEntities()) {
            if (showStarredMobs && e instanceof ArmorStandEntity stand) { // && !BossEventDispatcher.inBoss
                if (!isValidEntity(stand)) continue;
                Entity mob = getMobEntity(stand);
                if (mob == null) continue;

                starredMobs.add(mob.getId());
                stand.setCustomNameVisible(true);
                mob.setInvisible(false);
                continue;
            }

            if (e instanceof OtherClientPlayerEntity) { // && !BossEventDispatcher.inBoss
                if (showStarredMobs && e.getName().getString().trim().hashCode() == -0x277A5F7B) { //&& (DungeonUtils.currentRoom == null || DungeonUtils.currentRoom!!.contains(e.positionVector))) { // Shadow Assassin
                    starredMobs.add(e.getId());
                    e.setInvisible(false);
                    continue;
                }
                if (drawBloodMobs && bloodNames.contains(e.getName().getString().trim().hashCode())) {
                    bloodMobs.add(e.getId());
                    e.setInvisible(false);
                    continue;
                }
                continue;
            }

            if (showStarredMobs && e instanceof EndermanEntity) { // && !BossEventDispatcher.inBoss
                if (e.getName().hashCode() == -0x3BEF85AA)
                    e.setInvisible(false);
                continue;
            }

            if (drawBloodMobs && e instanceof GiantEntity) { //  && !BossEventDispatcher.inBoss
                bloodMobs.add(e.getId());
                e.setInvisible(false);
                continue;
            }

            if (withers && e instanceof WitherEntity && !e.isInvisible() && getSBMaxHealth((WitherEntity) e) > 400f) { // && BossEventDispatcher.inBoss
                if (wither == -1) {
                    wither = e.getId();
                    continue;
                }

                double dist = e.squaredDistanceTo(MinecraftClient.getInstance().player);
                if (dist < witherDistance) {
                    witherDistance = dist;
                    wither = e.getId();
                }
                continue;
            }
        }
    }

    public boolean isStarred(int id) {
        return starredMobs.contains(id);
    }

    public boolean isBlood(int id) {
        return bloodMobs.contains(id);
    }


    public boolean shouldCancelDepthCheck(int id) {
        return depthCheck && (isStarred(id) || isBlood(id)); // && esp enabled
    }

    private Entity getMobEntity(ArmorStandEntity stand) {
        if (MinecraftClient.getInstance().world == null) return null;
        return MinecraftClient.getInstance().world.getOtherEntities(
                        stand,
                        stand.getBoundingBox().offset(0.0, -1.0, 0.0)
                )
                .stream()
                .filter (e -> e instanceof LivingEntity && (!(e instanceof ArmorStandEntity)) && e != MinecraftClient.getInstance().player && !(e instanceof WitherEntity && e.isInvisible()) )
        .min(Comparator.comparingDouble( e ->
                e.squaredDistanceTo(stand)
        )).orElse(null);
    }

    public float getSBMaxHealth(LivingEntity e) {
        if (e == null) return 0f;
        EntityAttributeInstance attribute = e.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attribute == null) return 0f;
        return (float) attribute.getBaseValue();
    }
}

// TODO : esp the watcher eye


