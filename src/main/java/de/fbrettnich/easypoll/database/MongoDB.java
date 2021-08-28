package de.fbrettnich.easypoll.database;


import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import de.fbrettnich.easypoll.files.ConfigFile;
import org.bson.Document;
import org.bson.UuidRepresentation;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MongoDB {

    private final MongoClient mongoClient;
    private final MongoDatabase mongoDatabase;

    @Inject
    public MongoDB(ConfigFile configFile) {

        MongoClientSettings mongoClientSettings= MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(configFile.getString("mongodb.clienturi")))
                .uuidRepresentation(UuidRepresentation.JAVA_LEGACY)
                .build();

        this.mongoClient = MongoClients.create(mongoClientSettings);
        this.mongoDatabase = mongoClient.getDatabase(configFile.getString("mongodb.database"));

    }

    /**
     *
     * @return MongoClient
     */
    public MongoClient getClient() {
        return mongoClient;
    }

    /**
     * Get Mongo Collection from Database
     *
     * @param collection name
     * @return DBCollection
     */
    public MongoCollection<Document> getCollection(String collection) {
        return mongoDatabase.getCollection(collection);
    }
}
