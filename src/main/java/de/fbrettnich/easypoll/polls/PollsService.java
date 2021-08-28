package de.fbrettnich.easypoll.polls;

import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import de.fbrettnich.easypoll.core.Constants;
import de.fbrettnich.easypoll.language.GuildLanguage;
import de.fbrettnich.easypoll.language.GuildLanguageService;
import de.fbrettnich.easypoll.utils.enums.PollType;
import io.reactivex.rxjava3.core.Flowable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.UUID;

@Singleton
public class PollsService {

    @Inject
    private PollsRepository pollRepository;

    public Flowable<UpdateResult> checkPollClose() {
        return pollRepository.checkTimedPolls().flatMap(this::closePoll);
    }

    public Flowable<UpdateResult> closePollByPollId(UUID pollId) {
        return pollRepository.findPollByPollId(pollId).flatMap(this::closePoll);
    }

    public Flowable<UpdateResult> closePollByMessageId(String messageId) {
        return pollRepository.findPollByMessageId(messageId).flatMap(this::closePoll);
    }

    public Flowable<UpdateResult> closePoll(Poll poll) {
        poll.setClosed(System.currentTimeMillis());
        poll.setActive(false);
        return pollRepository.updateOne(poll);
    }

    public Flowable<InsertOneResult> insertPoll(Poll poll){
        return pollRepository.insert(poll);
    }

    public MessageEmbed getPollEmbed(Poll poll, List<MessageReaction> messageReactions, GuildLanguage gl, GuildLanguageService guildLanguageService) {

        EmbedBuilder eb = new EmbedBuilder();
        StringBuilder description = new StringBuilder();

        description
                .append("**")
                .append(guildLanguageService.getTranslation(gl, "polls.question"))
                .append("**\n")
                .append(poll.getQuestion())
                .append("\n")
                .append("\n")
                .append("**")
                .append(guildLanguageService.getTranslation(gl, "polls.choices"))
                .append("**\n");

        if (poll.getPollType() == PollType.DEFAULT_UPDOWN || poll.getPollType() == PollType.TIME_UPDOWN) {
            eb.setColor(Constants.COLOR_POLL_UPDOWN);
            description
                    .append(":thumbsup: ")
                    .append(guildLanguageService.getTranslation(gl, "polls.yes"))
                    .append("\n")
                    .append(":thumbsdown: ")
                    .append(guildLanguageService.getTranslation(gl, "polls.no"))
                    .append("\n");
        } else {
            poll.getChoices().forEach(choices -> description
                    .append(choices.reaction())
                    .append(" ")
                    .append(choices.content())
                    .append("\n"));

            if (poll.isMultipleChoices()) {
                eb.setColor(Constants.COLOR_POLL_CUSTOM_MULTI);
            } else {
                eb.setColor(Constants.COLOR_POLL_CUSTOM_SINGEL);
            }
        }

        if(poll.isActive() && messageReactions != null) {

            double allReactionsCount = messageReactions.stream().mapToInt(MessageReaction::getCount).sum() - messageReactions.size();

            if(messageReactions.size() >= poll.getChoices().size()) {

                description
                        .append("\n**")
                        .append(guildLanguageService.getTranslation(gl, "polls.finalresult"))
                        .append("**\n");

                for (int i = 0; i < poll.getChoices().size(); i++) {

                    int reactionCount = messageReactions.get(i).getCount() - 1;

                    double percentage = (reactionCount / allReactionsCount) * 100;
                    if (reactionCount == 0 || allReactionsCount == 0) percentage = 0;

                    description
                            .append(poll.getChoices().get(i).reaction())
                            .append(" ")
                            .append(getProgressbar(percentage))
                            .append(" [").append(reactionCount)
                            .append(" • ")
                            .append(String.format("%.1f", percentage))
                            .append("%]\n");
                }
            }
        }

        if (poll.getPollType() == PollType.TIME_UPDOWN || poll.getPollType() == PollType.TIME_MULTI) {
            if(!poll.isActive()) {
                description.
                        append("\n:alarm_clock: ")
                        .append(guildLanguageService.getTranslation(gl, "polls.alreadyended"));
            }else{
                description
                        .append("\n:alarm_clock: ")
                        .append(guildLanguageService.getTranslation(gl, "polls.ends"))
                        .append(" ")
                        .append("<t:")
                        .append(poll.getEndTime() / 1000L)
                        .append(":R>");
            }
        }

        if (poll.isMultipleChoices() ) {
            description
                    .append("\n:white_check_mark: ")
                    .append(guildLanguageService.getTranslation(gl, "polls.multiplechoice.allowed"));
        } else {
            description
                    .append("\n:no_entry: ")
                    .append(guildLanguageService.getTranslation(gl, "polls.multiplechoice.disallowed"));
        }

        if(!poll.isActive()) {
            eb.setColor(Constants.COLOR_POLL_CLOSED);
            description
                    .append("\n:lock: ")
                    .append(guildLanguageService.getTranslation(gl, "polls.noothervotes"));
        }

        eb.setDescription(description);

        eb.setFooter(guildLanguageService.getTranslation(gl, "polls.pollid", poll.getPollId().toString()));

        return eb.build();
    }

    /**
     * Convert the percentage to progress bar
     *
     * @param percentage The percentage
     * @return The progress bar
     */
    private String getProgressbar(double percentage) {

        int chars = 10;
        int filled = (int) Math.round(percentage / chars);
        int empty = chars - filled;

        return "▓".repeat(Math.max(0, filled)) +
                "░".repeat(Math.max(0, empty));
    }
}
