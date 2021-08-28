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
public class InviteCommand {

    @Inject
    private GuildLanguageService guildLanguageService;

    public void run(@Nonnull SlashCommandEvent event, GuildLanguage gl) {

        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle(guildLanguageService.getTranslation(gl, "commands.invite.title"), Constants.INVITE_URL);
        eb.setColor(Color.decode("#5865F2"));
        eb.setDescription(guildLanguageService.getTranslation(gl, "commands.invite.description", event.getJDA().getSelfUser().getAsMention(), Constants.INVITE_URL));

        event.replyEmbeds(
                eb.build()
        ).queue(null, Sentry::captureException);

    }
}
