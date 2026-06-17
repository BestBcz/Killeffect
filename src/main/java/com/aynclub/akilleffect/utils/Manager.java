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
import org.bukkit.inventory.meta.SkullMeta;

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
                String helpMessage = "aKilleffect - A Killeffect plugin made by aynclub.\n"
                        + "\n"
                        + "/killeffect - Open the effect menu.\n"
                        + "/killeffect help - Show this message.\n"
                        + "\n";
                checkAndSetConfigText(yaml, "token", "");
                Object currentHelp = yaml.getConfig().get("help-command-message");
                if (!(currentHelp instanceof String)
                        || String.valueOf(currentHelp).contains("/akilleffect")
                        || String.valueOf(currentHelp).contains("/killeffect menu")) {
                    yaml.getConfig().set("help-command-message", helpMessage);
                }
                yaml.save();
            } else if (config.equalsIgnoreCase("messages")) {
                YAMLUtils yaml = YAMLUtils.get("messages");
                checkAndSetConfigText(yaml, "prefix", "&4[!] &cKilleffect &7-");
                checkAndSetConfigText(yaml, "no-permission", "%prefix% &cYou do not have the required permission!");
                checkAndSetConfigText(yaml, "no-player", "%prefix% &cThis player %player% doesn't exist");
                checkAndSetConfigText(yaml, "list-effect", "%prefix% &cThe Effect &e%effectname% &cdoes not exist. Here is the list of effects:&a ");
                checkAndSetConfigText(yaml, "remove", "%prefix% &cYou deleted your effect");
                checkAndSetConfigText(yaml, "spawn", "%prefix% &fYou selected %effectname%");
                checkAndSetConfigText(yaml, "check-permission-yes", "&a&nYou can switch this effect!");
                checkAndSetConfigText(yaml, "check-permission-no", "&c&nYou don't have this effect yet! :(");
                checkAndSetConfigText(yaml, "menu.effectKill", "&7EffectMenu");
                checkAndSetConfigText(yaml, "menu.spawn", "&a[SELECT]");
                checkAndSetConfigText(yaml, "menu.despawn", "&c[REMOVE]");
                checkAndSetConfigText(yaml, "menu.effect", "&fState -> ");

                for (MainEffectKill effectKill : MainEffectKill.instanceList) {
                    String effectName = effectKill.getName();
                    checkAndSetConfigText(yaml, "effectKill." + effectName + ".name", "&c" + capitalizeFirstLetter(effectName));
                    checkAndSetConfigText(yaml, "effectKill." + effectName + ".description", effectKill.getDescription());
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
                new Redstone(),
                new Firework(),
                new Rainbow(),
                new Spiral(),
                new Tornado(),
                new SnowFall(),
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
                ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
                SkullMeta skull = (SkullMeta) item.getItemMeta();
                skull.setDisplayName(Utils.colorize(String.valueOf(Utils.gfc("messages", "menu.effect"))) + user.getEffectKill().getDisplayName());
                skull.setOwner(user.getPlayer().getName());
                item.setItemMeta(skull);
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
                lores.add(" ");
                if (user.getPlayer().hasPermission("akilleffect.effect." + effectKill.getName())) {
                    lores.add(Utils.colorize(String.valueOf(Utils.gfc("messages", "check-permission-yes"))));
                } else {
                    lores.add(Utils.colorize(String.valueOf(Utils.gfc("messages", "check-permission-no"))));
                }

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
