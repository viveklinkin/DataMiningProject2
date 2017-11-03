package preprc;

public class Article{
	int docid;
	int id;
	String topic, body;
	Article(int docid, int id, String topic, String body){
		this.docid = docid;
		this.id = id;
		this.topic = topic;
		this.body = body;
	}
}
