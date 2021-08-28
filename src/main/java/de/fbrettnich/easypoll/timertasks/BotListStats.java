package de.fbrettnich.easypoll.timertasks;

import de.fbrettnich.easypoll.core.Constants;
import de.fbrettnich.easypoll.EasyPoll;
import de.fbrettnich.easypoll.files.ConfigFile;
import io.sentry.Sentry;
import kong.unirest.CookieSpecs;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONObject;
import net.dv8tion.jda.api.entities.Guild;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.TimerTask;

@Singleton
public class BotListStats extends TimerTask {

    @Inject
    private ConfigFile configFile;

    @Override
    public void run() {

        final int servers = EasyPoll.getShardManager().getGuilds().size();
        final int users = EasyPoll.getShardManager().getGuilds().stream().mapToInt(Guild::getMemberCount).sum();
        final int shards = EasyPoll.getShardManager().getShards().size();

        Unirest.config().cookieSpec(CookieSpecs.STANDARD);

        // top.gg
        {
            JSONObject obj = new JSONObject().put("server_count", servers).put("shard_count", shards);
            try {
                Unirest.post("https://top.gg/api/bots/" + Constants.BOT_ID + "/stats")
                        .header("Content-Type", "application/json")
                        .header("Authorization", configFile.getString("botlist.topgg.token"))
                        .body(obj.toString())
                        .asJson();
            } catch (UnirestException e) {
                Sentry.captureException(e);
            }
        }

        // discordbotlist.com
        {
            JSONObject obj = new JSONObject().put("guilds", servers).put("users", users);
            try {
                Unirest.post("https://discordbotlist.com/api/v1/bots/" + Constants.BOT_ID + "/stats")
                        .header("Content-Type", "application/json")
                        .header("Authorization", configFile.getString("botlist.discordbotlist.token"))
                        .body(obj.toString())
                        .asJson();
            } catch (UnirestException e) {
                Sentry.captureException(e);
            }
        }

        // bots.ondiscord.xyz
        {
            JSONObject obj = new JSONObject().put("guildCount", servers);
            try {
                Unirest.post("https://bots.ondiscord.xyz/bot-api/bots/" + Constants.BOT_ID + "/guilds")
                        .header("Content-Type", "application/json")
                        .header("Authorization", configFile.getString("botlist.botsondiscordxyz.token"))
                        .body(obj.toString())
                        .asJson();
            } catch (UnirestException e) {
                Sentry.captureException(e);
            }
        }

        // discord.bots.gg
        {
            JSONObject obj = new JSONObject().put("guildCount", servers).put("shardCount", shards);
            try {
                Unirest.post("https://discord.bots.gg/api/v1/bots/" + Constants.BOT_ID + "/stats")
                        .header("Content-Type", "application/json")
                        .header("Authorization", configFile.getString("botlist.discordbotsgg.token"))
                        .body(obj.toString())
                        .asJson();
            } catch (UnirestException e) {
                Sentry.captureException(e);
            }
        }
    }
}
