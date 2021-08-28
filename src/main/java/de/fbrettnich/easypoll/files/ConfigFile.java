package de.fbrettnich.easypoll.files;

import io.sentry.Sentry;

import javax.inject.Singleton;
import java.io.*;
import java.util.Properties;

@Singleton
public class ConfigFile {

    private final Properties prop = new Properties();

    public ConfigFile() {
        createFile();
        loadFile();
    }

    /**
     * Create config file if not exists
     */
    private void createFile() {
        if(!fileExists()) {
            try (OutputStream output = new FileOutputStream("config.properties")) {

                Properties prop = new Properties();

                prop.setProperty("devmode", "true");
                prop.setProperty("bot.token", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
                prop.setProperty("bot.token_dev", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");

                prop.setProperty("sentry.url", "https://XXXXX.ingest.sentry.io/12345");

                prop.setProperty("mongodb.clienturi", "mongodb+srv://XXX:XXX@XXX.mongodb.net/test?retryWrites=true&w=majority1");
                prop.setProperty("mongodb.database", "DiscordBot");

                prop.setProperty("botlist.topgg.token", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
                prop.setProperty("botlist.discordbotlist.token", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
                prop.setProperty("botlist.discordbotsgg.token", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
                prop.setProperty("botlist.botsondiscordxyz.token", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");

                prop.store(output, null);

            } catch (IOException e) {
                Sentry.captureException(e);
            }
        }
    }

    /**
     * Load config file
     */
    private void loadFile() {
        try (InputStream input = new FileInputStream("config.properties")) {

            prop.load(input);

        } catch (IOException e) {
            Sentry.captureException(e);
        }
    }

    /**
     * Check if the config file exists
     *
     * @return true if config file exists, otherwise false
     */
    private boolean fileExists() {
        try {

            InputStream input = new FileInputStream("config.properties");
            return true;

        } catch (FileNotFoundException ignored) { }

        return false;
    }

    /**
     * Get a string based on the key
     *
     * @param key property key
     * @return property value
     */
    public String getString(String key) {
        return prop.getProperty(key);
    }
}
