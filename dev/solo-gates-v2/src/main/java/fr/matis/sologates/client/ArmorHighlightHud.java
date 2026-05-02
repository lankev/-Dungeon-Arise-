package fr.matis.sologates.client;

import fr.matis.sologates.SoloGates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.registries.ForgeRegistries;

@OnlyIn(Dist.CLIENT)
public class ArmorHighlightHud {

    // Disabled: the Solo Gates armor highlight overlay was too intrusive in-game.
    public static void onRenderArmorOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.ARMOR_LEVEL.type()) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || player.isCreative() || player.isSpectator()) return;
        if (!hasBetterModArmorInInventory(player) && !isEquippedArmorAboveBase(player)) return;

        float pulse = (float)(Math.sin(System.currentTimeMillis() / 350.0) * 0.5 + 0.5);
        int alpha = (int)(30 + pulse * 80);

        int sw = event.getWindow().getGuiScaledWidth();
        int sh = event.getWindow().getGuiScaledHeight();
        int x = sw / 2 - 91;

        // Armor bar moves up when there are extra health rows (maxHealth > 20)
        int totalHP = (int) Math.ceil(player.getMaxHealth() + player.getAbsorptionAmount());
        int healthRows = Math.max(1, (int) Math.ceil(totalHP / 20.0));
        int y = sh - 49 - (healthRows - 1) * 10;

        GuiGraphics g = event.getGuiGraphics();
        g.fill(x - 1, y - 1, x + 82, y + 10, (alpha << 24) | 0xFF2200);
        int border = Math.min(alpha + 60, 200);
        g.fill(x - 1, y - 1, x + 82, y,       (border << 24) | 0xFF2200);
        g.fill(x - 1, y + 9, x + 82, y + 10,  (border << 24) | 0xFF2200);
        g.fill(x - 1, y - 1, x,      y + 10,  (border << 24) | 0xFF2200);
        g.fill(x + 81, y - 1, x + 82, y + 10, (border << 24) | 0xFF2200);
    }

    private static final int[] VANILLA_MAX_DEFENSE = {3, 8, 6, 3}; // HEAD, CHEST, LEGS, FEET
    private static final EquipmentSlot[] ARMOR_SLOTS = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private static boolean isEquippedArmorAboveBase(Player player) {
        for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            ItemStack equipped = player.getItemBySlot(ARMOR_SLOTS[i]);
            if (equipped.getItem() instanceof ArmorItem a && a.getDefense() > VANILLA_MAX_DEFENSE[i])
                return true;
        }
        return false;
    }

    private static boolean hasBetterModArmorInInventory(Player player) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!(stack.getItem() instanceof ArmorItem armorItem)) continue;

            ResourceLocation key = ForgeRegistries.ITEMS.getKey(armorItem);
            if (key == null || !SoloGates.MOD_ID.equals(key.getNamespace())) continue;

            EquipmentSlot slot = armorItem.getEquipmentSlot();
            ItemStack equipped = player.getItemBySlot(slot);
            int curDef = equipped.getItem() instanceof ArmorItem a ? a.getDefense() : 0;

            if (armorItem.getDefense() > curDef) return true;
        }
        return false;
    }
}
