package me.isra.hgkits.commands;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.enums.GameState;
import me.isra.hgkits.translate.TranslateManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class FinalBattleCommand implements CommandExecutor {

    private final TranslateManager translateManager;
    private final HGKits plugin;

    public FinalBattleCommand(TranslateManager translateManager, HGKits plugin) {
        this.translateManager = translateManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args){
        if(!commandSender.hasPermission("finalbattle")){
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("no-permission")));
            return true;
        }

        if(HGKits.GAMESTATE != GameState.GAME){
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("game-not-started")));
            return true;
        }

        plugin.getFinalBattleManager().createBattle();
        plugin.getFinalBattleManager().teleportGamers();
        return false;
    }
}
