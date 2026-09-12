package com.aynclub.akilleffect.effect;

import com.aynclub.akilleffect.Main;
import com.aynclub.akilleffect.utils.ItemsUtils;
import com.aynclub.akilleffect.utils.User;
import com.aynclub.akilleffect.utils.Utils;
import com.google.common.collect.Lists;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class MainEffectKill implements Listener {

    public static List<MainEffectKill> instanceList = Lists.newArrayList();
    public static Main instance;
    public static MainEffectKill effectKill;
    public static List<Class<? extends MainEffectKill>> effectList = Lists.newArrayList();
    public ArrayList<Entity> spawnedEntities = new ArrayList<Entity>();
    public ItemStack itemStack;
    protected String name;
    protected String displayName;
    protected List<String> description;

    public MainEffectKill(String name, String displayName, ArrayList<String> description, ItemStack icon) {
        instance = Main.getInstance();
        effectKill = this;
        itemStack = ItemsUtils.create(icon, Utils.colorize(displayName == null ? capitalizeFirstLetter(name) : displayName), description);
        this.name = name;
        this.displayName = displayName == null ? capitalizeFirstLetter(name) : displayName;
        this.description = description;
        instance.getServer().getPluginManager().registerEvents(this, instance);
        instance.getEffectKill().put(name, this);
    }

    public static MainEffectKill getInstance() {
        return effectKill;
    }

    public void despawn(User user) {
        if (user.getEffectKill() != null) {
            user.setEffectKill(null);
        }
    }

    public abstract void update(User user);

    public void update(User victim, User killer) {
        update(victim);
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        Object configuredName = Utils.gfc("messages", "effectKill." + name + ".name");
        if (configuredName instanceof String) {
            return Utils.colorize((String) configuredName);
        }

        return Utils.colorize(displayName);
    }

    public List<String> getDescription() {
        return description;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
