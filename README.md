Opinion Phrases
=======


In [Predicting what user reviews are about with LDA and gensim](http://www.vladsandulescu.com/topic-prediction-lda-user-reviews/) I played with extracting topics from short reviews and given a new review, tried to predict the most probable topic(s) it can be associated with. LDA relies on a bag-of-words model, which is a very popular document representation approach. The model disregards any syntactic dependencies between the words, i.e. any grammar, as well as word order in the documents. For a deeper read about the assumptions made by the LDA model, try to digest [Blei's paper](http://machinelearning.wustl.edu/mlpapers/paper_files/BleiNJ03.pdf)...if you dare!

Anyway, much of the research on opinion mining used the bag-of-words model, but as [Samaneh Abbasi Moghaddam](http://www.sfu.ca/~sam39/) also suggests in her PhD thesis [Aspect-based opinion mining in online reviews](http://summit.sfu.ca/item/12790), it is not clear whether this approach is actually the most effective. Instead, she experimented with a LDA model based on opinion phrases. The full details are found in her paper, but I will make a short summary of the method.
In a nutshell, she concluded that bag-of-opinion phrases outperform the bag-of-words topic models and using the grammar relationships outperforms the existing preprocessing techniques in extracting aspects from user reviews.

**What is an opinion phrase, anyway?**
<br />
An opinion phrase is defined as a pair (aspect, sentiment) like *camera nice* or *room clean*. This is the stuff that people are generally interested in when reading a review, these key points that sum up a user's experience with the product. A very simple way to extract these pairs is to look for nouns and then pick the nearest adjective around it. This approch has obvious shortcomings. 

Luckily, we are contemporary with some clever Stanford guys who made the [Stanford CoreNLP](http://nlp.stanford.edu/software/corenlp.shtml) tool. Given a sentence, it can extract syntactic dependencies between words, output the words' base forms and even predict the overall sentiment of a text.

**What syntactical relations should be exploited?**
<br />
The following grammatical dependencies are used to extract and construct the opinion phrases inside a short sentence. They are all present and explained in her PhD thesis. 
The [Stanford typed dependencies manual](http://nlp.stanford.edu/software/dependencies_manual.pdf) explains very well the grammatical representations used below.

In order to end-up with opinion phrases, first, basic patterns are extracted:
<br />
1. Adjectival complement (acomp): *The camera looks nice*, parsed to *acomp(nice, looks)*.
<br />
2. Adjectival modifier (amod): *This camera has great zoom* parsed to *amod(zoom, great)*;
<br />
3. "And" conjunct (conj and): *This camera has great zoom and resolution* parsed to *conj and(zoom, resolution)*.
<br />
4. Copula (cop): *The screen is wide* parsed to *cop(wide, is)*.
<br />
5. Direct object (dobj):  *I love the quality* parsed to *dobj(love, quality)*.
<br />
6. Negation modifier (neg): *The battery life is not long* parsed to *neg(long, not)*.
<br />
7. Noun compound modifier (nn): *The battery life is not long* parsed to *nn(life, battery)*.
<br />
8. Nominal subject (nsubj): *The screen is wide* parsed to *nsubj(wide, screen)*.

The simple patterns are then combined in a tree-like manner to obtain more valuable opinion phrases. (N indicates a noun, A an adjective, V a verb, h a head term, m a modifier, and < h, m > an opinion phrase)[1].

1.amod(N, A) →< N, A > 
*This camera has great zoom and resolution → (zoom, great)*
<br />
2.acomp(V, A) + nsubj(V, N) →< N, A >
*The camera case looks nice --> (case, nice)*
<br />
3. cop(A, V ) + nsubj(A, N) →< N, A >
*The screen is wide and clear --> (screen, wide)*
<br />
4. dobj(V, N) + nsubj(V, N0) →< N, V >
*I love the picture quality --> (picture, love)*
<br />
5. < h1, m > +conj and(h1, h2) →< h2, m >
*This camera has great zoom and resolution --> (zoom, great), (resolution, great)*
<br />
6. < h, m1 > +conj and(m1, m2) →< h, m2 >
*The screen is wide and clear --> (screen, wide), (screen, clear)*
<br />
7. < h, m > +neg(m, not) →< h, not + m >
*The battery life is not long --> (battery life, not long)*
<br />
8. < h, m > +nn(h, N) →< N + h, m >
*The camera case looks nice --> (camera case, nice)*
<br />
9. < h, m > +nn(N, h) →< h + N, m >
*I love the picture quality --> (picture quality, love)*

Of course, these syntactical relations can be improved further on by looking up more combined patterns - did not think about this too much, but I have a gut feeling there are more combined patterns out there. 

**Pruning the extracted patterns**
<br />
As you can see in the code if you look in the [GitHub repository](https://github.com/vladsandulescu/phrases), the pruning step is quite important, as the "tree leafs" are the most significant patterns to keep, the final ones. 
I wanted opinion phrases like *fish had* and *bill paid* to go away, so I added the usual [stopwords](http://www.lextek.com/manuals/onix/stopwords2.html) removal to the pruning step. This basically means eliminating the patterns which contain any stopword.

**Some results**
<br />
Given a real-life user review:
<br />
*Great food and atmosphere.  Plenty of TVs to watch the games. The chef and his partners just opened this great location. Pumpkin Soup with pumpkin oil and croutons is such a great start to the Fall season. Wood fired oven pumping out flatbreads. Sweet Potato gnocchi made in house with roasted corn and gorgonzola crema is unbelievable. Very impressive selection of beer handles and delicious cocktails. Amazing view of the sunset as well. Can't wait to return.*

the extracted opinion phrases were:
<br />
["atmosphere Great", "food Great", "location great", "start great", "corn roasted", "gorgonzola crema roasted", "selection impressive", "view Amazing"]

Pretty good right?

Here's another one:
<br />
*This place is amazing. I come here at least once a month & am never disappointed. Food & service is always great. The buffalo burger it TDF as well as the bruschetta. Outside seating is so cute with a lights (great for date nights) live music inside is a wonderful touch. This place is great to meet with friends, family or date night.*

opinion phrases:
<br />
["place amazing", "service great", "burger buffalo", "seating cute", "touch wonderful", "seating Outside", "place great"]

By now, it should be pretty obvious to see how easier aggregating after specific aspects such as *place* and *service* is.

Another one:
<br />
*The mailing pack that was sent to me was very thorough and well explained,correspondence from the shop was prompt and accurate,I opted for the cheque payment method which was swift in getting to me. All in all, a fast efficient service that I had the upmost confidence in,very professionally executed and I will suggest you to my friends when there mobiles are due for recycling :-)*

opinion phrases:
<br />
["correspondence, prompt", "correspondence, accurate", "service, efficient"]

OK, one more and that's it:
<br />
*Aside from the wait to order and the other wait to get your food!  I was there for a late lunch on Friday and I opted to forgo my usual salad choice and go for the eggplant parm sandwich - yum!  Each bite of perfectly crusted eggplant had the most amazing tangy tomato sauce and melted cheese and it was oh so dlvine!  I had it on wheat bread and finished my entire sandwich (and yes, I ordered a full size!)  What a treat! Don't try to special order at the restaurant - my boyfriend attempted to create his own sandwich and what he ended up with was nothing close to what he ordered. I can't wait to go back to 'make my own pizza' - i know about that thanks to him! I have a feeling that I'll be visiting this place quite a bit since I'm now living in the area after a recent move.  It's a good thing - well, it's a tasty thing, maybe not so good for the waistline!*

opinion phrases:
<br />
["salad choice usual", "lunch late", "salad choice forgo", "tomato sauce amazing", "eggplant crusted", "tomato sauce tangy", "sandwich finished", "sandwich entire", "size ordered", "size full", "sandwich create", "order special", "pizza make", "move recent", "bit visiting", "thing good", "thing tasty"]
    
In this last one, you may notice phrases like *thing good*, *sandwich entire*, *sandwich finished* and *sandwich create*, which don't really help much in any aggregation, so it would be nice to eliminate them.
This could easily be done by cleverly and time-consumingly shove words like *thing*, *entire* and *create* into the stopwords list. Otherwise, the LDA model should filter these out, assuming a lot of people don't mention the exact phrases.

Check out the [code repository](https://github.com/vladsandulescu/phrases), try extracting opinion phrases by running the code yourself and don't forget to tweet to me if you have any comments.

The code is written in Java and it requires Stanford CoreNLP 3.4, Stanford Parser, JUnit and Mongo Java driver (if you plan to run it over many reviews stored in Mongo, because why wouldn't you already keep the reviews in Mongo right?). 
If you do not want to use Mongo, just call the *run* method in *Extract* class giving it the text to extract opinion phrases from.

That is basically it. 

==================
[1]: [Abbasi Moghaddam, Samaneh. Aspect-based opinion mining in online reviews. Diss. Applied Sciences: School of Computing Science, 2013.](http://summit.sfu.ca/item/12790)
