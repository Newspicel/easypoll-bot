package de.fbrettnich.easypoll.listener;

import de.fbrettnich.easypoll.core.Constants;
import de.fbrettnich.easypoll.EasyPoll;
import de.fbrettnich.easypoll.timertasks.GameStatus;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Timer;

public class ReadyListener extends ListenerAdapter {

    @Override
    public void onReady(ReadyEvent event) {
        Constants.BOT_ID = event.getJDA().getSelfUser().getId();
        new Timer().schedule(new GameStatus(event.getJDA()), 1000, 2*60*1000);
        System.out.println("[INFO|READY] EasyPoll (Shard #" + event.getJDA().getShardInfo().getShardId() + ") is running on " + event.getJDA().getGuilds().size() + " servers.");
        EasyPoll.registerSlashCommands();
    }
}
