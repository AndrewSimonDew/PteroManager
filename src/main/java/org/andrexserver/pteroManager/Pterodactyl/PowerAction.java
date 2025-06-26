package org.andrexserver.pteroManager.Pterodactyl;

public enum PowerAction {
    START("pterodactyl-start"), STOP("pterodactyl-stop"), RESTART("pterodactyl-restart");
    private final String actionName;
    PowerAction(String actionName) {
        this.actionName = actionName;
    }
    public String getActionName() {
        return actionName;
    }
}
