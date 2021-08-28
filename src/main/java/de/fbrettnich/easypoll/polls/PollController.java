package de.fbrettnich.easypoll.polls;

import com.vdurmont.emoji.EmojiManager;
import de.fbrettnich.easypoll.core.Constants;
import de.fbrettnich.easypoll.language.GuildLanguage;
import de.fbrettnich.easypoll.language.GuildLanguageService;
import de.fbrettnich.easypoll.utils.Permissions;
import de.fbrettnich.easypoll.utils.enums.PollType;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class PollController {

    @Inject
    private PollsService pollsService;

    @Inject
    private GuildLanguageService guildLanguageService;

    public void runPollCreateCommand(SlashCommandEvent event, GuildLanguage guildLanguage, boolean isTimePoll) {

        InteractionHook hook = event.getHook();
        Guild guild = event.getGuild();
        User user = event.getUser();
        Member member = event.getMember();

        if (guild == null) return;
        if (member == null) return;

        if (
                !member.isOwner() &&
                        !member.hasPermission(Permission.ADMINISTRATOR) &&
                        !member.hasPermission(Permission.MANAGE_PERMISSIONS) &&
                        !Permissions.hasPollCreatorRole(event.getMember()) &&
                        !event.getChannel().getName().toLowerCase().contains("easypoll") &&
                        !(event.getTextChannel().getTopic() != null && event.getTextChannel().getTopic().toLowerCase().contains("easypoll"))
        ) {

            EmbedBuilder eb = new EmbedBuilder();

            eb.setColor(Color.RED);
            eb.setTitle(guildLanguageService.getTranslation(guildLanguage, "errors.no_permissions.member.title"), Constants.WEBSITE_URL);
            eb.addField(
                    guildLanguageService.getTranslation(guildLanguage, "errors.no_permissions.member.field.title"),
                    """
                            \u2022 ADMINISTRATOR *(Permission)*
                            \u2022 MANAGE_PERMISSIONS *(Permission)*
                            \u2022 PollCreator *(Role)*
                            \u2022 EasyPoll *(in Channel Name)*
                            \u2022 EasyPoll *(in Channel Topic)*""",
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


        PollType pollType;
        long endTime = -1;

        List<OptionMapping> optionDataList = event.getOptions();
        OptionMapping optionDataQuestion = event.getOption("question");

        if (optionDataQuestion == null) return;

        String question = optionDataQuestion.getAsString();
        question = question.substring(0, Math.min(question.length(), 1950));

        if (isTimePoll) {
            String time = Objects.requireNonNull(event.getOption("time")).getAsString();
            time = time
                    .replace(" ", "")
                    .replace("/", "");

            time = time
                    .replace("s", "s/")
                    .replace("m", "m/")
                    .replace("h", "h/")
                    .replace("d", "d/")
                    .replace("w", "w/");

            String[] split = time.split("/");
            AtomicLong timeResult = new AtomicLong();
            AtomicBoolean error = new AtomicBoolean(false);

            Arrays.stream(split).forEach(timecode -> {
                long multiplier = 0L;

                if (timecode.endsWith("s")) {
                    multiplier = 1L;
                } else if (timecode.endsWith("m")) {
                    multiplier = 60L;
                } else if (timecode.endsWith("h")) {
                    multiplier = 60 * 60L;
                } else if (timecode.endsWith("d")) {
                    multiplier = 24 * 60 * 60L;
                } else if (timecode.endsWith("w")) {
                    multiplier = 7 * 24 * 60 * 60L;
                }

                String timeString = timecode.substring(0, timecode.length() - 1);
                if (timeString.matches("[0-9]+")) {
                    timeResult.getAndAdd(Long.parseLong(timeString) * multiplier);
                } else {
                    error.set(true);
                }

            });

            if (error.get()) {

                EmbedBuilder eb = new EmbedBuilder();

                eb.setColor(Color.ORANGE);
                eb.setTitle(guildLanguageService.getTranslation(guildLanguage, "commands.poll.invalid_time.title"), Constants.WEBSITE_URL);
                eb.addField(
                        guildLanguageService.getTranslation(guildLanguage, "commands.poll.invalid_time.field.title"),
                        guildLanguageService.getTranslation(guildLanguage, "commands.poll.invalid_time.field.description"),
                        true);

                hook.sendMessageEmbeds(
                        eb.build()
                ).queue(null, Sentry::captureException);

                return;
            }

            long totalTime = timeResult.get() * 1000L;
            if (totalTime > 7 * 24 * 60 * 60 * 1000L) totalTime = 7 * 24 * 60 * 60 * 1000L;
            endTime = System.currentTimeMillis() + totalTime + 1000L;
        }

        if (optionDataList.stream().map(OptionMapping::getName).noneMatch(s -> s.startsWith("answer"))) {

            pollType = isTimePoll ? PollType.TIME_UPDOWN : PollType.DEFAULT_UPDOWN;


            Poll poll = new Poll(
                    UUID.randomUUID(),
                    guild.getId(),
                    event.getChannel().getId(),
                    user.getId(),
                    question,
                    Arrays.asList(new Poll.Choices(":thumbsup:", "Yes"), new Poll.Choices(":thumbsdown:", "No")),
                    pollType,
                    false,
                    System.currentTimeMillis(),
                    endTime
            );

            Message message = getPollMessage(guildLanguage, hook, poll);

            if (message == null) return;

            try {
                message.addReaction("\uD83D\uDC4D").queue(null, Sentry::captureException); // 👍 :thumbsup:
                message.addReaction("\uD83D\uDC4E").queue(null, Sentry::captureException); // 👎 :thumbsdown:
            } catch (InsufficientPermissionException e) {
                sendNoPermissionMessage(guildLanguage, hook);
            }

        } else {

            pollType = isTimePoll ? PollType.TIME_MULTI : PollType.DEFAULT_MULTI;

            ArrayList<String> reactionsListAdd = new ArrayList<>();
            ArrayList<Poll.Choices> choicesList = new ArrayList<>();
            OptionMapping optionDataAllowmultiplechoices = event.getOption("allowmultiplechoices");
            boolean allowmultiplechoices = (optionDataAllowmultiplechoices != null && optionDataAllowmultiplechoices.getAsBoolean());

            int choiceCount = 0;
            for (OptionMapping optionData : optionDataList) {

                if (!optionData.getName().startsWith("answer")) continue;

                String answer = optionData.getAsString();
                if (answer.startsWith(" ")) answer = answer.replaceFirst(" ", "");

                String[] partSplit = answer.split(" ");

                Pattern pattern = Pattern.compile("^<a?:([a-zA-Z0-9_]+):([0-9]+)>$");
                Matcher matcher = pattern.matcher(partSplit[0]);

                if (matcher.find()) {
                    reactionsListAdd.add(matcher.group(1) + ":" + matcher.group(2));
                    choicesList.add(new Poll.Choices(partSplit[0], answer.replace(partSplit[0], "")));
                } else if (EmojiManager.isEmoji(partSplit[0].replace("️", ""))) { // Replaces an empty character, which prevents the isEmoji detection
                    reactionsListAdd.add(partSplit[0]);
                    choicesList.add(new Poll.Choices(partSplit[0], answer.replace(partSplit[0], "")));
                } else {
                    String charReaction = String.copyValueOf(Character.toChars("\uD83C\uDDE6".codePointAt(0) + choiceCount));
                    reactionsListAdd.add(charReaction);
                    choicesList.add(new Poll.Choices(charReaction, answer));
                }
                choiceCount++;
            }

            Poll poll = new Poll(
                    UUID.randomUUID(),
                    guild.getId(),
                    event.getChannel().getId(),
                    user.getId(),
                    question,
                    choicesList,
                    pollType,
                    allowmultiplechoices,
                    System.currentTimeMillis(),
                    endTime
            );

            Message message = getPollMessage(guildLanguage, hook, poll);
            if (message == null) return;

            for (String reaction : reactionsListAdd) {
                try {

                    message.addReaction(reaction).complete();

                } catch (ErrorResponseException e) {
                    if (e.getErrorResponse() == ErrorResponse.UNKNOWN_EMOJI) {

                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setColor(Color.ORANGE);
                        eb.setTitle(guildLanguageService.getTranslation(guildLanguage, "commands.poll.unknown_emoji.title"), Constants.WEBSITE_URL);
                        eb.addField(
                                guildLanguageService.getTranslation(guildLanguage, "commands.poll.unknown_emoji.field.title"),
                                guildLanguageService.getTranslation(guildLanguage, "commands.poll.unknown_emoji.field.description", (isTimePoll ? "/timepoll" : "/poll")),
                                true);

                        message.clearReactions().queue(null, Sentry::captureException);
                        message.editMessageEmbeds(eb.build()).queue(null, Sentry::captureException);

                        break;

                    } else if (e.getErrorResponse() == ErrorResponse.MISSING_PERMISSIONS) {

                        sendNoPermissionMessage(guildLanguage, hook);

                    } else {
                        Sentry.captureException(e);
                    }
                } catch (InsufficientPermissionException e) {

                    sendNoPermissionMessage(guildLanguage, hook);

                    break;
                }
            }
        }
    }
    
    public void runClosePollCommand(SlashCommandEvent event, GuildLanguage guildLanguage){
        event.deferReply().queue(null, Sentry::captureException);

        InteractionHook hook = event.getHook();
        Member member = event.getMember();

        if(member == null) return;

        UUID pollId = UUID.fromString(Objects.requireNonNull(event.getOption("pollid")).getAsString());

        if(
                !member.isOwner() &&
                        !member.hasPermission(Permission.ADMINISTRATOR) &&
                        !member.hasPermission(Permission.MANAGE_PERMISSIONS) &&
                        !Permissions.hasPollCreatorRole(member) //&&
                        //TODO !pm.getPollCreatorIdByPollId(pollId).equals(member.getId())
        )
        {

            EmbedBuilder eb = new EmbedBuilder();

            eb.setColor(Color.RED);
            eb.setTitle(guildLanguageService.getTranslation(guildLanguage, "errors.no_permissions.member.title"), Constants.WEBSITE_URL);
            eb.addField(
                    guildLanguageService.getTranslation(guildLanguage, "errors.no_permissions.member.field.title"),
                    """
                            \u2022 ADMINISTRATOR *(Permission)*
                            \u2022 MANAGE_PERMISSIONS *(Permission)*
                            \u2022 PollCreator *(Role)*
                            \u2022 Creator of this Poll""",
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

        pollsService.closePollByPollId(pollId).subscribe(updateResult -> {
            eb.setTitle(guildLanguageService.getTranslation(guildLanguage, "commands.closepoll.success.title"), Constants.WEBSITE_URL);
            eb.setColor(Color.decode("#01FF70"));
            eb.setDescription(guildLanguageService.getTranslation(guildLanguage, "commands.closepoll.success.description", pollId.toString()));
        });

        hook.sendMessageEmbeds(
                        eb.build()
                )
                .delay(30, TimeUnit.SECONDS)
                .flatMap(Message::delete)
                .queue(null, new ErrorHandler()
                        .ignore(ErrorResponse.UNKNOWN_MESSAGE)
                        .handle(Objects::nonNull, Sentry::captureException)
                );
    }

    @Nullable
    private Message getPollMessage(GuildLanguage guildLanguage, InteractionHook hook, Poll poll) {
        Message message = null;
        try {
            message = hook.sendMessageEmbeds(pollsService.getPollEmbed(poll, null, guildLanguage, guildLanguageService)).complete();
        } catch (Exception e) {
            Sentry.captureException(e);
        }

        if (message == null) return null;

        poll.setMessageId(message.getId());

        pollsService.insertPoll(poll).subscribe();
        return message;
    }

    private void sendNoPermissionMessage(GuildLanguage guildLanguage, InteractionHook hook) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setColor(Color.RED);
        eb.setTitle(guildLanguageService.getTranslation(guildLanguage, "errors.no_permissions.bot.title"));
        eb.addField(guildLanguageService.getTranslation(guildLanguage, "errors.no_permissions.bot.field.title"),
                "MESSAGE_WRITE, MESSAGE_MANAGE, MESSAGE_EMBED_LINKS, MESSAGE_HISTORY, MESSAGE_ADD_REACTION, MESSAGE_EXT_EMOJI",
                true);

        hook.sendMessageEmbeds(
                eb.build()
        ).queue(null, Sentry::captureException);
    }
    
    
}
