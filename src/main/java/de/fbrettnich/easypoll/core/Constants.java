package de.fbrettnich.easypoll.core;

import java.awt.*;

public class Constants {

    public static boolean DEVMODE = true;

    public static final String VERSION = "4.0.0";
    public static final String DEFAULT_LANGUAGE = "en-us";

    public static String BOT_ID = "";
    public static final String BOT_OWNER_MENTION = "<@850045707917328474> & <@231091710195662848>";

    public static final String WEBSITE_URL = "https://easypoll.me/?utm_source=discordbot&utm_medium=website&utm_campaign=easypoll";
    public static final String INVITE_URL = "https://discord.com/oauth2/authorize?client_id=437618149505105920&permissions=355392&redirect_uri=https%3A%2F%2Feasypoll.me%2Fdiscord&response_type=code&scope=bot%20applications.commands";
    public static final String VOTE_URL = "https://easypoll.me/vote";

    public static final Color COLOR_POLL_UPDOWN = new Color(0, 255, 255);
    public static final Color COLOR_POLL_CUSTOM_SINGEL = new Color(0, 255, 254);
    public static final Color COLOR_POLL_CUSTOM_MULTI = new Color(0, 255, 253);
    public static final Color COLOR_POLL_CLOSED = new Color(250, 38, 38);

    public static final long STARTUP = System.currentTimeMillis();

}
