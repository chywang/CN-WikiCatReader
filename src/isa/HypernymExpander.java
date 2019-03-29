package isa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class HypernymExpander {

	public static void main(String[] args) throws IOException {
		Set<String> wikiDic=new HashSet<String>();
		BufferedReader br=new BufferedReader(new FileReader(new File("cat.txt")));
		String line;
		while ((line=br.readLine())!=null) {
			String[] items=line.split("\t");
			if (items[0].length()<=3 && items[0].length()>=2)
				wikiDic.add(items[0]);
		}
		br.close();
		br=new BufferedReader(new FileReader(new File("whitelist.txt")));
		while ((line=br.readLine())!=null) {
			if (line.length()>=2)
				wikiDic.add(line);
		}
		br.close();
		System.out.println(wikiDic.size());

		int count=0;
		br = new BufferedReader(new FileReader(new File("total-isa.txt")));
		PrintWriter pw=new PrintWriter("total-isa-expand.txt");
		while ((line = br.readLine()) != null) {
			String[] items=line.split("\t");
			String entity=items[0];
			Set<String> categories=new HashSet<String>();
			for (int i=1;i<items.length;i++) {
				String category=items[i];
				if (entity.equals(categories))
					continue;
				for (String wikiE:wikiDic) {
					if (category.endsWith(wikiE))
						categories.add(wikiE);
				}
				categories.add(category);
				if (expandPerson(categories))
					categories.add("人物");
				if (expandLocation(categories))
					categories.add("地区");
			}
			count+=categories.size();
			if (categories.size()>0) {
				pw.print(entity);
				for (String c:categories) {
					pw.print("\t"+c);
				}
				pw.println();
				pw.flush();
			}
		}
		br.close();
		pw.close();
		System.out.println(count);
	}
	
	private static boolean expandPerson(Set<String> categories) {
		for (String s:categories) {
			if (s.endsWith("人") || s.endsWith("人物"))
				return true;
		}
		return false;
	}
	
	private static boolean expandLocation(Set<String> categories) {
		for (String s:categories) {
			if (s.endsWith("区") || s.endsWith("区划"))
				return true;
		}
		return false;
	}

}
