package de.fbrettnich.easypoll.language;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GuildLanguage {

    private final String guildId;
    private String language;


}
