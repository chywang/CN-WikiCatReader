package isa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProjectionBasedIsARelationExtractor {

	public static boolean filtered(String entity,String category,Set<String> blacklist) {
		for (String s:blacklist) {
			if (category.endsWith(s))
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
	
	public static void main(String[] args) throws IOException {
		Set<String> blacklist=loadBlacklist();
		Map<String, Set<String>> totalMap=new HashMap<String,Set<String>>();
		BufferedReader br=new BufferedReader(new FileReader(new File("rule-based-isa.txt")));
		String line;
		while ((line = br.readLine()) != null) {
			String[] items=line.split("\t");
			String entity=items[0];
			Set<String> categories=new HashSet<String>();
			for (int i=1;i<items.length;i++) {
				String category=items[i];
				categories.add(category);
			}
			totalMap.put(entity, categories);
		}
		br.close();
		
		br=new BufferedReader(new FileReader(new File("projection-based-isa-collective.txt")));
		while ((line = br.readLine()) != null) {
			String[] items=line.split("\t");
			String entity=items[0];
			String category=items[1];
			if (filtered(entity, category, blacklist))
				continue;
			double weight=Double.parseDouble(items[2]);
			if (weight>0) {
				if (!totalMap.containsKey(entity))
					totalMap.put(entity, new HashSet<String>());
				Set<String> categories=totalMap.get(entity);
				categories.add(category);
				totalMap.put(entity,categories);
			}
		}
		br.close();
		int count=0;
		PrintWriter pw=new PrintWriter("total-isa-output.txt");
		for (String entity:totalMap.keySet()) {
			if (totalMap.get(entity).size()==0)
				continue;
			pw.print(entity);
			count=count+totalMap.get(entity).size();
			for (String s:totalMap.get(entity)) {
				pw.print("\t"+s);
			}
			pw.println();
			pw.flush();
		}
		pw.close();
		pw.close();	
		System.out.println(count);
	}

}
