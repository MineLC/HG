package me.isra.hgkits.tops;

public final class TopStorage  {
  private final Top kills;
  
  private final Top deaths;
  
  private static TopStorage storage;

    public TopStorage(Top kills, Top deaths) {
        this.kills = kills;
        this.deaths = deaths;
    }
    
    public static Top kills() {
        return storage.kills;
    }
    
    public static Top deaths() {
        return storage.deaths;
    }
    
    static void set(TopStorage newStorage) {
        storage = newStorage;
    }
}
