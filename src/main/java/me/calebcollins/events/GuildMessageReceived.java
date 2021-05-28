package me.calebcollins.events;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import me.calebcollins.Bot;
import me.calebcollins.jsonobjects.StatusResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class GuildMessageReceived extends ListenerAdapter {

    public static HashMap<String, String> databaseMap = new HashMap<>();

    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {

        if (!event.getAuthor().isBot()) {
            
            String guildId = event.getGuild().getId();
            TextChannel chat = event.getChannel();
            String message = event.getMessage().getContentRaw();

            String serverIp = "";
            if (databaseMap.containsKey(guildId)) {
                serverIp = databaseMap.get(guildId);
            }

            if (message.charAt(0) == Bot.config.getCommandPrefix()) {
                
                String command = message.split(" ")[0].substring(1);
                String[] args = Arrays.copyOfRange(message.split(" "),1,message.split(" ").length);
                
                switch (command) {
                    case "setip" -> {
                        EmbedBuilder embed = new EmbedBuilder().setColor(Color.ORANGE);
                        if (args.length == 1) {
                            serverIp = args[0];
                            if (databaseMap.containsKey(guildId)) {
                                databaseMap.replace(guildId, serverIp);
                                Document query = Bot.mongoCollection.find(new Document("guild_id",guildId)).first();
                                if (query != null) {
                                    Bson update = new Document("ip",serverIp);
                                    Bson updateOperation = new Document("$set",update);
                                    Bot.mongoCollection.updateOne(query,updateOperation);
                                } else {
                                    User user = User.fromId(Bot.config.getOwnerID());
                                    PrivateChannel privateChat = user.openPrivateChannel().complete();
                                    privateChat.sendMessage("There was an error syncing the database and hashmap. Consider restarting the bot.").queue();
                                }
                            } else {
                                databaseMap.put(guildId, serverIp);
                                Document document = new Document("guild_id", guildId).append("ip",serverIp);
                                Bot.mongoCollection.insertOne(document);
                            }
                            embed.setTitle("Server IP was set to:  `" + serverIp + "`");
                        } else if (args.length == 0) {
                            embed.setTitle("Please include the server's IP address");
                        } else {
                            embed.setTitle("Too many arguments. To use this command type 'setip [serverIp address]'");
                        }
                        chat.sendMessage(embed.build()).queue();
                    }
                    case "info" -> {
                        if (args.length == 0) {
                            getInfo(serverIp, chat);
                        } else if (args.length == 1) {
                            getInfo(args[0], chat);
                        }
                    }
                    case "status" -> {
                        if (args.length == 0) {
                            getStatus(serverIp, chat);
                        } else if (args.length == 1) {
                            getStatus(args[0], chat);
                        }
                    }
                    case "shutdown" -> {
                        if (event.getAuthor().getId().equals(Bot.config.getOwnerID())) {
                            EmbedBuilder embed = new EmbedBuilder()
                                    .setColor(Color.ORANGE)
                                    .setTitle("Shutting down SimpleServerStats...");
                            chat.sendMessage(embed.build()).queue();
                            System.out.println("[JDA] Remotely shut down from Discord!");
                            System.exit(0);
                        }

                    }
                }
            }
        }
    }

    public StatusResponse getResponse(String ip) throws UnirestException {

        HttpResponse<JsonNode> response = Unirest.get("https://api.mcsrvstat.us/2/" + ip).header("accept", "application/json").asJson();
        Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
        return gson.fromJson((response.getBody()).toString(), StatusResponse.class);

    }

    public void getStatus(String ip, TextChannel chat) {

        if (!ip.equals("")) {
            try {
                StatusResponse response = getResponse(ip);
                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(Color.ORANGE)
                        .setAuthor(ip, "https://api.mcsrvstat.us/icon/" + ip, "https://api.mcsrvstat.us/icon/" + ip);
                if (response.getOnline().equals("true")) {
                    embed.addField("\ud83d\udd79  # of Players Online:", "```" + response.getPlayers().getOnline() + "```", true);
                    int numPlayers = Integer.parseInt(response.getPlayers().getOnline());
                    if (numPlayers > 15) {
                        embed.addField("\ud83d\udcac  Players:", "```Too many players to display```", false);
                    } else if (numPlayers > 0) {
                        String players = Arrays.asList(response.getPlayers().getList()).toString();
                        embed.addField("\ud83d\udcac  Players:", "```" + players.substring(1, Arrays.asList(response.getPlayers().getList()).toString().length() - 1) + "```", false);
                    } else {
                        embed.addField("\ud83d\udcac  Players:", "```No players online```", false);
                    }
                } else {
                    embed.setDescription("This server is offline. Are you sure the ip adress is: " + ip);
                }
                chat.sendMessage(embed.build()).queue();
            } catch (Exception e) {
                EmbedBuilder errorembed = new EmbedBuilder()
                    .setColor(Color.ORANGE)
                    .setTitle("There was an error getting the server info");
                chat.sendMessage(errorembed.build()).queue();
            }
        } else {
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("You haven't set the server IP address. Use  `!setip [ip]`  to set it.")
                .setColor(Color.ORANGE);
            chat.sendMessage(embed.build()).queue();
        }
    }

    public void getInfo(String ip, TextChannel chat) {

        try {
            StatusResponse response = this.getResponse(ip);
            boolean online = response.getOnline().equals("true");
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(Color.ORANGE);
            embed.setAuthor(ip, "https://api.mcsrvstat.us/icon/" + ip, "https://api.mcsrvstat.us/icon/" + ip);
            if (!online) {
                embed.setDescription("This server is offline. Are you sure the ip adress is: " + ip);
            } else {
                embed.addField("ℹ️  IP Address:", "```" + response.getIp() + "```", false);
                ArrayList<String> motd = new ArrayList<>(Arrays.asList(response.getMotd().get("clean")));
                for (int line = 0; line < motd.size(); line++) {
                    motd.set(line,motd.get(line).trim());
                }
                embed.addField("⌨️  MOTD:", "```" + motd + "```", false);
                embed.addField("\ud83d\udcbf  Version:", response.getVersion().equals("1.8.x, 1.9.x, 1.10.x, 1.11.x, 1.12.x, 1.13.x, 1.14.x, 1.15.x, 1.16.x") ? "```1.8.x - 1.16.x```" : "```" + response.getVersion() + "```", false);
                embed.addField("\ud83d\udd0c  Port:", "```" + response.getPort() + "```", false);
            }
            chat.sendMessage(embed.build()).queue();
        } catch (Exception e) {
            EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("There was an error getting the server info");
            chat.sendMessage(embed.build()).queue();
        }
    }
}
