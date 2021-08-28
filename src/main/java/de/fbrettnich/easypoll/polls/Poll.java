package de.fbrettnich.easypoll.polls;

import de.fbrettnich.easypoll.utils.enums.PollType;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Poll {

    private final UUID pollId;
    private final String guildId;
    private final String channelId;
    private String messageId;
    private final String authorId;
    private final String question;
    private final List<Choices> choices;
    private final PollType pollType;
    private final boolean multipleChoices;
    private final long created;
    private final long endTime;
    private long closed = -1;
    private boolean active = true;


    public record Choices(String reaction, String content) {
    }

}
