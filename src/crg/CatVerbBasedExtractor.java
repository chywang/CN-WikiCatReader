package crg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class CatVerbBasedExtractor {

	public static void main(String[] args) throws IOException {
		Set<String> set=new HashSet<String>();
		BufferedReader br=new BufferedReader(new FileReader(new File("step1_verb_count.txt")));
		String line;
		while ((line=br.readLine())!=null) {
			set.add(line.substring(0, line.indexOf("\t")));
		}
		br.close();
		
		br=new BufferedReader(new FileReader(new File("can_relation_raw_count.txt")));
		PrintWriter pw=new PrintWriter("step1_verb_rel_extraction.txt");
		PrintWriter pw1=new PrintWriter("step1_remain.txt");

		int count=0;
		while ((line=br.readLine())!=null) {
			String[] items=line.split("\t");
			String subject=items[0];
			boolean find=false;
			if (!items[1].equals("?")) {
				String verb=items[1].substring(0, items[1].indexOf("/"));
				String object=items[2];
				if (set.contains(verb)) {
					if (object.substring(object.indexOf("/")+1).indexOf("#")<0) {
						pw.println(line);
						pw.flush();
						count++;
						find=true;
					}
				}
			} else {
				if (!find) {
					pw1.println(line);
					pw1.flush();
				}
			}
			
		}
		br.close();
		System.out.println(count);
		pw.close();
		pw1.close();
	}

}
