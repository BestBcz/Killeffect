package com.aynclub.akilleffect.effect;

import com.aynclub.akilleffect.Main;
import com.aynclub.akilleffect.utils.ItemsUtils;
import com.aynclub.akilleffect.utils.User;
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

    public MainEffectKill(String name, String displayName, ArrayList<String> description, String texture) {
        instance = Main.getInstance();
        effectKill = this;
        ItemStack head = ItemsUtils.getSkull(texture);
        itemStack = ItemsUtils.create(head, displayName, description);
        this.name = name;
        this.displayName = capitalizeFirstLetter(name);
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
        return displayName;
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
