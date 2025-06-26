package org.andrexserver.pteroManager.Pterodactyl;

public class StatsResponse {
    public String object;
    public Attributes attributes;

    public static class Attributes {
        public String current_state;
        public boolean is_suspended;
        public Resources resources;

    }

    public static class Resources {
        public long memory_bytes;
        public double cpu_absolute;
        public long disk_bytes;
        public long network_rx_bytes;
        public long network_tx_bytes;
        public int uptime;
    }
}
