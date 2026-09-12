package com.aynclub.akilleffect.utils;

import com.aynclub.akilleffect.Main;
import com.aynclub.akilleffect.effect.MainEffectKill;
import com.aynclub.akilleffect.effect.item.DropDiamond;
import com.aynclub.akilleffect.effect.item.DropFlowers;
import com.aynclub.akilleffect.effect.item.DropSoup;
import com.aynclub.akilleffect.effect.particle.Firework;
import com.aynclub.akilleffect.effect.particle.FrostFlame;
import com.aynclub.akilleffect.effect.particle.Heart;
import com.aynclub.akilleffect.effect.particle.Rainbow;
import com.aynclub.akilleffect.effect.particle.Redstone;
import com.aynclub.akilleffect.effect.particle.Spiral;
import com.aynclub.akilleffect.effect.particle.TNT;
import com.aynclub.akilleffect.effect.particle.TNTBreak;
import com.aynclub.akilleffect.effect.particle.Wave;
import com.aynclub.akilleffect.effect.particle.WoolBreak;
import com.aynclub.akilleffect.effect.sound.Lightning;
import com.aynclub.akilleffect.effect.sound.Music;
import com.aynclub.akilleffect.effect.sound.Thunder;
import com.aynclub.akilleffect.effect.special.SnowFall;
import com.aynclub.akilleffect.effect.special.Tornado;
import com.aynclub.akilleffect.effect.visual.FireTrail;
import com.aynclub.akilleffect.utils.config.YAMLUtils;
import com.aynclub.akilleffect.utils.inventory.CustomInventory;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Manager {
    private final Map<String, MainEffectKill> effectKills = new HashMap<String, MainEffectKill>();

    public static void buildConfigs(String... configs) {
        for (String config : configs) {
            if (config.equalsIgnoreCase("config")) {
                YAMLUtils yaml = YAMLUtils.get("config");
                String helpMessage = "&c击杀特效 &7- &f选择你喜欢的击杀收尾效果。\n"
                        + "\n"
                        + "&e/killeffect &7- &f打开击杀特效菜单。\n"
                        + "&e/killeffect help &7- &f查看帮助信息。\n"
                        + "\n";
                checkAndSetConfigText(yaml, "token", "");
                setConfigText(yaml, "help-command-message", helpMessage);
                yaml.save();
            } else if (config.equalsIgnoreCase("messages")) {
                YAMLUtils yaml = YAMLUtils.get("messages");
                setConfigText(yaml, "prefix", "&4[!] &c击杀特效 &7-");
                setConfigText(yaml, "no-player", "%prefix% &c玩家 %player% 不存在。");
                setConfigText(yaml, "list-effect", "%prefix% &c特效 &e%effectname% &c不存在。可用特效：&a ");
                setConfigText(yaml, "remove", "%prefix% &c已取消当前击杀特效。");
                setConfigText(yaml, "spawn", "%prefix% &f已选择 %effectname%");
                removeConfigText(yaml, "no-permission");
                removeConfigText(yaml, "check-permission-yes");
                removeConfigText(yaml, "check-permission-no");
                setConfigText(yaml, "menu.effectKill", "&8击杀特效");
                setConfigText(yaml, "menu.spawn", "&a[选择]");
                setConfigText(yaml, "menu.despawn", "&c[取消]");
                setConfigText(yaml, "menu.effect", "&f当前特效 -> ");

                for (MainEffectKill effectKill : MainEffectKill.instanceList) {
                    String effectName = effectKill.getName();
                    setConfigText(yaml, "effectKill." + effectName + ".name", getLocalizedEffectName(effectName));
                    setConfigText(yaml, "effectKill." + effectName + ".description", getLocalizedEffectDescription(effectName, effectKill.getDescription()));
                }
                yaml.save();
            } else {
                throw new NullPointerException(config + " is not defined!");
            }
        }
    }

    private static void checkAndSetConfigText(YAMLUtils yaml, String key, Object defaultValue) {
        if (!yaml.getConfig().contains(key)) {
            yaml.getConfig().set(key, defaultValue);
        }
    }

    private static void setConfigText(YAMLUtils yaml, String key, Object value) {
        yaml.getConfig().set(key, value);
    }

    private static void removeConfigText(YAMLUtils yaml, String key) {
        if (yaml.getConfig().contains(key)) {
            yaml.getConfig().set(key, null);
        }
    }

    private static String getLocalizedEffectName(String effectName) {
        switch (effectName.toLowerCase()) {
            case "dropsoup":
                return "&c蘑菇汤喷洒";
            case "dropdiamond":
                return "&b钻石雨";
            case "dropflowers":
                return "&d鲜花绽放";
            case "heart":
                return "&c爱心";
            case "wave":
                return "&b冰雪波纹";
            case "frostflame":
                return "&b冰焰";
            case "tnt":
                return "&cTNT 爆炸";
            case "tntbreak":
                return "&cTNT 碎裂";
            case "woolbreak":
                return "&f羊毛破碎";
            case "redstone":
                return "&c红石碎裂";
            case "firework":
                return "&e烟花";
            case "rainbow":
                return "&d彩虹";
            case "spiral":
                return "&5螺旋";
            case "tornado":
                return "&7龙卷风";
            case "snowfall":
                return "&f雪落";
            case "lightning":
                return "&b雷击";
            case "thunder":
                return "&9雷暴";
            case "music":
                return "&a音符";
            case "firetrail":
                return "&6火焰轨迹";
            default:
                return "&c" + capitalizeFirstLetter(effectName);
        }
    }

    private static List<String> getLocalizedEffectDescription(String effectName, List<String> fallback) {
        switch (effectName.toLowerCase()) {
            case "dropsoup":
                return Arrays.asList("&7在敌人倒下的位置喷出一圈蘑菇汤。", "&7短暂展示后会自动消失。");
            case "dropdiamond":
                return Arrays.asList("&7让钻石从击杀点四散飞出。", "&7胜利时更加耀眼。");
            case "dropflowers":
                return Arrays.asList("&7让鲜花在击杀点绽放散落。", "&7适合温柔又醒目的收尾。");
            case "heart":
                return Arrays.asList("&7在目标周围飘出爱心粒子。", "&7用一点温柔结束战斗。");
            case "wave":
                return Arrays.asList("&7释放向上扩散的冰雪水波。", "&7粒子会环绕击杀位置升起。");
            case "frostflame":
                return Arrays.asList("&7召唤一圈上升的冰蓝火焰。", "&7冷冽又利落。");
            case "tnt":
                return Arrays.asList("&7在击杀位置播放巨型爆炸特效。", "&7不会破坏方块。");
            case "tntbreak":
                return Arrays.asList("&7让 TNT 碎裂粒子四散。", "&7爆裂感十足。");
            case "woolbreak":
                return Arrays.asList("&7随机一种羊毛颜色碎裂飞散。", "&7伴随羊毛方块被破坏的声音。");
            case "redstone":
                return Arrays.asList("&7让红石碎裂粒子散开。", "&7红色光尘标记你的击杀。");
            case "firework":
                return Arrays.asList("&7发射一束短促烟花。", "&7在目标上方炸开火花。");
            case "rainbow":
                return Arrays.asList("&7用七彩尘环包围击杀位置。", "&7让胜利变得更鲜明。");
            case "spiral":
                return Arrays.asList("&7魔法暴击粒子螺旋升起。", "&7将目标包进上升粒子中。");
            case "tornado":
                return Arrays.asList("&7召唤一阵云雾龙卷风。", "&7从地面卷起到空中。");
            case "snowfall":
                return Arrays.asList("&7让雪花在击杀点旋转飘落。", "&7清冷干净的收尾。");
            case "lightning":
                return Arrays.asList("&7Micet 风格的默认雷击。", "&7击杀时落下一道无伤闪电。");
            case "thunder":
                return Arrays.asList("&7召唤更强烈的雷暴收尾。", "&7带有雷声、烟雾和爆炸冲击。");
            case "music":
                return Arrays.asList("&7播放一段短促旋律。", "&7音符会在目标上方升起。");
            case "firetrail":
                return Arrays.asList("&7火焰围绕击杀点旋转。", "&7留下短暂燃烧轨迹。");
            default:
                return fallback;
        }
    }

    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public void loadMinions() {
        MainEffectKill.instanceList.clear();
        MainEffectKill.effectList.clear();
        Main.getInstance().getEffectKill().clear();

        MainEffectKill.instanceList.addAll(Arrays.asList(
                new DropSoup(),
                new DropDiamond(),
                new DropFlowers(),
                new Heart(),
                new Wave(),
                new FrostFlame(),
                new TNT(),
                new TNTBreak(),
                new WoolBreak(),
                new Redstone(),
                new Firework(),
                new Rainbow(),
                new Spiral(),
                new Tornado(),
                new SnowFall(),
                new Lightning(),
                new Thunder(),
                new Music(),
                new FireTrail()));

        for (MainEffectKill effectKill : MainEffectKill.instanceList) {
            MainEffectKill.effectList.add(effectKill.getClass());
        }
    }

    public Map<String, MainEffectKill> getEffectKills() {
        return effectKills;
    }

    public CustomInventory buildInventory(User user) {
        return new CustomInventory(Main.getInstance(), "effectkill", false, null,
                Utils.colorize(String.valueOf(Utils.gfc("messages", "menu.effectKill"))), 45).advManipule(customInventory -> {
            if (user.getEffectKill() != null) {
                ItemStack item = ItemsUtils.create(user.getEffectKill().getItemStack(),
                        Utils.colorize(String.valueOf(Utils.gfc("messages", "menu.effect"))) + user.getEffectKill().getDisplayName(),
                        new ArrayList<String>());
                customInventory.addItem(item, 0);
                customInventory.addItem(ItemsUtils.create(getMenuDespawnMaterial(), (byte) 0,
                        Utils.colorize(String.valueOf(Utils.gfc("messages", "menu.despawn")))), 8);
            }

            int slot = 9;
            for (MainEffectKill effectKill : MainEffectKill.instanceList) {
                String itemName = String.valueOf(Utils.gfc("messages", "menu.spawn"));
                if (user.getEffectKill() != null && user.getEffectKill().getName().equalsIgnoreCase(effectKill.getName())) {
                    itemName = String.valueOf(Utils.gfc("messages", "menu.despawn"));
                }

                List<String> lores = new ArrayList<String>(YAMLUtils.get("messages").getConfig()
                        .getStringList("effectKill." + effectKill.getName() + ".description"));
                lores = lores.stream().map(Utils::colorize).collect(Collectors.toList());

                customInventory.addItem(
                        ItemsUtils.create(effectKill.getItemStack(),
                                Utils.colorize(itemName + " " + Utils.gfc("messages", "effectKill." + effectKill.getName() + ".name")
                                        + ChatColor.GRAY + " (" + effectKill.getName() + ")"),
                                lores),
                        slot);
                slot++;
            }
        });
    }

    private Material getMenuDespawnMaterial() {
        try {
            return Material.valueOf("BARRIER");
        } catch (IllegalArgumentException ignored) {
            return Material.REDSTONE;
        }
    }
}
