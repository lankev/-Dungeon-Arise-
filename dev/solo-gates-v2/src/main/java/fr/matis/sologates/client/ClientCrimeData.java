package fr.matis.sologates.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public final class ClientCrimeData {
    private static final Map<UUID, Integer> levels = new HashMap<>();

    public static void update(UUID uuid, int level) {
        if (level <= 0) levels.remove(uuid);
        else levels.put(uuid, level);
    }

    public static int getLevel(UUID uuid) {
        return levels.getOrDefault(uuid, 0);
    }

    private ClientCrimeData() {}
}
