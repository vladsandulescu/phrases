import com.mongodb.*;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Phrases {

    final static String MongoUri = "mongodb://localhost:27030/";
    final static String Database = "Reviews";
    final static String Reviews = "Reviews";
    final static String Phrases = "Phrases";
    MongoClient mongoClient;
    DB database;
    DBCollection reviewsCollection, phrasesCollection;

    public Phrases() {
        try {
            mongoClient = new MongoClient(new MongoClientURI(MongoUri));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        database = mongoClient.getDB(Database);
        reviewsCollection = database.getCollection(Reviews);
        phrasesCollection = database.getCollection(Phrases);
    }

    public static void main(String[] args) {
        Phrases phrases = new Phrases();
        phrases.run();
    }

    public void run() {
        DBCursor cursor = reviewsCollection.find();
        Extract extractor = new Extract();
        try {
            while (cursor.hasNext()) {
                DBObject dbItem = cursor.next();
                String text = dbItem.get("text").toString();
                String reviewId = dbItem.get("reviewId").toString();
                String business = dbItem.get("business").toString();

                List<Pattern> phrases = extractor.run(text);
                Review review = new Review(text, reviewId, business);
                savePhrases(review, phrases);
            }
        } finally {
            cursor.close();
        }
    }

    private void savePhrases(Review review, List<Pattern> patterns) {
        List<String> phrases = new ArrayList<String>();
        for (Pattern pattern : patterns) {
            phrases.add(pattern.toAspect());
        }
        ReviewPhrases result = new ReviewPhrases(review, phrases);
        phrasesCollection.insert(result.toDBObject());
    }
}
