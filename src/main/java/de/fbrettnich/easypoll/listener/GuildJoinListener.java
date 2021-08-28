package de.fbrettnich.easypoll.listener;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildJoinListener extends ListenerAdapter {

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        System.out.println("[INFO|GUILD] Bot entered " + event.getGuild().getId() + " ( " + event.getGuild().getName() + " )");
    }
}
