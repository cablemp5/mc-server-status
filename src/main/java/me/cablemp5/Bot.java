package me.cablemp5;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.*;
import me.cablemp5.events.GuildMessageReceived;
import me.cablemp5.jsonobjects.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;


public class Bot {

    public static Config CONFIG = getConfig();
    public static MongoCollection<Document> MONGO_COLLECTION = connectMongoDBDatabase();

    public static void main(String[] args) {

        //Load HashMap
        loadDatabaseMap(MONGO_COLLECTION);

        //Connect to discord websocket
        try {
            JDA jda = JDABuilder.createDefault(CONFIG.getToken())
                    .setActivity(Activity.playing("Minecraft | !status"))
                    .addEventListeners(new GuildMessageReceived())
                    .build();
            jda.awaitReady();
            System.out.println("[JDA] Discord Bot Connected!");
        } catch (Exception e) {
            System.out.println("Failed to connect Discord Bot!");
        }
    }

    public static Config getConfig() {

        try {
            File file = new File(Bot.class.getClassLoader().getResource("config.json").getFile());
            BufferedReader br = new BufferedReader(new FileReader(file));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            System.out.println("[Config] Extracted config.json!");
            return gson.fromJson(br,Config.class);
        } catch (Exception e) {
            System.out.println("Failed to get config file!");
            System.exit(0);
        }
        return null;
    }

    public static MongoCollection<Document> connectMongoDBDatabase() {

        try {
            MongoClient mongoClient = MongoClients.create("mongodb+srv://cablemp5:oG3Secd3MH3A@cluster.6v91e.mongodb.net/test");
            MongoDatabase mongoDatabase = mongoClient.getDatabase("SimpleServerStatus");
            System.out.println("[MongoDB] Database Connected!");
            return mongoDatabase.getCollection("GuildsAndIPs");
        } catch (Exception e) {
            System.out.println("Failed to connect to the database!");
            System.exit(0);
        }
        return null;
    }

    public static void loadDatabaseMap(MongoCollection<Document> collection) {

        MongoCursor<Document> cursor = collection.find().iterator();
        while (cursor.hasNext()) {
            Document GuildDocument = cursor.next();
            GuildMessageReceived.databaseMap.put(GuildDocument.get("guild_id").toString(),GuildDocument.get("ip").toString());
        }
        cursor.close();

        System.out.println("[MongoDB] Collection updated!");

    }
}
