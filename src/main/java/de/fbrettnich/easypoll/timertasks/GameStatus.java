package de.fbrettnich.easypoll.timertasks;

import de.fbrettnich.easypoll.EasyPoll;
import de.fbrettnich.easypoll.utils.FormatUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.TimerTask;

@Singleton
public class GameStatus extends TimerTask {

    private final JDA jda;
    private int i = 0;

    public GameStatus(JDA jda) {
        this.jda = jda;
    }

    @Override
    public void run() {

        ArrayList<Activity> activities = new ArrayList<>();

        activities.add(Activity.listening("/poll"));
        activities.add(Activity.listening("/timepoll"));
        activities.add(Activity.listening("/easypoll"));
        activities.add(Activity.watching("www.easypoll.me"));
        activities.add(Activity.playing("easypoll.me | /help"));
        activities.add(Activity.streaming(
                "on " +
                        FormatUtil.decimalFormat(EasyPoll.getShardManager().getGuilds().size()) + " Servers | " +
                        FormatUtil.decimalFormat(EasyPoll.getShardManager().getGuilds().stream().mapToInt(Guild::getMemberCount).sum()) + " Users | " +
                        FormatUtil.decimalFormat(jda.getShardInfo().getShardTotal()) + " Shards",
                "https://www.twitch.tv/floxiii_")
        );

        if(i == activities.size()) i = 0;

        jda.getPresence().setActivity(activities.get(i));

        i++;
    }
}
