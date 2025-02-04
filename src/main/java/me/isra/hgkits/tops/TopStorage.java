package me.isra.hgkits.tops;

public final class TopStorage  {
    private final Top kills;
    private final Top deaths;
    private final Top wins;
    private final Top kdr;

    private static TopStorage storage;

    public TopStorage(Top kills, Top deaths, Top wins, Top kdr) {
        this.kills = kills;
        this.deaths = deaths;
        this.wins = wins;
        this.kdr = kdr;
    }

    public static Top kills() {
        return storage.kills;
    }

    public static Top deaths() {
        return storage.deaths;
    }

    public static Top wins() {
        return storage.wins;
    }

    public static Top kdr() {
        return storage.kdr;
    }

    static void set(TopStorage newStorage) {
        storage = newStorage;
    }
}
