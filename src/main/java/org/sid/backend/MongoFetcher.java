package org.sid.backend;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class MongoFetcher extends MongoConnection {

    public MongoFetcher(MongoCollection<Document> cloud_collection, MongoCollection<Document> local_collection, int periodicity) {
        super(cloud_collection, local_collection, periodicity);
    }

    @Override
    protected boolean isValid(Document doc) {
        return true;
    }

    @Override
    protected void handleData(Document doc) {
        System.out.println(doc.toJson());
    }
}
