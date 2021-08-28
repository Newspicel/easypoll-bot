package de.fbrettnich.easypoll;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.fbrettnich.easypoll.core.Constants;
import de.fbrettnich.easypoll.files.ConfigFile;
import de.fbrettnich.easypoll.language.TranslationManager;
import de.fbrettnich.easypoll.listener.*;
import de.fbrettnich.easypoll.timertasks.BotListStats;
import de.fbrettnich.easypoll.timertasks.CloseTimedPolls;
import io.sentry.Sentry;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;

import java.util.Timer;

public class EasyPoll {

    private static ShardManager shardManager;

    /**
     * The main method to build and start the bot
     *
     * @param args unused
     * @throws LoginException Errors when logging in
     */
    public static void main(String[] args) throws LoginException {

        Injector injector = Guice.createInjector();

        ConfigFile config = injector.getInstance(ConfigFile.class);
        Constants.DEVMODE = Boolean.parseBoolean(config.getString("devmode"));

        Sentry.init(options -> {
            options.setDsn(config.getString("sentry.url"));
            options.setEnvironment(Constants.DEVMODE ? "development" : "production");
        });

        injector.getInstance(TranslationManager.class).loadTranslations("de-at", "de-de", "dk-dk", "en-us", "fr-fr", "it-it", "nl-nl", "pt-br", "zh-cn", "zh-tw", "lol-us", "es-es", "ru-ru", "sq-sq");


        DefaultShardManagerBuilder defaultShardManagerBuilder = DefaultShardManagerBuilder.createDefault(config.getString("bot.token"))

                .setAutoReconnect(true)
                .setShardsTotal(-1)
                .setChunkingFilter(ChunkingFilter.NONE)
                .setMemberCachePolicy(MemberCachePolicy.NONE)
                .disableCache(CacheFlag.EMOTE, CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE, CacheFlag.CLIENT_STATUS, CacheFlag.ROLE_TAGS)
                .setEnabledIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.DIRECT_MESSAGE_REACTIONS)

                .addEventListeners(new GuildJoinListener())
                .addEventListeners(new GuildLeaveListener())
                .addEventListeners(new MessageReactionAddListener())
                .addEventListeners(new MessageReceivedListener())
                .addEventListeners(new ReadyListener())
                .addEventListeners(injector.getInstance(SelectionMenuListener.class))
                .addEventListeners(injector.getInstance(SlashCommandListener.class))

                .setStatus(OnlineStatus.ONLINE);

        shardManager = defaultShardManagerBuilder.build();


        Timer timer = new Timer();
        timer.schedule(injector.getInstance(CloseTimedPolls.class), 5 * 60 * 1000, 3 * 1000);

        if(!Constants.DEVMODE) {
            timer.schedule(injector.getInstance(BotListStats.class), 5 * 60 * 1000, 5 * 60 * 1000);
        }

    }

    /**
     * Registering the Discord SlashCommands
     */
    public static void registerSlashCommands() {

        System.out.println("Register Commands");

        JDA jda = shardManager.getShardById(0);
        if(jda == null) return;

        jda.updateCommands()
                .addCommands(new CommandData("easypoll", "Learn more about EasyPoll and get help how to use the bot"))
                .addCommands(new CommandData("help", "Show the EasyPoll Bot Help"))
                .addCommands(new CommandData("vote", "Vote for the EasyPoll Bot"))
                .addCommands(new CommandData("invite", "Invite EasyPoll to your own Discord Server"))
                .addCommands(new CommandData("info", "Show some information about EasyPoll"))
                .addCommands(new CommandData("ping", "See the Ping of the Bot to the Discord Gateway"))
                .addCommands(
                        new CommandData("closepoll", "Close a poll so that no more votes are counted")
                                .addOption(OptionType.STRING, "pollid", "Poll ID", true)
                )
                .addCommands(
                        new CommandData("poll", "Create a normal poll")
                                .addOption(OptionType.STRING, "question", "What is the question?", true)
                                .addOption(OptionType.BOOLEAN, "allowmultiplechoices", "Are multiple choices allowed?")
                                .addOption(OptionType.STRING, "answer1", "Answer 1")
                                .addOption(OptionType.STRING, "answer2", "Answer 2")
                                .addOption(OptionType.STRING, "answer3", "Answer 3")
                                .addOption(OptionType.STRING, "answer4", "Answer 4")
                                .addOption(OptionType.STRING, "answer5", "Answer 5")
                                .addOption(OptionType.STRING, "answer6", "Answer 6")
                                .addOption(OptionType.STRING, "answer7", "Answer 7")
                                .addOption(OptionType.STRING, "answer8", "Answer 8")
                                .addOption(OptionType.STRING, "answer9", "Answer 9")
                                .addOption(OptionType.STRING, "answer10", "Answer 10")
                                .addOption(OptionType.STRING, "answer11", "Answer 11")
                                .addOption(OptionType.STRING, "answer12", "Answer 12")
                                .addOption(OptionType.STRING, "answer13", "Answer 13")
                                .addOption(OptionType.STRING, "answer14", "Answer 14")
                                .addOption(OptionType.STRING, "answer15", "Answer 15")
                                .addOption(OptionType.STRING, "answer16", "Answer 16")
                                .addOption(OptionType.STRING, "answer17", "Answer 17")
                                .addOption(OptionType.STRING, "answer18", "Answer 18")
                                .addOption(OptionType.STRING, "answer19", "Answer 19")
                                .addOption(OptionType.STRING, "answer20", "Answer 20")
                )
                .addCommands(
                        new CommandData("timepoll", "Create a timed poll with end date")
                                .addOption(OptionType.STRING, "question", "What is the question?", true)
                                .addOption(OptionType.STRING, "time", "How long should the poll run? (Minutes (m), Hours (h), Days (d) | Example: 3h, 2d, 1d3h5m | Max: 7d)", true)
                                .addOption(OptionType.BOOLEAN, "allowmultiplechoices", "Are multiple choices allowed?")
                                .addOption(OptionType.STRING, "answer1", "Answer 1")
                                .addOption(OptionType.STRING, "answer2", "Answer 2")
                                .addOption(OptionType.STRING, "answer3", "Answer 3")
                                .addOption(OptionType.STRING, "answer4", "Answer 4")
                                .addOption(OptionType.STRING, "answer5", "Answer 5")
                                .addOption(OptionType.STRING, "answer6", "Answer 6")
                                .addOption(OptionType.STRING, "answer7", "Answer 7")
                                .addOption(OptionType.STRING, "answer8", "Answer 8")
                                .addOption(OptionType.STRING, "answer9", "Answer 9")
                                .addOption(OptionType.STRING, "answer10", "Answer 10")
                                .addOption(OptionType.STRING, "answer11", "Answer 11")
                                .addOption(OptionType.STRING, "answer12", "Answer 12")
                                .addOption(OptionType.STRING, "answer13", "Answer 13")
                                .addOption(OptionType.STRING, "answer14", "Answer 14")
                                .addOption(OptionType.STRING, "answer15", "Answer 15")
                                .addOption(OptionType.STRING, "answer16", "Answer 16")
                                .addOption(OptionType.STRING, "answer17", "Answer 17")
                                .addOption(OptionType.STRING, "answer18", "Answer 18")
                                .addOption(OptionType.STRING, "answer19", "Answer 19")
                                .addOption(OptionType.STRING, "answer20", "Answer 20")
                )
                .addCommands(
                        new CommandData("setup", "Bot setup and settings")
                                .addSubcommands(
                                        new SubcommandData("language", "Change the guild language")
                                )
                                .addSubcommands(
                                        new SubcommandData("permissions", "Check required bot permissions")
                                )
                )
                .queue(null, Sentry::captureException);
    }

    /**
     * Get the ShardManager
     *
     * @return ShardManager
     */
    public static ShardManager getShardManager() {
        return shardManager;
    }

}
