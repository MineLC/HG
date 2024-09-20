package me.isra.hgkits.database;

import java.util.UUID;

public class User {

    public final UUID uuid;
    public final String name;

    public int kills = 0;
    public int deaths = 0;
    public int fame = 0;

    public int wins = 0;

    public User(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public boolean isNew() {
        return false;
    }

    public double getKdr() {
        return (deaths == 0) ? kills : kills / deaths;
    }

    public static final class New extends User {
        public New(UUID uuid, String name) {
            super(uuid, name);
        }
        @Override
        public boolean isNew() {
            return true;
        }
    }
}