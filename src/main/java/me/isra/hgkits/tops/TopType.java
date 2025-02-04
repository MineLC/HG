package me.isra.hgkits.tops;

public enum TopType {
    KILLS("asesinato"), DEATHS("muerte");

    public final String displayName;

    TopType(String displayName) {
        this.displayName = displayName;
    }
}
