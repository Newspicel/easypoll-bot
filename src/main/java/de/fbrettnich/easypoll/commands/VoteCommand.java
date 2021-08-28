package de.fbrettnich.easypoll.commands;

import de.fbrettnich.easypoll.core.Constants;
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
public class VoteCommand {

    @Inject
    private GuildLanguageService guildLanguageService;

    public void run(@Nonnull SlashCommandEvent event, GuildLanguage gl) {

        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle(guildLanguageService.getTranslation(gl, "commands.vote.title"), Constants.VOTE_URL);
        eb.setColor(Color.decode("#01FF70"));
        eb.setDescription(
                guildLanguageService.getTranslation(gl, "commands.vote.description") +
                "\n" +
                "\u2022 [top.gg/bot/437618149505105920](https://top.gg/bot/437618149505105920/vote)\n" +
                "\u2022 [discordbotlist.com/bots/easypoll](https://discordbotlist.com/bots/easypoll/upvote)\n"
        );

        event.replyEmbeds(
                eb.build()
        ).queue(null, Sentry::captureException);

    }
}
