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

public class RelationPatternMiner {

	public static void main(String[] args) throws IOException {
		//loading
		Set<String> wikiDic=new HashSet<String>();
		BufferedReader br=new BufferedReader(new FileReader(new File("wiki.dic")));
		String line;
		while ((line=br.readLine())!=null) {
			String[] items=line.split("\t");
			if (items[0].length()>3)
				wikiDic.add(items[0]);
		}
		br.close();
		Set<String> categories=new HashSet<String>();
		br=new BufferedReader(new FileReader(new File("zh/cat.txt")));
		while ((line=br.readLine())!=null) {
			String[] items=line.split("\t");
			for (int i=1;i<items.length;i++)
				categories.add(items[i]);
		}
		br.close();
		
		//raw pattern mining
		PrintWriter pw=new PrintWriter("patterns-relation.txt");
		Map<String, Set<String>> patternCount=new HashMap<String,Set<String>>();
		int count=0;
		for (String cat:categories) {
			count++;
			if (count%1000==0)
				System.out.println(count);
			if (cat.length()<=3)
				continue;
			for (String entity:wikiDic) {
				if (cat.contains(entity)) {
					String pattern=cat.replaceAll(entity, "ENTITY");
					if (pattern.equals("ENTITY"))
						continue;
					if (pattern.toCharArray()[0]>='0' && pattern.toCharArray()[0]<='9')
						continue;
					if (!patternCount.containsKey(pattern))
						patternCount.put(pattern, new HashSet<String>());
					else {
						Set<String> set=patternCount.get(pattern);
						set.add(entity);
						patternCount.put(pattern, set);
					}
				}
			}
			
		}
		for (String pat:patternCount.keySet()) {
			Set<String> set=patternCount.get(pat);
		//	if (set.size()>=10) {
				pw.print(pat);
				for (String s:set)
					pw.print("\t"+s);
				pw.println();
				pw.flush();
		//	}
		}
		pw.close();
	}

}
