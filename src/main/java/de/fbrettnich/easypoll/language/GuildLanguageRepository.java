package de.fbrettnich.easypoll.language;

import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.MongoCollection;
import de.fbrettnich.easypoll.database.MongoDB;
import de.fbrettnich.easypoll.database.MongoDBRepository;
import io.reactivex.rxjava3.core.Flowable;
import org.bson.Document;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GuildLanguageRepository implements MongoDBRepository<GuildLanguage> {

    private final MongoCollection<Document> documents;

    @Inject
    public GuildLanguageRepository(MongoDB mongoDB) {
        this.documents = mongoDB.getCollection("guilds");
    }

    public Flowable<InsertOneResult> insert(GuildLanguage guildLanguage) {
        return Flowable.fromPublisher(documents.insertOne(serialize(guildLanguage)));
    }

    public Flowable<UpdateResult> update(GuildLanguage guildLanguage) {
        return Flowable.fromPublisher(documents.replaceOne(Filters.eq("guildId", guildLanguage.getGuildId()), serialize(guildLanguage)));
    }

    public Flowable<GuildLanguage> getGuildLanguage(String guildId) {
        return Flowable.fromPublisher(documents.find(Filters.eq("guildId", guildId))).map(this::deserialize);
    }

    public Flowable<Boolean> isExists(String guildId){
        return Flowable.fromPublisher(documents.countDocuments(Filters.eq("guildId", guildId))).map(aLong -> aLong != 0);
    }


    public Document serialize(GuildLanguage guildLanguage) {
        Document document = new Document();
        document.put("guildId", guildLanguage.getGuildId());
        document.put("language", guildLanguage.getLanguage());
        return document;
    }

    @Override
    public GuildLanguage deserialize(Document bson) {
        return new GuildLanguage(
                bson.getString("guildId"),
                bson.getString("language")
        );
    }
}
