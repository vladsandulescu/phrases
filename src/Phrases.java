import com.mongodb.*;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Phrases {

    final static String MongoUri = "mongodb://localhost:27030/";
    final static String Database = "YelpReviews";
    final static String Reviews = "Review";
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
        try {
            phrases.run();
        }
        catch(Exception exception) {
            phrases.run();
        }
    }

    public void run() {
        DBCursor cursor = reviewsCollection.find();
        int count = cursor.count();
        Extract extractor = new Extract();
        int done = 0;
        try {
            while (cursor.hasNext()) {
                DBObject dbItem = cursor.next();
                String text = dbItem.get("Text").toString();
                String reviewId = dbItem.get("ReviewId").toString();
                String business = dbItem.get("Business").toString();

                if (!alreadyExtractedPhrases(reviewId)) {
                    List<Pattern> phrases = extractor.run(text);
                    Review review = new Review(text, reviewId, business);
                    savePhrases(review, phrases);
                }

                done++;
                System.out.println("Done " + done + "/" + count);
            }
        } finally {
            cursor.close();
        }
    }

    private boolean alreadyExtractedPhrases(String reviewId) {
        return phrasesCollection.find(new BasicDBObject("review.reviewId", reviewId)).count() > 0;
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
