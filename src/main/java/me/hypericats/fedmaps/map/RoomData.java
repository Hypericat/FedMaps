package me.hypericats.fedmaps.map;

import java.util.Arrays;

public record RoomData(String name, RoomType type, int[] cores, int crypts, int secrets, int trappedChests) {
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Name : ").append(name).append(", Crypts : ").append(crypts).append(", Secrets : ").append(secrets).append(", TrappedChests : ").append(trappedChests).append(", Type : ").append(type.toString());
        builder.append(", Core(s) : ");
        Arrays.stream(cores).forEach(core -> builder.append(", ").append(core));
        return builder.toString();
    }
}
