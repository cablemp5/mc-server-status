package me.cablemp5.mongo;

import com.mongodb.client.*;
import me.cablemp5.events.GuildMessageReceived;
import org.bson.Document;

public class MongoConnection {

    private String uri;
    private String databaseName;
    private String collectionName;
    

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private MongoCollection<Document> mongoCollection;

    public MongoConnection(String uri, String databaseName, String collectionName) {

        this.uri = uri;
        this.databaseName = databaseName;
        this.collectionName = collectionName;

    }

    public void initializeConnection() {

        try {
            mongoClient = MongoClients.create(uri);
            mongoDatabase = mongoClient.getDatabase(databaseName);
            mongoCollection = mongoDatabase.getCollection(collectionName);
            System.out.println("[MongoDB] Database Connected!");
        } catch (Exception e) {
            System.out.println("[MongoDB] Failed to connect to the database!");
            e.printStackTrace();
        }
    }

    public void loadHashMap() {

        MongoCursor<Document> cursor = mongoCollection.find().iterator();
        while (cursor.hasNext()) {
            Document GuildDocument = cursor.next();
            GuildMessageReceived.ipMap.put(GuildDocument.get("guild_id").toString(),GuildDocument.get("ip").toString());
        }
        cursor.close();
        System.out.println("[MongoDB] Hashmap Initialized!");

    }

    public MongoCollection<Document> getMongoCollection() {
        return mongoCollection;
    }
}
