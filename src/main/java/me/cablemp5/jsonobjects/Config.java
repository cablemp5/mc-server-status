package me.cablemp5.jsonobjects;

public class Config {

    private String token;
    private Character command_prefix;
    private String owner_id;

    public Character getCommandPrefix() {
        return command_prefix;
    }

    public String getOwnerID() { return owner_id; }

    public String getToken() {
        return token;
    }
}