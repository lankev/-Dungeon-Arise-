package fr.matis.sologates.client;

import fr.matis.sologates.GateRank;
import fr.matis.sologates.SoloGates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = SoloGates.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class HunterRankHud {

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || player.isSpectator() || mc.options.hideGui) return;

        GateRank rank = ClientPlayerData.rank();
        int streak = ClientPlayerData.streak();
        int crimeStat = ClientPlayerData.crimeStat();

        GuiGraphics g = event.getGuiGraphics();
        Font font = mc.font;

        int x = 4;
        int y = 4;

        String rankLabel = "Chasseur " + rank.name();
        String crimeLabel = "Crime Stat " + crimeStat;
        int rankColor = rankColor(rank);

        boolean hasStreak = streak >= 2;
        int panelW = Math.max(font.width(rankLabel), crimeStat > 0 ? font.width(crimeLabel) : 0) + 10;
        int panelH = 13 + (crimeStat > 0 ? 10 : 0) + (hasStreak ? 10 : 0);

        // Background + border
        g.fill(x, y, x + panelW, y + panelH, 0xAA000000);
        g.fill(x, y, x + panelW, y + 1, 0x88FFFFFF);
        g.fill(x, y + panelH - 1, x + panelW, y + panelH, 0x88FFFFFF);
        g.fill(x, y, x + 1, y + panelH, 0x88FFFFFF);
        g.fill(x + panelW - 1, y, x + panelW, y + panelH, 0x88FFFFFF);

        // Rank text: "Chasseur " in white, rank letter in rank color
        int textY = y + 3;
        g.drawString(font, "Chasseur ", x + 4, textY, 0xDDDDDD, false);
        int labelW = font.width("Chasseur ");
        g.drawString(font, rank.name(), x + 4 + labelW, textY, rankColor, false);

        // Streak line
        int nextLineY = textY + 10;
        if (crimeStat > 0) {
            g.drawString(font, crimeLabel, x + 4, nextLineY, crimeColor(crimeStat), false);
            nextLineY += 10;
        }

        if (hasStreak) {
            String streakText = "Serie x" + streak;
            g.drawString(font, streakText, x + 4, nextLineY, 0xFFAA00, false);
        }
    }

    private static int rankColor(GateRank rank) {
        Integer color = rank.color().getColor();
        return color != null ? color : 0xFFFFFF;
    }

    private static int crimeColor(int crimeStat) {
        return switch (crimeStat) {
            case 1 -> 0xFFAA00;
            case 2 -> 0xFF5500;
            case 3 -> 0xFF2222;
            default -> 0xFFFFFF;
        };
    }
}
