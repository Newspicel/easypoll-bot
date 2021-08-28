package de.fbrettnich.easypoll.commands;

import de.fbrettnich.easypoll.core.Constants;
import de.fbrettnich.easypoll.language.GuildLanguage;
import de.fbrettnich.easypoll.language.GuildLanguageService;
import de.fbrettnich.easypoll.language.TranslationManager;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import net.dv8tion.jda.api.requests.ErrorResponse;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Singleton
public class SetupLanguageCommand {

    @Inject
    private GuildLanguageService guildLanguageService;

    @Inject
    private TranslationManager translationManager;

    public void run(@Nonnull SlashCommandEvent event, GuildLanguage gl) {

        event.deferReply().queue(null, Sentry::captureException);

        InteractionHook hook = event.getHook();
        Member member = event.getMember();

        if (member == null) return;


        if(
                !member.isOwner() &&
                !member.hasPermission(Permission.ADMINISTRATOR) &&
                !member.hasPermission(Permission.MANAGE_PERMISSIONS)
        )
        {

            EmbedBuilder eb = new EmbedBuilder();

            eb.setColor(Color.RED);
            eb.setTitle(guildLanguageService.getTranslation(gl, "errors.no_permissions.member.title"), Constants.WEBSITE_URL);
            eb.addField(
                    guildLanguageService.getTranslation(gl, "errors.no_permissions.member.field.title"),
                    "\u2022 ADMINISTRATOR *(Permission)*\n" +
                            "\u2022 MANAGE_PERMISSIONS *(Permission)*",
                    true);

            hook.sendMessageEmbeds(
                            eb.build()
                    )
                    .delay(30, TimeUnit.SECONDS)
                    .flatMap(Message::delete)
                    .queue(null, new ErrorHandler()
                            .ignore(ErrorResponse.UNKNOWN_MESSAGE)
                            .handle(Objects::nonNull, Sentry::captureException)
                    );

            return;
        }


        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle(guildLanguageService.getTranslation(gl, "commands.setup.language.change.title"), Constants.WEBSITE_URL);
        eb.setColor(Color.decode("#01FF70"));
        eb.setDescription(guildLanguageService.getTranslation(gl, "commands.setup.language.change.description"));


        SelectionMenu.Builder selectionMenuBuilder = SelectionMenu
                .create("ChangeLanguageMenu")
                .setPlaceholder(guildLanguageService.getTranslation(gl, "commands.setup.language.change.selectionmenu.placeholder"))
                .setMinValues(1)
                .setMaxValues(1);

        translationManager.getLanguages().forEach(lang -> selectionMenuBuilder.addOption(
                        translationManager.getTranslation(lang, "translation.name_local"),
                        lang,
                        translationManager.getTranslation(lang, "translation.name"),
                        Emoji.fromUnicode(translationManager.getTranslation(lang, "translation.flag_unicode"))
                )
        );

        hook.sendMessageEmbeds(
                        eb.build()
                )
                .addActionRow(
                        selectionMenuBuilder.build()
                )
                .queue(null, Sentry::captureException);

    }
}
