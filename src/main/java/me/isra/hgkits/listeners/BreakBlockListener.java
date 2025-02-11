package me.isra.hgkits.listeners;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.enums.GameState;
import me.isra.hgkits.data.Kit;
import me.isra.hgkits.managers.KitManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashSet;
import java.util.Set;

public class BreakBlockListener implements Listener {
    private final KitManager kitManager;

    public BreakBlockListener(KitManager kitManager) {
        this.kitManager = kitManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (HGKits.GAMESTATE == GameState.PREGAME) {
            event.setCancelled(true);
        } else {
            Block block = event.getBlock();

            Player player = event.getPlayer();
            Kit kit = kitManager.getKitByPlayer(player);

            if (kit == null) {
                return;
            }

            if(kit.name().equals("Encantador")) {
                if (    block.getType() == Material.DIRT ||
                        block.getType() == Material.LEAVES ||
                        block.getType() == Material.LEAVES_2) {
                    event.setExpToDrop(10);
                }
            }

            if (kit.name().equals("Barbaro")) {
                if (block.getType() == Material.LOG) {
                    destroyTree(block);
                }
            }
        }
    }

    private void destroyTree(Block startBlock) {
        Set<Block> processedBlocks = new HashSet<>();
        processBlock(startBlock, processedBlocks);
    }

    private void processBlock(Block block, Set<Block> processedBlocks) {
        if (processedBlocks.contains(block)) {
            return;
        }

        processedBlocks.add(block);

        // Si el bloque es un tronco lo rompemos
        if (block.getType() == Material.LOG) {
            block.breakNaturally(); // Rompe el bloque y su caída es gestionada por el juego
            for (BlockFace face : BlockFace.values()) {
                Block adjacentBlock = block.getRelative(face);
                processBlock(adjacentBlock, processedBlocks);
            }
        }
    }
}
