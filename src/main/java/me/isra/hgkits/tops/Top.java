package me.isra.hgkits.tops;


public class Top {
    private final Player[] players;

    public Top(Player[] players) {
        this.players = players;
    }
      
    public Player[] getPlayers() {
        return this.players;
    }
      
    public static final class Player {
        public final String name;
        
        public int value;
        
        public Player(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }
}
