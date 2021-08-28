package de.fbrettnich.easypoll.commands;

import de.fbrettnich.easypoll.language.GuildLanguage;
import de.fbrettnich.easypoll.language.GuildLanguageService;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;

@Singleton
public class PingCommand {

    @Inject
    private GuildLanguageService guildLanguageService;

    public void run(@Nonnull SlashCommandEvent event, GuildLanguage gl) {

        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle(guildLanguageService.getTranslation(gl, "commands.ping.title"));
        eb.setColor(Color.decode("#4CBB17"));
        eb.setDescription(guildLanguageService.getTranslation(gl, "commands.ping.description", String.valueOf(((int) event.getJDA().getGatewayPing()))));

        event.replyEmbeds(
                eb.build()
        ).queue(null, Sentry::captureException);

    }
}
