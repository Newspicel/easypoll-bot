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
public class HelpCommand {
    
    @Inject
    private GuildLanguageService guildLanguageService;

    public void run(@Nonnull SlashCommandEvent event, GuildLanguage gl) {

        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle(guildLanguageService.getTranslation(gl, "commands.help.title"), Constants.WEBSITE_URL);
        eb.setColor(Color.decode("#FDA50F"));

        eb.addField(
                guildLanguageService.getTranslation(gl, "commands.help.fields.poll_commands.title"),
                "**/poll** \u2022 " + guildLanguageService.getTranslation(gl, "commands.help.fields.poll_commands.commands.poll") + "\n" +
                        "**/timepoll** \u2022 " + guildLanguageService.getTranslation(gl, "commands.help.fields.poll_commands.commands.timepoll") + "\n" +
                        "**/closepoll** \u2022 " + guildLanguageService.getTranslation(gl, "commands.help.fields.poll_commands.commands.closepoll"),
                false
        );

        eb.addField(
                guildLanguageService.getTranslation(gl, "commands.help.fields.public_commands.title"),
                "**/help** \u2022 " + guildLanguageService.getTranslation(gl, "commands.help.fields.public_commands.commands.help") + "\n" +
                        "**/vote** \u2022 " + guildLanguageService.getTranslation(gl, "commands.help.fields.public_commands.commands.vote") + "\n" +
                        "**/invite** \u2022 " + guildLanguageService.getTranslation(gl, "commands.help.fields.public_commands.commands.invite") + "\n" +
                        "**/info** \u2022 " + guildLanguageService.getTranslation(gl, "commands.help.fields.public_commands.commands.info") + "\n" +
                        "**/ping** \u2022 " + guildLanguageService.getTranslation(gl, "commands.help.fields.public_commands.commands.ping"),
                false
        );

        event.replyEmbeds(
                eb.build()
        ).queue(null, Sentry::captureException);

    }
}
