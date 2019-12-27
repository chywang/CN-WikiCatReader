package lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


public class Indexer {

	public void index(String indexDirectoryPath, String dataFilePath)
			throws IOException {
		File dataFile = new File(dataFilePath);
		File indexDir = new File(indexDirectoryPath);
		Analyzer analyzer = new SmartChineseAnalyzer(Version.LUCENE_46, true);
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(
				Version.LUCENE_46, analyzer);
		Directory directory = FSDirectory.open(indexDir);
		IndexWriter writer = new IndexWriter(directory, indexWriterConfig);
		writer.deleteAll();
		indexRecords(writer, dataFile);
		writer.close();
	}

	private void indexRecords(IndexWriter writer, File dataFile)
			throws IOException {
		BufferedReader br=new BufferedReader(new FileReader(dataFile));
		String line;
		while ((line=br.readLine())!=null) {
			Document doc = new Document();
			doc.add(new TextField("content", line, Store.YES));
			writer.addDocument(doc);
		}
		br.close();
	}
	
	public static void main(String[] args) throws IOException {
		Indexer indexer = new Indexer();
		indexer.index("index","wiki_contents.txt");
	}

}
