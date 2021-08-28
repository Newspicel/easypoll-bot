package de.fbrettnich.easypoll.commands;

import de.fbrettnich.easypoll.core.Constants;
import de.fbrettnich.easypoll.EasyPoll;
import de.fbrettnich.easypoll.language.GuildLanguage;
import de.fbrettnich.easypoll.language.GuildLanguageService;
import de.fbrettnich.easypoll.utils.FormatUtil;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;

@Singleton
public class InfoCommand {

    @Inject
    private GuildLanguageService guildLanguageService;

    public void run(@Nonnull SlashCommandEvent event, GuildLanguage gl) {

        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle(guildLanguageService.getTranslation(gl, "commands.info.title"), Constants.WEBSITE_URL);
        eb.setColor(Color.decode("#01FF70"));
        eb.setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

        eb.addField(guildLanguageService.getTranslation(gl, "commands.info.fields.creator"), Constants.BOT_OWNER_MENTION, false);
        eb.addField(guildLanguageService.getTranslation(gl, "commands.info.fields.repository"), "[github.com/newspicel/easypoll-bot](https://github.com/newspicel/easypoll-bot)", false);
        eb.addField(guildLanguageService.getTranslation(gl, "commands.info.fields.version"), Constants.VERSION, false);

        eb.addField(guildLanguageService.getTranslation(gl, "commands.info.fields.library"), "[JDA (Java Discord API)](https://github.com/DV8FromTheWorld/JDA)", false);

        eb.addField(guildLanguageService.getTranslation(gl, "commands.info.fields.servers"), FormatUtil.decimalFormat(EasyPoll.getShardManager().getGuilds().size()), false);
        eb.addField(guildLanguageService.getTranslation(gl, "commands.info.fields.users"), FormatUtil.decimalFormat(EasyPoll.getShardManager().getGuilds().stream().mapToInt(Guild::getMemberCount).sum()), false);

        eb.addField(guildLanguageService.getTranslation(gl, "commands.info.fields.shard"), (event.getGuild().getJDA().getShardInfo().getShardId() + 1) + "/" + EasyPoll.getShardManager().getShardsTotal(), false);
        eb.addField(guildLanguageService.getTranslation(gl, "commands.info.fields.uptime"), getUptime(), false);

        event.replyEmbeds(
                eb.build()
        ).queue(null, Sentry::captureException);

    }

    /**
     * Calculate the uptime of the bot and combine it as a string
     *
     * @return the bot uptime as string
     */
    private String getUptime() {
        int sec = (int)((System.currentTimeMillis() - Constants.STARTUP) / 1000);

        int day = sec / 60 / 60 / 24 % 365;
        int hour = sec / 60 /60 % 24;
        int minute = sec / 60 % 60;
        int second = sec % 60;

        if(sec < 60) second = sec;

        if(day == 0) {
            return hour + "h, " + minute + "m, " + second + "s";
        }else{
            return day + "d, " + hour + "h, " + minute + "m, " + second + "s";
        }
    }
}
