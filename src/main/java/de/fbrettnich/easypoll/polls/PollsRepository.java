package de.fbrettnich.easypoll.polls;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.MongoCollection;
import de.fbrettnich.easypoll.database.MongoDB;
import de.fbrettnich.easypoll.database.MongoDBRepository;
import de.fbrettnich.easypoll.utils.enums.PollType;
import io.reactivex.rxjava3.core.Flowable;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public class PollsRepository implements MongoDBRepository<Poll> {

    @Getter
    private final MongoCollection<Document> documents;

    @Inject
    public PollsRepository(MongoDB mongoDB) {
        this.documents = mongoDB.getCollection("polls");
    }


    //inserts a new poll into the database
    public Flowable<InsertOneResult> insert(Poll poll) {
        return Flowable.fromPublisher(documents.insertOne(serialize(poll)));
    }

    //updates a poll in the database
    public Flowable<UpdateResult> updateOne(Poll poll) {
        return Flowable.fromPublisher(documents.replaceOne(Filters.eq("pollId", poll.getPollId().toString()), serialize(poll)));
    }


    public Flowable<Poll> findPollByPollId(UUID pollId){
        return Flowable.fromPublisher(documents.find(Filters.eq("pollId", pollId.toString())).limit(1))
                .map(this::deserialize);
    }

    public Flowable<Poll> findPollByMessageId(String messageId){
        return Flowable.fromPublisher(documents.find(Filters.eq("messageId", messageId)).limit(1))
                .map(this::deserialize);
    }

    public Flowable<Poll> checkTimedPolls(){
        DBObject searchQuery = new BasicDBObject()
                .append("active", true)
                .append("end",
                        new BasicDBObject()
                                .append("$gt", 0)
                                .append("$lt", System.currentTimeMillis())
                );

        return Flowable.fromPublisher(documents.find((Bson) searchQuery).limit(100)).map(this::deserialize);
    }



    @Override
    public Document serialize(Poll poll) {
        Document document = new Document();

        document.put("pollId", poll.getPollId());
        document.put("guildId", poll.getGuildId());
        document.put("channelId", poll.getChannelId());
        document.put("messageId", poll.getMessageId());
        document.put("userId", poll.getAuthorId());
        document.put("question", poll.getQuestion());
        document.put("choices", poll.getChoices());
        document.put("type", poll.getPollType().name());
        document.put("multiplechoices", poll.isMultipleChoices());
        document.put("created", poll.getCreated());
        document.put("end", poll.getEndTime());
        document.put("closed", poll.getClosed());
        document.put("active", poll.isActive());

        return document;
    }

    @Override
    public Poll deserialize(Document bson) {
        return new Poll(
                bson.get("pollId", UUID.class),
                bson.getString("guildId"),
                bson.getString("channelId"),
                bson.getString("messageId"),
                bson.getString("userId"),
                bson.getString("question"),
                bson.getList("choices", Poll.Choices.class),
                PollType.valueOf(bson.getString("type")),
                bson.getBoolean("multiplechoices"),
                bson.getLong("created"),
                bson.getLong("end"),
                bson.getLong("closed"),
                bson.getBoolean("active")
        );
    }
}
