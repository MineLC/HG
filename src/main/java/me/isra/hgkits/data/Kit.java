package me.isra.hgkits.data;

import java.util.List;

public record Kit(String name, int cost, List<String> items, List<String> effects, List<String> lore) {

}
