package de.fbrettnich.easypoll.language;

import io.sentry.Sentry;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

@Singleton
public class TranslationManager {

    private final ArrayList<String> languages = new ArrayList<>();
    private final HashMap<String, String> translations = new HashMap<>();

    public TranslationManager() { }

    /**
     * Load multiple translations
     *
     * @param countryCodes country code list
     */
    public void loadTranslations(String... countryCodes) {
        for (String countryCode : countryCodes) {
            loadTranslation(countryCode);
        }
        languages.sort(String::compareTo);
    }

    /**
     * Load a translation
     *
     * @param countryCode country code
     */
    public void loadTranslation(String countryCode) {
        languages.add(countryCode);

        JSONParser parser = new JSONParser();
        InputStream inputStream = TranslationManager.class.getResourceAsStream("/translations/" + countryCode + ".json");

        try {
            Object obj = parser.parse(inputStreamToString(inputStream));
            JSONObject jsonObject = (JSONObject) obj;
            jsonObject.forEach((key, value) -> addTranslation(countryCode, "", key, value));
        } catch (Exception e) {
            Sentry.captureException(e);
        }
    }

    /**
     * Read an InputStream and convert it to a String
     *
     * @param stream {@link InputStream}
     * @return {@link InputStream} as {@link String}
     */
    private String inputStreamToString(InputStream stream) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader isr = new InputStreamReader(stream, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);

        String read;
        while((read = br.readLine()) != null) {
            sb.append(read);
        }
        br.close();

        return sb.toString();
    }

    /**
     * Load a translation path
     *
     * @param lang country code
     * @param path translation path
     * @param key translation path key
     * @param obj translation object
     */
    private void addTranslation(String lang, String path, Object key, Object obj) {
        String pathKey = path + "." + key;
        if(obj instanceof JSONObject) {
            ((JSONObject) obj).forEach((o, o2) -> addTranslation(lang, pathKey, o, o2));
        }else{
            String k = lang + ":" + pathKey;
            k = k.replaceFirst("\\.", "");
            translations.put(k, (String) obj);
        }
    }

    /**
     * Get a translation
     *
     * @param lang country code
     * @param key translation path key
     * @return translation
     */
    public String getTranslation(String lang, String key) {
        if (translations.containsKey(lang + ":" + key)) {
            return translations.get(lang + ":" + key);
        }else {
            return translations.getOrDefault("en:" + key, "[Empty translation " +lang+":"+ key + "]");
        }
    }

    /**
     * Get loaded languages code list
     *
     * @return languages
     */
    public ArrayList<String> getLanguages() {
        return languages;
    }
}
