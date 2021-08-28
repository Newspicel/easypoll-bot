package de.fbrettnich.easypoll.listener;

import de.fbrettnich.easypoll.language.GuildLanguageService;
import de.fbrettnich.easypoll.selectionmenus.ChangeLanguageMenu;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SelectionMenuListener extends ListenerAdapter {

    @Inject
    private GuildLanguageService guildLanguageService;

    @Inject
    private ChangeLanguageMenu changeLanguageMenu;

    @Override
    public void onSelectionMenu(SelectionMenuEvent event) {

        if(event.getGuild() == null) return;

        guildLanguageService.getGuildLanguage(event.getGuild().getId()).subscribe(gl -> {
            String componentId = event.getComponentId();

            switch (componentId) {
                case "ChangeLanguageMenu" -> changeLanguageMenu.run(event, gl);
                default -> {
                    event.reply("Sorry! I cannot process this selection.").queue(null, Sentry::captureException);
                    Sentry.captureMessage("Cannot process selection: " + componentId, SentryLevel.ERROR);
                }
            }
        });
    }
}
