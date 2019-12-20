package nontaxonomic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class WikiDicGenerator {

	public static void main(String[] args) throws IOException {
		Set<String> set=new HashSet<String>();
		BufferedReader br=new BufferedReader(new FileReader(new File("cat.txt")));
		String line;
		while ((line=br.readLine())!=null) {
			String[] items=line.split("\t");
			String entity=items[0];
			if (entity.length()>20)
				continue;
			if (entity.length()<3)
				continue;
			if (entity.indexOf(" ")>=0)
				continue;
			set.add(entity);
		}
		br.close();
		PrintWriter pw=new PrintWriter("wiki.dic");
		for (String s:set) {
			pw.println(s+"\t专有名");
			pw.flush();
		}
		pw.close();
	}

}
