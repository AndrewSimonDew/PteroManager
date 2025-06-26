package org.andrexserver.pteroManager.Pterodactyl;

public enum PowerStatus {
    INVALID_SERVER("pterodactyl-invalid-server"), INVALID_STATUS("pterodactyl-invalid-status"),
    ALREADY_RUNNING("pterodactyl-already-running"),
    ALREADY_DOWN("pterodactyl-already-shutdown"), INVALID_UUID("pterodactyl-invalid-uuid"),
    INVALID_REQUEST("pterodactyl-invalid-request"),
    SUCCESS("pterodactyl-success");
    private final String statusName;
    PowerStatus(String statusName) {
        this.statusName = statusName;
    }
    public String getStatusName() {
        return statusName;
    }
}
