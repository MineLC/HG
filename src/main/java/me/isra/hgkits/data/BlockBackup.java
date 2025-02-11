package me.isra.hgkits.data;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;

@Getter
public class BlockBackup {
    private final Block block;
    private final byte data;
    private final Material material;

    @SuppressWarnings("deprecation")
    public BlockBackup(Block block) {
        this.block = block;
        this.data = block.getData();
        this.material = block.getType();
    }
}
