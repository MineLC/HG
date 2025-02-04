package me.isra.hgkits.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InvitationManager {
    private final Map<UUID, UUID> pendingInvitations = new HashMap<>();

    public void addInvitation(UUID invitee, UUID inviter) {
        pendingInvitations.put(invitee, inviter);
    }

    public UUID getInviter(UUID invitee) {
        return pendingInvitations.get(invitee);
    }

    public UUID removeInvitation(UUID invitee) {
        return pendingInvitations.remove(invitee);
    }

    public boolean hasPendingInvitation(UUID invitee) {
        return pendingInvitations.containsKey(invitee);
    }
}
