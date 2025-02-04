package me.isra.hgkits;

import java.util.HashSet;
import java.util.Set;

public class Team {
    private String leader;
    private Set<String> members;

    public Team(String leader) {
        this.leader = leader;
        this.members = new HashSet<>();
        this.members.add(leader);
    }

    public String getLeader() {
        return leader;
    }

    public Set<String> getMembers() {
        return members;
    }

    public void addMember(String player) {
        members.add(player);
    }

    public void removeMember(String player) {
        members.remove(player);
    }

    public boolean isMember(String player) {
        return members.contains(player);
    }
}
