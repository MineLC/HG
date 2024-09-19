package me.isra.hgkits.data;

import java.util.List;

public class Kit{
    private String name;
    private List<String> items;
    private List<String> effects;
    private List<String> lore;

    public Kit(String name, List<String> items, List<String> effects, List<String> lore) {
        this.name = name;
        this.items = items;
        this.effects = effects;
        this.lore = lore;
    }

    public String getName() {
        return name;
    }

    public List<String> getItems() {
        return items;
    }

    public List<String> getEffects() {
        return effects;
    }

    public List<String> getLore() {
        return lore;
    }
}
