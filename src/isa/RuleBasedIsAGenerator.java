package isa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class RuleBasedIsAGenerator {
	
	public static boolean matched(String entity,String category,Set<String> blacklist,Set<String> whitelist) {
		char[] es=entity.toCharArray();
		char[] cs=category.toCharArray();
		if (es[es.length-1]==cs[cs.length-1])
			return true;
		for (String s:blacklist) {
			if (category.endsWith(s))
				return false;
		}
		for (String s:whitelist) {
			if (category.endsWith(s))
				return true;
		}
		if (category.indexOf("的")>=0) {
			String[] items=category.split("的");
			if (items.length==2)
				return true;
		}
		return false;
	}
	
	private static Set<String> loadBlacklist() throws IOException {
		BufferedReader br=new BufferedReader(new FileReader(new File("blacklist.txt")));
		Set<String> set=new HashSet<String>();
		String line;
		while ((line=br.readLine())!=null)
			set.add(line);
		br.close();
		return set;
	}
	
	private static Set<String> loadWhitelist() throws IOException {
		BufferedReader br=new BufferedReader(new FileReader(new File("whitelist.txt")));
		Set<String> set=new HashSet<String>();
		String line;
		while ((line=br.readLine())!=null)
			set.add(line);
		br.close();
		return set;
	}
	
	public static void main(String[] args) throws IOException {
		Set<String> blacklist=loadBlacklist();
		Set<String> whitelist=loadWhitelist();
		BufferedReader br=new BufferedReader(new FileReader(new File("cat.txt")));
		PrintWriter pw=new PrintWriter("rule-based-isa.txt");
		PrintWriter pw1=new PrintWriter("cat-1.txt");
		String line;
		int count=0;
		while ((line=br.readLine())!=null) {
			String[] items=line.split("\t");
			String entity=items[0];
			Set<String> remainedCategories=new HashSet<String>();
			Set<String> isaCategories=new HashSet<String>();
			for (int i=1;i<items.length;i++) {
				String category=items[i];
				boolean result=matched(entity,category,blacklist,whitelist);
				if (result) {
					count++;
					isaCategories.add(category);
				}
				else
					remainedCategories.add(category);
			}
			if (isaCategories.size()>0) {
				pw.print(entity);
				for (String s:isaCategories) {
					pw.print("\t"+s);
				}
				pw.println();
				pw.flush();
			}
			if (remainedCategories.size()>0) {
				pw1.print(entity);
				for (String s:remainedCategories) {
					pw1.print("\t"+s);
				}
				pw1.println();
				pw1.flush();
			}
		}
		br.close();
		pw.close();
		pw1.close();
		System.out.println(count);
	}

}
