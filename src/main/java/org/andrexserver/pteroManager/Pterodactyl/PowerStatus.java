package org.andrexserver.pteroManager.Pterodactyl;

public enum PowerStatus {
    INVALID_SERVER("invalid-server"), INVALID_STATUS("invalid-status"), ALREADY_RUNNING("already-running"),
    ALREADY_DOWN("already-shutdown"), INVALID_UUID("invalid-uuid"), INVALID_REQUEST("invalid-request"),
    SUCCESS("success");
    private final String statusName;
    PowerStatus(String statusName) {
        this.statusName = statusName;
    }
    public String getStatusName() {
        return statusName;
    }
}
