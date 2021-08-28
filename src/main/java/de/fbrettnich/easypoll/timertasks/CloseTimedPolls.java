package de.fbrettnich.easypoll.timertasks;

import de.fbrettnich.easypoll.polls.PollsService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.TimerTask;

@Singleton
public class CloseTimedPolls extends TimerTask {

    @Inject
    private PollsService pollsService;

    @Override
    public void run() {

        pollsService.checkPollClose().subscribe();

    }
}
