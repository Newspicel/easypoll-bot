package de.fbrettnich.easypoll.selectionmenus;

import de.fbrettnich.easypoll.core.Constants;
import de.fbrettnich.easypoll.language.GuildLanguage;
import de.fbrettnich.easypoll.language.GuildLanguageService;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.MissingAccessException;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.requests.ErrorResponse;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Singleton
public class ChangeLanguageMenu {

    @Inject
    private GuildLanguageService guildLanguageService;

    public void run(@Nonnull SelectionMenuEvent event, GuildLanguage gl) {

        event.deferReply().queue(null, Sentry::captureException);

        InteractionHook hook = event.getHook();
        User user = event.getUser();
        Member member = event.getMember();
        Message message = event.getMessage();
        List<SelectOption> selectOptions = event.getSelectedOptions();

        if(member == null) return;
        if(selectOptions == null) return;

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

        if(message != null) {
            try {
                message.delete().queue(null, Sentry::captureException);
            }catch (MissingAccessException ignored) {}
        }

        String lang = Constants.DEFAULT_LANGUAGE;
        if(!selectOptions.isEmpty()) {
            lang = selectOptions.get(0).getValue();
        }

        gl.setLanguage(lang);

        guildLanguageService.setGuildLanguage(gl).subscribe(o -> {
            EmbedBuilder eb = new EmbedBuilder();

            eb.setTitle(guildLanguageService.getTranslation(gl, "commands.setup.language.success.title"), Constants.WEBSITE_URL);
            eb.setColor(Color.decode("#01FF70"));
            eb.setDescription(guildLanguageService.getTranslation(gl, "commands.setup.language.success.description", guildLanguageService.getTranslation(gl, "translation.name_local")));
            eb.setFooter(guildLanguageService.getTranslation(gl, "commands.setup.language.success.footer", (user.getName() + "#" + user.getDiscriminator())));

            hook.sendMessageEmbeds(eb.build()).queue(null, Sentry::captureException);
        });
    }
}
