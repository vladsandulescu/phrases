import com.mongodb.*;

import java.net.UnknownHostException;

public class Phrases {

    final static String MongoUri = "mongodb://localhost:27030/";
    final static String Database = "Reviews";
    final static String Collection = "Reviews";

    public static void main(String[] args) {
        try {
            DBCursor cursor = getDbObjects();
            Extract extractor = new Extract();
            try {
                while(cursor.hasNext()) {
                    String text = cursor.next().get("text").toString();
                    System.out.println(extractor.run(text));
                }
            } finally {
                cursor.close();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private static DBCursor getDbObjects() throws UnknownHostException {
        MongoClient mongoClient = new MongoClient(new MongoClientURI(MongoUri));
        DB database = mongoClient.getDB(Database);
        DBCollection collection = database.getCollection(Collection);

        return collection.find();
    }
}
