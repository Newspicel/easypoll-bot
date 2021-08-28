package de.fbrettnich.easypoll.language;

import de.fbrettnich.easypoll.core.Constants;
import io.reactivex.rxjava3.core.Flowable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;

@Singleton
public class GuildLanguageService {

    private final HashMap<String, String> guildLanguageCache = new HashMap<>();

    @Inject
    private GuildLanguageRepository guildLanguageRepository;

    @Inject
    private TranslationManager translationManager;

    public String getTranslation(String guildLanguage, String key, String... placeholders){
        String translation = translationManager.getTranslation(guildLanguage, key);
        for (String s : placeholders) {
            translation = translation.replaceFirst("%s", s);
        }

        return translation;
    }

    public String getTranslation(GuildLanguage guildLanguage, String key, String... placeholders){
        return getTranslation(guildLanguage.getLanguage(), key, placeholders);
    }

    public Flowable<GuildLanguage> getGuildLanguage(String guildId){
        if (guildLanguageCache.containsKey(guildId)){
            return Flowable.just(new GuildLanguage(guildId, guildLanguageCache.get(guildId)));
        }
        return guildLanguageRepository.isExists(guildId).flatMap(aBoolean -> {
            if (aBoolean){
                return guildLanguageRepository.getGuildLanguage(guildId).doOnNext(guildLanguage -> guildLanguageCache.put(guildId, guildLanguage.getLanguage()));
            } else {
                return Flowable.just(new GuildLanguage(guildId, Constants.DEFAULT_LANGUAGE));
            }
        });
    }

    public Flowable<Object> setGuildLanguage(GuildLanguage guildLanguage){
        return guildLanguageRepository.isExists(guildLanguage.getGuildId()).flatMap(aBoolean -> aBoolean ? guildLanguageRepository.update(guildLanguage) : guildLanguageRepository.insert(guildLanguage));
    }

}
