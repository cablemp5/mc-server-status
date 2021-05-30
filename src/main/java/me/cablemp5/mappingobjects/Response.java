package me.cablemp5.mappingobjects;

import java.util.HashMap;

public class Response {

    private HashMap<String,String[]> motd;
    private String protocol;
    private String hostname;
    private HashMap<String,String> debug;
    private String port;
    private ResponsePlayer players;
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

    public ResponsePlayer getPlayers() {
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
