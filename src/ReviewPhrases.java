import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.io.Serializable;
import java.util.List;

public class ReviewPhrases implements Serializable{

    private Review review;

    private List<String> phrases;

    public ReviewPhrases(Review review, List<String> phrases) {
        this.review = review;
        this.phrases = phrases;
    }

    public DBObject toDBObject() {
        DBObject dbObject = new BasicDBObject();
        dbObject.put("review", review.toDBObject());
        dbObject.put("phrases", phrases);

        return dbObject;
    }
}
