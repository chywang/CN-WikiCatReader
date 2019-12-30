package crg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class RuleBasedGen {

	//combiner
	public static void main(String[] args) throws IOException {
		Set<String> set=new HashSet<String>();
		File folder=new File("rule");
		for (File f:folder.listFiles()) {
			if (!f.getName().endsWith(".txt"))
				continue;
			String verb=f.getName().substring(0, f.getName().indexOf("."));
			System.out.println(verb);
			BufferedReader br=new BufferedReader(new FileReader(f));
			String line;
			while ((line=br.readLine())!=null) {
				String[] items=line.split("\t");
				String subject=items[0];
				String object=items[1];
				set.add(subject+"\t"+verb+"\t"+object);
			}
			br.close();
			
		}
		System.out.println(set.size());
		BufferedReader br=new BufferedReader(new FileReader(new File("step1_verb_rel_extraction.txt")));
		String line;
		while ((line=br.readLine())!=null) {
			String[] items=line.split("\t");
			String subject=items[0];
			String verb=items[1].substring(0, items[1].indexOf("/"));
			String object=items[2].substring(0, items[2].indexOf("/"));
			set.add(subject+"\t"+verb+"\t"+object);
		}
		br.close();
		br=new BufferedReader(new FileReader(new File("step2_verb_rel_extraction.txt")));
		while ((line=br.readLine())!=null) {
			String[] items=line.split("\t");
			String subject=items[0];
			String verb=items[1].substring(0, items[1].indexOf("/"));
			String object=items[2].substring(0, items[2].indexOf("/"));
			set.add(subject+"\t"+verb+"\t"+object);
		}
		br.close();
		System.out.println(set.size());
		PrintWriter pw=new PrintWriter("basic_relation.txt");
		for (String s:set) {
			pw.println(s);
			pw.flush();
		}
		pw.close();
		int count=0;
		br=new BufferedReader(new FileReader(new File("step2_remain.txt")));
		pw=new PrintWriter("basic_remain.txt");
		while ((line=br.readLine())!=null) {
			String[] items=line.split("\t");
			String subject=items[0];
			String verb=items[1];
			if (items[1].indexOf("/")>=0)
				verb=items[1].substring(0, items[1].indexOf("/"));
			String object=items[2];
			if (items[2].indexOf("/")>=0)
				object=items[2].substring(0, items[2].indexOf("/"));
			if (!set.contains(subject+"\t"+verb+"\t"+object)) {
				count++;
				pw.println(line);
				pw.flush();
			}
		}
		br.close();
		pw.close();
		System.out.println(count);
	}
	
}
