package me.cablemp5.events;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import me.cablemp5.Main;
import me.cablemp5.mappingobjects.Response;
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

    private final Color DEFAULT_COLOR = new Color(0,180,181);

    public static HashMap<String, String> ipMap = new HashMap<>();

    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent e) {

        if (!e.getAuthor().isBot()) {
            
            String guildId = e.getGuild().getId();
            TextChannel chat = e.getChannel();
            String message = e.getMessage().getContentRaw();

            String ip = ipMap.getOrDefault(guildId, "");

            if (message.startsWith(Main.CONFIG.getCommandPrefix())) {
                
                String command = message.split(" ")[0].substring(Main.CONFIG.getCommandPrefix().length()).toLowerCase();
                String[] args = Arrays.copyOfRange(message.split(" "),1,message.split(" ").length);

                switch (command) {
                    case "setip" -> {
                        EmbedBuilder embed = new EmbedBuilder().setColor(DEFAULT_COLOR);
                        if (args.length == 1) {
                            ip = args[0];
                            if (ipMap.replace(guildId, ip) != null) {
                                Document query = Main.MONGO_CONNECTION.getMongoCollection().find(new Document("guild_id",guildId)).first();
                                if (query != null) {
                                    Bson update = new Document("ip",ip);
                                    Bson updateOperation = new Document("$set",update);
                                    Main.MONGO_CONNECTION.getMongoCollection().updateOne(query,updateOperation);
                                } else {
                                    PrivateChannel privateChannel = User.fromId(Main.CONFIG.getOwnerID()).openPrivateChannel().complete();
                                    privateChannel.sendMessage("[MongoDB] There was an error syncing the database and hashmap! Consider restarting the bot!").queue();
                                }
                            } else {
                                ipMap.put(guildId, ip);
                                Main.MONGO_CONNECTION.getMongoCollection().insertOne(new Document("guild_id", guildId).append("ip",ip));
                            }
                            embed.setTitle("Server IP was set to:  `" + ip + "`");
                        } else if (args.length == 0) {
                            embed.setTitle("Please include the server's IP address!");
                        } else {
                            embed.setTitle("Too many arguments! To use this command type 'setip [ip address]'");
                        }
                        chat.sendMessage(embed.build()).queue();
                    }
                    case "serverinfo" -> chat.sendMessage(args.length == 0 ? getInfo(ip).build() : getInfo(args[0]).build()).queue();
                    case "status" -> chat.sendMessage(args.length == 0 ? getStatus(ip).build() : getStatus(args[0]).build()).queue();
                    case "help" -> {
                        EmbedBuilder embed = new EmbedBuilder()
                            .setColor(DEFAULT_COLOR)
                            .setFooter("mcStatus's Commands","https://i.imgur.com/fFuQdNp.png")
                            .addField(Main.CONFIG.getCommandPrefix() + "setip <ip>","Set the minecraft server IP address that the status command will use.",false)
                            .addField(Main.CONFIG.getCommandPrefix() + "status","Get the status of a minecraft server, allowing you to see the number of players online, and a list of their names.",false)
                            .addField(Main.CONFIG.getCommandPrefix() + "serverinfo","Get the information of a minecraft server, allowing you to see the MOTD, Version, and the Port.",false);
                        chat.sendMessage(embed.build()).queue();
                    }
                    //maintenance commands
                    case "shutdown" -> {
                        if (e.getAuthor().getId().equals(Main.CONFIG.getOwnerID())) {
                            EmbedBuilder embed = new EmbedBuilder()
                                    .setColor(DEFAULT_COLOR)
                                    .setTitle("Shutting down...");
                            chat.sendMessage(embed.build()).queue();
                            System.out.println("[JDA] Remotely shut down from Discord!");
                            System.exit(0);
                        }
                    }
                }
            }
        }
    }

    public Response getResponse(String ip) throws UnirestException {

        HttpResponse<JsonNode> response = Unirest.get("https://api.mcsrvstat.us/2/" + ip).header("accept", "application/json").asJson();
        Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
        return gson.fromJson((response.getBody()).toString(), Response.class);

    }

    public EmbedBuilder getStatus(String ip) {

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(DEFAULT_COLOR)
                .setFooter(ip,"https://api.mcsrvstat.us/icon/" + ip);

        if (!ip.isEmpty()) {
            try {
                Response response = getResponse(ip);
                if (response.getOnline().equals("true")) {
                    int numPlayers = Integer.parseInt(response.getPlayers().getOnline());
                    embed.addField("\ud83d\udd79  # of Players Online:", "```" + numPlayers + "```", true);
                    if (numPlayers > 15) {
                        embed.addField("\ud83d\udcac   Players:", "```Too many players to display```", false);
                    } else if (numPlayers > 0) {
                        String players = Arrays.asList(response.getPlayers().getList()).toString();
                        embed.addField("\ud83d\udcac    Players:", "```" + players.substring(1, Arrays.asList(response.getPlayers().getList()).toString().length() - 1) + "```", false);
                    } else {
                        embed.addField("\ud83d\udcac   Players:", "```No players online```", false);
                    }
                } else {
                    embed.setDescription("This server is offline. Are you sure the ip adress is: " + ip);
                }
            } catch (Exception e) {
                embed.setTitle("There was an error getting the server info!");
            }
        } else {
            embed.setTitle("You haven't set the server IP address! Use `" + Main.CONFIG.getCommandPrefix() + "setip {ip}` to set it!");
        }
        return embed;
    }

    public EmbedBuilder getInfo(String ip) {

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(DEFAULT_COLOR)
                .setFooter(ip, "https://api.mcsrvstat.us/icon/" + ip);

        try {
            Response response = getResponse(ip);
            if(response.getOnline().equals("true")) {
                ArrayList<String> motd = new ArrayList<>(Arrays.asList(response.getMotd().get("clean")));
                for (int line = 0; line < motd.size(); line++) {
                    motd.set(line,motd.get(line).trim());
                }
                embed.addField("ℹ  IP Address:", "```" + response.getIp() + "```", false)
                    .addField("⌨  MOTD:", "```" + motd + "```", false)
                    .addField("\ud83d\udcbf  Version:", "```" + (response.getVersion().equals("1.8.x, 1.9.x, 1.10.x, 1.11.x, 1.12.x, 1.13.x, 1.14.x, 1.15.x, 1.16.x") ? "1.8.x - 1.16.x" : response.getVersion()) + "```", false)
                    .addField("\ud83d\udd0c  Port:", "```" + response.getPort() + "```", false);
            } else {
                embed.setDescription("This server is offline. Are you sure the ip adress is: " + ip);
            }
        } catch (Exception e) {
            embed.setTitle("There was an error getting the server info");
        }
        return embed;
    }
}
