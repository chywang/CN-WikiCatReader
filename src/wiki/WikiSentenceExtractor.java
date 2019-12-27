package wiki;
import java.io.*;

import com.spreada.utils.chinese.ZHConverter;

import edu.jhu.nlp.wikipedia.*;

public class WikiSentenceExtractor {

	public static void main(String[] args) throws IOException {
		String xmlSourceFile = "zhwiki-20170120-pages-articles-multistream.xml";
		WikiXMLParser wxsp = WikiXMLParserFactory.getSAXParser(xmlSourceFile);
		ZHConverter converter = ZHConverter.getInstance(ZHConverter.SIMPLIFIED);
		PrintWriter pw=new PrintWriter("wiki_contents.txt");
		try {
			wxsp.setPageCallback(new PageCallbackHandler() {
				@Override
				public void process(WikiPage page) {
					if (page.isDisambiguationPage() || page.isRedirect() || page.isSpecialPage() || page.isStub())
						return;
					String title = page.getTitle().trim();
					String content=page.getText().trim();
					System.out.println(title);
					String[] items=content.split("\n");
					for (String s:items) {
						s=s.trim();
						s = s.replaceAll(" ","");
						s = s.replaceAll("<ref.*?/(ref)?>","");
						s = s.replaceAll("&nbsp;"," ");
						s = s.replaceAll("]]","");
						s = s.replaceAll("}}","");
						s = s.replaceAll("[[|]]","");
						if (s.startsWith("*") || s.startsWith("=") || s.startsWith("|") 
								|| s.startsWith("{") || s.startsWith("<") || s.startsWith(">")
								|| s.startsWith(";") || s.startsWith("}") || s.startsWith("#")
								|| s.startsWith("!") || s.startsWith(":") || s.startsWith(" ")
								|| s.startsWith("&") || s.startsWith("※") || s.startsWith("_")
								|| s.startsWith("[[Category") || s.startsWith("File:") || s.startsWith("]]"))
							continue;
						s=s.trim();
						if (s.length()<10)
							continue;
						String[] items1=s.split("。！？?!");
						for (String s1:items1) {
							s1=s1.trim();
							if (s1.length()<10 || s1.length()>80)
								continue;
							if (s1.startsWith("|"))
								continue;
							s1 = s1.replaceAll("\\{\\{citeweb","");
							while (s1.contains(",,"))
								s1=s1.replaceAll(",,", ",");
							if (s1.startsWith(","))
								s1=s1.substring(1);
							if (s1.endsWith(","))
								s1=s1.substring(0, s1.length()-1);
							if (s1.startsWith("url"))
								continue;
							if (s1.startsWith("title"))
								continue;
							if (!zhSen(s1))
								continue;
							pw.println(converter.convert(s1));
							pw.flush();
						}
					}
				}
			});
			wxsp.parse();
			System.out.println("Done!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		pw.close();
	}
	
	public static boolean zhSen(String s) {
		String reg = "[\\u4e00-\\u9fa5]+";
		double count=0;
		for (int i=0;i<s.length();i++) {
			String s1=s.substring(i,i+1);
			boolean result1 = s1.matches(reg);
			if (result1)
				count++;
		}
		return (count/s.length())>0.5;
	}

}
