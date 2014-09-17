import java.util.List;

public class SimpleRunner {

    public static void main(String[] args) {
        Extract extract = new Extract();
        List<Pattern> patterns = extract.run("Cafe is conveniently located on Pier 39 in San Fransisco and offers a great view of the bay along with a good hearty breakfast. I had heard some mixed things about Cafe but I wanted to see for myself so I went Sunday morning before my flight home. The restaurant itself is very clean and as expected for being in the middle of a big tourist location it is pretty busy. My meal consisted of banana and pecan french toast, country potatoes, fruit and 2 eggs over easy. The food was good but not great, my biggest complaint was the syrup was very thin and not super flavorful and the french toast was a little dry. 3 stars for A ok (not bad)");
        for (Pattern pattern : patterns) {
            System.out.println(pattern.toAspect());
        }
    }
}
