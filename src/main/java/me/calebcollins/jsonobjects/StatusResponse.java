package me.calebcollins.jsonobjects;

import java.util.HashMap;

public class StatusResponse {

    private HashMap<String,String[]> motd;
    private String protocol;
    private String hostname;
    private HashMap<String,String> debug;
    private String port;
    private Player players;
    private String ip;
    private String icon;
    private String online;
    private String version;

    public HashMap<String, String[]> getMotd() {
        return motd;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHostname() {
        return hostname;
    }

    public HashMap<String, String> getDebug() {
        return debug;
    }

    public String getPort() {
        return port;
    }

    public Player getPlayers() {
        return players;
    }

    public String getIp() {
        return ip;
    }

    public String getIcon() {
        return icon;
    }

    public String getOnline() {
        return online;
    }

    public String getVersion() {
        return version;
    }
}
