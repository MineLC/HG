package me.isra.hgkits.tops;

public enum TopType {
    KILLS("asesinato"), DEATHS("muerte"), WINS("victoria"), KDR("kdr");

    public final String displayName;

    TopType(String displayName) {
        this.displayName = displayName;
    }
}
