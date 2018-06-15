package com.mengcraft.util;

import org.bukkit.enchantments.Enchantment;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

public class EnchantmentLocalize {

    private static final Properties PROPERTIES = new Properties();

    static {
        try {
            PROPERTIES.load(new StringReader("PROTECTION_ENVIRONMENTAL=保护\n" +
                    "PROTECTION_FIRE=火焰保护\n" +
                    "PROTECTION_FALL=掉落保护\n" +
                    "PROTECTION_EXPLOSIONS=爆炸保护\n" +
                    "PROTECTION_PROJECTILE=弹射物保护\n" +
                    "OXYGEN=水下呼吸\n" +
                    "WATER_WORKER=水下速掘\n" +
                    "THORNS=荆棘\n" +
                    "DEPTH_STRIDER=深海探索者\n" +
                    "FROST_WALKER=冰霜行者\n" +
                    "BINDING_CURSE=绑定诅咒\n" +
                    "DAMAGE_ALL=锋利\n" +
                    "DAMAGE_UNDEAD=亡灵克星\n" +
                    "DAMAGE_ARTHROPODS=截肢杀手\n" +
                    "KNOCKBACK=击退\n" +
                    "FIRE_ASPECT=火焰附加\n" +
                    "LOOT_BONUS_MOBS=抢夺\n" +
                    "SWEEPING_EDGE=横扫之刃\n" +
                    "DIG_SPEED=效率\n" +
                    "SILK_TOUCH=精准采集\n" +
                    "DURABILITY=耐久\n" +
                    "LOOT_BONUS_BLOCKS=时运\n" +
                    "ARROW_DAMAGE=力量\n" +
                    "ARROW_KNOCKBACK=冲击\n" +
                    "ARROW_FIRE=火矢\n" +
                    "ARROW_INFINITE=无限\n" +
                    "LUCK=海之眷顾\n" +
                    "LURE=钓饵\n" +
                    "MENDING=经验修补\n" +
                    "VANISHING_CURSE=消失诅咒\n"));
        } catch (IOException ignore) {
        }
    }

    public static String getLocalize(Enchantment enchant) {
        if (PROPERTIES.containsKey(enchant.getName())) {
            return (String) PROPERTIES.get(enchant.getName());
        }
        return enchant.getName();
    }
}
