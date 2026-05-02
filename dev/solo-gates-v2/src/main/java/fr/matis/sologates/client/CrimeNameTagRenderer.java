package fr.matis.sologates.client;

import fr.matis.sologates.SoloGates;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SoloGates.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class CrimeNameTagRenderer {

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        int level = ClientCrimeData.getLevel(player.getUUID());
        if (level <= 0) return;
        ChatFormatting color = switch (level) {
            case 1 -> ChatFormatting.GOLD;
            case 2 -> ChatFormatting.RED;
            default -> ChatFormatting.DARK_RED;
        };
        event.setContent(java.util.Objects.requireNonNull(event.getContent().copy().withStyle(color)));
    }

    private CrimeNameTagRenderer() {}
}
