package me.hypericats.fedmaps.map;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.item.map.MapDecoration;

public class DungeonPlayer {
    private final String name;
    private final String icon;
    private final SkinTextures textures;

    private float mapX = 0;
    private float mapZ = 0;
    private float yaw = 0f;

    private boolean renderHat = true;
    private String uuid = null;

    public DungeonPlayer(String name, int iconIndex, SkinTextures textures) {
        this.name = name;
        this.textures = textures;
        this.icon = "icon-" + iconIndex;
    }

    public void setData(PlayerEntity player) {
        this.uuid = player.getUuidAsString();
        this.renderHat = player.isModelPartVisible(PlayerModelPart.HAT);

    }

    public String getName() {
        return name;
    }

    // Assumes it is valid and belong to this player
    public void updatePosition(MapDecoration deco) {
        if (DungeonScan.getMapInfo() == null) return;
        this.mapX = (((deco.x() + 128) >> 1) - DungeonScan.getMapInfo().getStartCorner().x) / (float) DungeonScan.getMapInfo().getRoomSize();
        this.mapZ = (((deco.z() + 128) >> 1) - DungeonScan.getMapInfo().getStartCorner().y) / (float) DungeonScan.getMapInfo().getRoomSize();
        this.yaw = (float) Math.toRadians((deco.rotation() / 16.0f) * 360.0f + 180.0); //+ 90.0
    }

    public String getIcon() {
        return icon;
    }

    public boolean isRenderHat() {
        return renderHat;
    }

    public String getUuid() {
        return uuid;
    }

    public boolean isLoaded() {
        return uuid != null;
    }

    public SkinTextures getTextures() {
        return textures;
    }

    public float getMapX() {
        return mapX;
    }

    public float getMapZ() {
        return mapZ;
    }

    public float getYaw() {
        return yaw;
    }
}
