package nontaxonomic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import edu.fudan.nlp.cn.tag.POSTagger;

public class VerbBasedFilter {

	public static void main(String[] args) throws Exception {
		// loading
		Set<String> wikiDic = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(new File("wiki.dic")));
		String line;
		while ((line = br.readLine()) != null) {
			String[] items = line.split("\t");
			wikiDic.add(items[0]);
		}
		br.close();

		PrintWriter pw = new PrintWriter("zh/verb-patterns.txt");
		POSTagger tag = new POSTagger("fdnlp/seg.m", "fdnlp/pos.m");
		br = new BufferedReader(new FileReader(new File("patterns-relation-conf.txt")));
		int count = 0;
		while ((line = br.readLine()) != null) {
			String[] items = line.split("\t");
			String pattern = items[0];
			pattern = pattern.replaceAll("ENTITY代", "ENTITY");
			double conf = Double.parseDouble(items[1]);
			String verb = getVerb(tag, pattern);
			if (conf<0.7)
				continue;
			if (verb != null) {
				count++;
				pw.println(pattern + "\t" + verb);
				pw.flush();
			} else {
				String word = pattern.replaceAll("ENTITY", "");
				if (wikiDic.contains(word)) {
					pw.println(pattern + "\t" + "topic"+"\t"+word);
					pw.flush();
				} else{
					pw.println(pattern + "\t" + "unkown");
					pw.flush();
				}
	
			}
		}
		br.close();
		pw.close();
		System.out.println(count);
	}

	private static String getVerb(POSTagger tag, String pattern) {
		pattern = pattern.replaceAll("ENTITY", "");
		String outcome = null;
		String[] items = tag.tag(pattern).split(" ");
		for (String s : items) {
			try {
				String word = s.substring(0, s.indexOf("/"));
				String posTag = s.substring(s.indexOf("/") + 1);
				if (posTag.equals("动词")) {
					outcome = word;
					break;
				}
			} catch (Exception e) {
				continue;
			}

		}
		return outcome;
	}

}
