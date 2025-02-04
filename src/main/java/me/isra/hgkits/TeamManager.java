package me.isra.hgkits;

import me.isra.hgkits.managers.InvitationManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeamManager {
    private Map<UUID, Team> teams;
    private Map<UUID, UUID> invitations;
    private Map<UUID, Boolean> availability;
    private InvitationManager invitationManager;
    private HGKits plugin;

    public TeamManager(HGKits plugin) {
        this.plugin = plugin;
        this.teams = new HashMap<>();
        this.invitations = new HashMap<>();
        this.availability = new HashMap<>();
        this.invitationManager = new InvitationManager();
    }

    public void createTeam(Player leader) {
        teams.put(leader.getUniqueId(), new Team(leader.getName()));
    }

    public void invitePlayer(Player inviter, Player invitee) {
        if (inviter.getUniqueId().equals(invitee.getUniqueId())) {
            inviter.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getTranslateManager().getMessage("team-invite-self")));
            return;
        }

        if (!availability.getOrDefault(invitee.getUniqueId(), true)) {
            inviter.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getTranslateManager().getMessage("team-not-available").replace("%player%", invitee.getName())));
            return;
        }

        if (!teams.containsKey(inviter.getUniqueId())) {
            createTeam(inviter);
        }

        invitations.put(invitee.getUniqueId(), inviter.getUniqueId());
        invitationManager.addInvitation(invitee.getUniqueId(), inviter.getUniqueId());

        TextComponent message = new TextComponent(ChatColor.translateAlternateColorCodes('&', plugin.getTranslateManager().getMessage("team-invite").replace("%inviter%", inviter.getName())));
        message.addExtra("\n");

        TextComponent accept = new TextComponent(ChatColor.translateAlternateColorCodes('&', plugin.getTranslateManager().getMessage("team-accept")));
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/team accept"));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', plugin.getTranslateManager().getMessage("team-accept-hover"))).create()));
        message.addExtra(accept);

        message.addExtra(" ");

        TextComponent deny = new TextComponent(ChatColor.translateAlternateColorCodes('&', plugin.getTranslateManager().getMessage("team-deny")));
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/team deny"));
        deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', plugin.getTranslateManager().getMessage("team-deny-hover"))).create()));
        message.addExtra(deny);

        invitee.spigot().sendMessage(message);
        invitee.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getTranslateManager().getMessage("team-invite-duration")));

        inviter.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getTranslateManager().getMessage("team-invite-sent").replace("%player%", invitee.getName())));

        new BukkitRunnable() {
            @Override
            public void run() {
                invitations.remove(invitee.getUniqueId());
                invitationManager.removeInvitation(invitee.getUniqueId());
            }
        }.runTaskLater(plugin, 1200L); // 1 minute expiration
    }

    public void acceptInvitation(Player invitee) {
        UUID inviterId = invitationManager.removeInvitation(invitee.getUniqueId());
        if (inviterId != null) {
            Team team = teams.get(inviterId);
            if (team != null) {
                team.addMember(invitee.getName());
                invitee.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getTranslateManager().getMessage("team-joined").replace("%leader%", team.getLeader())));
                Player inviter = Bukkit.getPlayer(inviterId);
                if (inviter != null) {
                    inviter.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getTranslateManager().getMessage("team-member-joined").replace("%member%", invitee.getName())));
                }
            }
        } else {
            invitee.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getTranslateManager().getMessage("team-no-invitation")));
        }
    }

    public void denyInvitation(Player invitee) {
        UUID inviterId = invitationManager.removeInvitation(invitee.getUniqueId());
        if (inviterId != null) {
            invitee.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getTranslateManager().getMessage("team-denied")));
        } else {
            invitee.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getTranslateManager().getMessage("team-no-invitation")));
        }
    }

    public void kickPlayer(Player leader, Player member) {
        Team team = teams.get(leader.getUniqueId());
        if (team != null && team.isMember(member.getName())) {
            team.removeMember(member.getName());
            member.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getTranslateManager().getMessage("team-kicked").replace("%leader%", leader.getName())));
        }
    }

    public void toggleAvailability(Player player) {
        boolean current = availability.getOrDefault(player.getUniqueId(), true);
        availability.put(player.getUniqueId(), !current);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getTranslateManager().getMessage(current ? "team-toggle-disabled" : "team-toggle-enabled")));
    }

    public Team getTeam(Player player) {
        for (Team team : teams.values()) {
            if (team.isMember(player.getName())) {
                return team;
            }
        }
        return null;
    }
}
