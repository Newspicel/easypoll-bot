package de.fbrettnich.easypoll.database;

import org.bson.Document;

public interface MongoDBRepository<T> {

    Document serialize(T t);
    T deserialize(Document bson);
}
