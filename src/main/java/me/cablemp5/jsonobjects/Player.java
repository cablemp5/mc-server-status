package me.cablemp5.jsonobjects;

import java.util.HashMap;

public class Player {

    private String max;
    private String online;
    private String[] list;
    private HashMap<String,String> uuid;

    public String getMax() {
        return max;
    }

    public String getOnline() {
        return online;
    }

    public String[] getList() {
        return list;
    }

    public HashMap<String, String> getUuid() {
        return uuid;
    }

}
