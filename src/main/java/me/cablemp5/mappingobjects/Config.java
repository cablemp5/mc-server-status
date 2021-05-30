package me.cablemp5.mappingobjects;

public class Config {

    private String token;
    private String mongo_uri;
    private String command_prefix;
    private String owner_id;

    public String getCommandPrefix() {
        return command_prefix;
    }

    public String getOwnerID() { return owner_id; }

    public String getToken() {
        return token;
    }

    public String getMongoUri() { return mongo_uri; }

}
