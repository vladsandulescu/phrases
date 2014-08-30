import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.io.Serializable;

public class Review implements Serializable{

    private String text;

    private String reviewId;

    private String business;

    public Review(String text, String reviewId, String business) {
        this.text = text;
        this.reviewId = reviewId;
        this.business = business;
    }

    public Object toDBObject() {
        DBObject dbObject = new BasicDBObject();
        dbObject.put("text", text);
        dbObject.put("reviewId", reviewId);
        dbObject.put("business", business);

        return dbObject;
    }
}
