package me.isra.hgkits.commands;

import me.isra.hgkits.HGKits;
import me.isra.hgkits.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class TeamCommand implements CommandExecutor {
    private TeamManager teamManager;
    private HGKits plugin;

    public TeamCommand(HGKits plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            String message = plugin.getTranslateManager().getMessage("player-only-command");
            if (message != null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            } else {
                sender.sendMessage("Only players can use this command.");
            }
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            String message = plugin.getTranslateManager().getMessage("team-usage");
            if (message != null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            } else {
                player.sendMessage("Usage: /team <add|kick|accept|deny|toggle>");
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "add":
                if (args.length < 2) {
                    String message = plugin.getTranslateManager().getMessage("team-usage");
                    if (message != null) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                    } else {
                        player.sendMessage("Usage: /team <add|kick|accept|deny|toggle>");
                    }
                    return true;
                }
                Player invitee = Bukkit.getPlayer(args[1]);
                if (invitee != null) {
                    teamManager.invitePlayer(player, invitee);
                } else {
                    String message = plugin.getTranslateManager().getMessage("team-player-not-found");
                    if (message != null) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                    } else {
                        player.sendMessage("Player not found.");
                    }
                }
                break;
            case "kick":
                if (args.length < 2) {
                    String message = plugin.getTranslateManager().getMessage("team-usage");
                    if (message != null) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                    } else {
                        player.sendMessage("Usage: /team <add|kick|accept|deny|toggle>");
                    }
                    return true;
                }
                Player member = Bukkit.getPlayer(args[1]);
                if (member != null) {
                    teamManager.kickPlayer(player, member);
                } else {
                    String message = plugin.getTranslateManager().getMessage("team-player-not-found");
                    if (message != null) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                    } else {
                        player.sendMessage("Player not found.");
                    }
                }
                break;
            case "accept":
                teamManager.acceptInvitation(player);
                break;
            case "deny":
                teamManager.denyInvitation(player);
                break;
            case "toggle":
                teamManager.toggleAvailability(player);
                break;
            default:
                String message = plugin.getTranslateManager().getMessage("team-usage");
                if (message != null) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                } else {
                    player.sendMessage("Usage: /team <add|kick|accept|deny|toggle>");
                }
                break;
        }

        return true;
    }
}
