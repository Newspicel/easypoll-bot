package de.fbrettnich.easypoll.listener;

import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildLeaveListener extends ListenerAdapter {

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        System.out.println("[INFO|GUILD] Bot left " + event.getGuild().getId() + " ( " + event.getGuild().getName() + " )");
    }
}
