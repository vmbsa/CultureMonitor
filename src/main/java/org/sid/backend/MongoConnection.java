package org.sid.backend;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.Stack;

public abstract class MongoConnection extends Thread {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private final MongoCollection<Document> searched_collection;
    private final MongoCollection<Document> inserted_collection;

    private final Stack<Document> recent_documents = new Stack<>();

    private final int periodicity;

    public MongoConnection(MongoCollection<Document> cloud_collection, MongoCollection<Document> local_collection, int periodicity) {
        this.searched_collection = cloud_collection;
        this.inserted_collection = local_collection;
        this.periodicity = periodicity * 1000;
    }

    private boolean isPresent(Document doc) {
        try {
            if (recent_documents.peek().equals(doc)) {
                return true;
            } else {
                recent_documents.pop();
                recent_documents.push(doc);
                return false;
            }
        } catch (EmptyStackException e) {
            recent_documents.push(doc);
            return false;
        }
    }

    @Override
    public void run() {
        fetchDataFromCloud();
    }

    private void fetchDataFromCloud() {
        Date searchDate = new Date(System.currentTimeMillis());
        String searchDate_string = dateFormat.format(searchDate);
        while (true) {
            try (MongoCursor<Document> cursor = searched_collection.find(Filters.gte("Data", searchDate_string)).iterator()) {
                if (cursor.hasNext()) {
                    Document doc = cursor.next();
                    if (!isPresent(doc) && isValid(doc)) {
                        inserted_collection.insertOne(doc);
                        handleData(doc);
                    }
                    searchDate = new Date(searchDate.getTime() + periodicity);
                    searchDate_string = dateFormat.format(searchDate);
                }
            }
        }
    }

    protected abstract boolean isValid(Document doc);

    protected abstract void handleData(Document doc);

    protected MongoCollection<Document> getSearchedCollection() {
        return searched_collection;
    }


}
