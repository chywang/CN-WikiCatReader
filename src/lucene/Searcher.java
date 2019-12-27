package lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Searcher {

	public static void main(String[] args) throws IOException, ParseException {
		Searcher searcher = new Searcher("index");
		int topk = 100;
		List<String> records = searcher.search("维基百科", topk);
		for (String record : records)
			System.out.println(record);

	}
	
	private String indexDirectoryPath;
	private QueryParser contentParser;
	private IndexSearcher searcher;

	public Searcher(String indexDirectoryPath) throws IOException {
		this.indexDirectoryPath = indexDirectoryPath;
		Directory indexDir = FSDirectory
				.open(new File(this.indexDirectoryPath));
		IndexReader reader = DirectoryReader.open(indexDir);
		searcher = new IndexSearcher(reader);
		contentParser = new QueryParser(Version.LUCENE_46, "content",
				new SmartChineseAnalyzer(Version.LUCENE_46, true));
	}

	public List<String> search(String searchWords, int topk)
			throws ParseException, IOException {
		List<String> records = new ArrayList<String>();
		Query query = contentParser.parse(searchWords);
		TopDocs topdocs = searcher.search(query, topk);
		ScoreDoc[] scoreDocs = topdocs.scoreDocs;
		for (int i = 0; i < scoreDocs.length; i++) {
			int doc = scoreDocs[i].doc;
			Document document = searcher.doc(doc);
			String content = document.get("content");
			records.add(content);
		}
		return records;
	}

}
