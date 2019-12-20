package nontaxonomic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerbBasedRelationExtractor {

	public static void main(String[] args) throws IOException {
		//loading
		Set<String> wikiDic=new HashSet<String>();
		BufferedReader br=new BufferedReader(new FileReader(new File("wiki.dic")));
		String line;
		while ((line=br.readLine())!=null) {
			String[] items=line.split("\t");
			wikiDic.add(items[0]);
		}
		br.close();
		Map<String,String> regexPatterns=new HashMap<String,String>();
		br=new BufferedReader(new FileReader(new File("verb-patterns.txt")));
		while ((line=br.readLine())!=null) {
			String[] items=line.split("\t");
			if (items.length!=2)
				continue;
			String rawPattern=items[0];
			String verb=items[1];
			if (verb.equals("unkown"))
				continue;
			rawPattern=rawPattern.replaceAll("ENTITY", "(.+)");
			rawPattern="^"+rawPattern+"$";
			regexPatterns.put(rawPattern,verb);
		}
		br.close();
		
		PrintWriter pw=new PrintWriter("verb-relations.txt");
		br=new BufferedReader(new FileReader(new File("cat.txt")));
		while ((line=br.readLine())!=null) {
			String[] items=line.split("\t");
			String entity=items[0];
			for (int i=1;i<items.length;i++) {
				String category=items[i];
				for (String reg:regexPatterns.keySet()) {
					String verb=regexPatterns.get(reg);
					 Pattern r = Pattern.compile(reg);
				     Matcher m = r.matcher(category);
				     if (m.find( )) {
				    	 if (m.group(1).length()>6 && !wikiDic.contains(m.group(1)))
				    		 continue;
				    	 pw.println(entity+"\t"+verb+"\t"+m.group(1)+"\t"+reg);
				    	 pw.flush();
				     }
				}
			}
		}
		br.close();
		pw.close();
		
		Map<String,String> regexTriPatterns=new HashMap<String,String>();
		br=new BufferedReader(new FileReader(new File("verb-patterns.txt")));
		while ((line=br.readLine())!=null) {
			String[] items=line.split("\t");
			if (items.length!=3)
				continue;
			String rawPattern=items[0];
			String verb=items[1]+"\t"+items[2];
			if (verb.equals("unkown"))
				continue;
			rawPattern=rawPattern.replaceAll("ENTITY", "(.+)");
			rawPattern="^"+rawPattern+"$";
			System.out.println("patterns: "+rawPattern);
			regexTriPatterns.put(rawPattern,verb);
		}
		br.close();
		
		Set<String> relations=new HashSet<String>();
		pw=new PrintWriter("verb-relations-infer.txt");
		br=new BufferedReader(new FileReader(new File("cat.txt")));
		while ((line=br.readLine())!=null) {
			String[] items=line.split("\t");
			String entity=items[0];
			for (int i=1;i<items.length;i++) {
				String category=items[i];
				for (String reg:regexTriPatterns.keySet()) {
					String verb=regexTriPatterns.get(reg);
					 Pattern r = Pattern.compile(reg);
				     Matcher m = r.matcher(category);
				     if (m.find( )) {
				    	 relations.add(entity+"\t"+verb+"\t"+reg);
				     }
				}
			}
		}
		br.close();
		for (String s:relations) {
			pw.println(s);
			pw.flush();
		}
		pw.close();
		System.out.println(relations.size());
	}

}
