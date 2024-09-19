package me.isra.hgkits.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Config {

    private Map<String, Object> map;

    public Config(Map<String, Object> map) {
        this.map = map;
    }

    public Map<String, Object> map() {
        return this.map;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(final String key) {
        final Object object = map.get(key);
        if (object == null) {
            return null;
        }
        try {
            return (T)object;
        } catch (Exception e) {
            return null;
        }
    }

    public <T> T getOrDefault(final String key, final T defaultValue) {
        final T value = get(key);
        return (value == null) ? defaultValue : value;
    }

    public String getString(final String key) {
        final Object object = map.get(key);
        return (object instanceof String) ? ((String)object) : null;
    }

    public List<?> getList(final String key) {
        final Object object = map.get(key);
        return (object instanceof List<?>) ? ((List<?>)object) : null;    
    }

    public int getInt(final String key) {
        final Object object = map.get(key);
        return (object instanceof Number) ? ((Number)object).intValue() : 0;
    }

    public boolean getBoolean(final String key) {
        final Object object = map.get(key);
        return (object instanceof Boolean) ? ((Boolean)object) : false;
    }

    public double getDouble(final String key) {
        final Object object = map.get(key);
        return (object instanceof Number) ? ((Number)object).doubleValue() : 0;    
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getMap(final String key) {
        final Object object = map.get(key);
        return (object instanceof Map<?, ?>) ? (Map<String, Object>) object : new HashMap<>();
    }


    public void setMap(Map<String, Object> map) {
        this.map = map;
    }
}