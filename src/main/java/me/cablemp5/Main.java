package me.cablemp5;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.cablemp5.events.GuildMessageReceived;
import me.cablemp5.mappingobjects.Config;
import me.cablemp5.mongo.MongoConnection;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Objects;


public class Main {

    public static final Config CONFIG = getConfig();
    public static final MongoConnection MONGO_CONNECTION = new MongoConnection(CONFIG.getMongoUri(),"mcServerStatus","guildsAndIPs");

    public static void main(String[] args) {

        //Initialize MongoDB Connection
        MONGO_CONNECTION.initializeConnection();
        MONGO_CONNECTION.loadHashMap();

        //Connect to Websocket
        try {
            JDA jda = JDABuilder.createDefault(CONFIG.getToken())
                    .setActivity(Activity.playing("Minecraft | " + CONFIG.getCommandPrefix() + "help"))
                    .addEventListeners(new GuildMessageReceived())
                    .build();
            jda.awaitReady();
            System.out.println("[JDA] Discord Bot Connected!");
        } catch (Exception e) {
            System.out.println("[JDA] Failed to connect Discord Bot!");
        }
    }

    private static Config getConfig() {

        try {
            File file = new File(Objects.requireNonNull(Main.class.getClassLoader().getResource("config.json")).getFile());
            BufferedReader br = new BufferedReader(new FileReader(file));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            System.out.println("[Config] Extracted config.json!");
            return gson.fromJson(br,Config.class);
        } catch (Exception e) {
            System.out.println("[Config] Failed to get config file!");
            System.exit(0);
        }
        return null;
    }
}
