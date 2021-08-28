package de.fbrettnich.easypoll.listener;

import com.google.inject.Injector;
import de.fbrettnich.easypoll.commands.*;
import de.fbrettnich.easypoll.language.GuildLanguageService;
import de.fbrettnich.easypoll.polls.PollController;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SlashCommandListener  extends ListenerAdapter {

    @Inject
    private GuildLanguageService guildLanguageManager;

    @Inject
    private Injector injector;

    @Override
    public void onSlashCommand(SlashCommandEvent event) {

        event.deferReply().queue(null, Sentry::captureException);

        if(event.getGuild() == null) return;

        System.out.println("Test");

        guildLanguageManager.getGuildLanguage(event.getGuild().getId()).subscribe(gl -> {
            String commandName = event.getName();
            String subCommandName = event.getSubcommandName();

            switch (commandName) {
                case "help", "easypoll" -> injector.getInstance(HelpCommand.class).run(event, gl);
                case "info" -> injector.getInstance(InfoCommand.class).run(event, gl);
                case "invite" -> injector.getInstance(InviteCommand.class).run(event, gl);
                case "ping" -> injector.getInstance(PingCommand.class).run(event, gl);
                case "vote" -> injector.getInstance(VoteCommand.class).run(event, gl);

                case "closepoll" -> injector.getInstance(PollController.class).runClosePollCommand(event, gl);
                case "poll" -> injector.getInstance(PollController.class).runPollCreateCommand(event, gl, false);
                case "timepoll" -> injector.getInstance(PollController.class).runPollCreateCommand(event, gl, true);

                case "setup" -> {
                    if (subCommandName == null) break;
                    switch (subCommandName) {
                        case "language" ->  injector.getInstance(SetupLanguageCommand.class).run(event, gl);
                        case "permissions" -> injector.getInstance(SetupPermissionsCommand.class).run(event, gl);
                    }
                }
                default -> {
                    event.reply("Sorry! I cannot process this command.").queue(null, Sentry::captureException);
                    Sentry.captureMessage("Cannot process command: " + commandName, SentryLevel.ERROR);
                }
            }
        });
    }
}
