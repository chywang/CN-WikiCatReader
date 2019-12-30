package crg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CandidateRelGen {

	public static void main(String[] args) throws IOException {
		BufferedReader br=new BufferedReader(new FileReader(new File("cat_mod_seg.txt")));
		PrintWriter pw=new PrintWriter("can_relation_raw.txt");
		String line;
		int count=0;
		while ((line=br.readLine())!=null) {
			String[] items=line.split("\t");
			String entity=items[0];
			String modTags=items[3];
			List<String> outcome=new ArrayList<String>();
			outcome.addAll(relationGenForEntity(entity, modTags));
			for (String s:outcome) {
				pw.println(s);
				pw.flush();
				count++;
				if (count%1000==0) {
					System.out.println(count);
				}
			}
		}
		br.close();
		br=new BufferedReader(new FileReader(new File("cat_seg.txt")));
		while ((line=br.readLine())!=null) {
			String[] items=line.split("\t");
			String entity=items[0];
			String tags=items[2];
			List<String> outcome=new ArrayList<String>();
			outcome.addAll(relationGenForEntity(entity, tags));
			for (String s:outcome) {
				pw.println(s);
				pw.flush();
				count++;
				if (count%1000==0) {
					System.out.println(count);
				}
			}
		}
		pw.close();
	}
	
	public static List<String> relationGenForEntity(String entity, String tags) {
		List<String> outcome=new ArrayList<String>();
		String[] items=tags.split(" ");
		for (int i=0;i<items.length-1;i++) {
			String target=items[i];
			if (target.contains("专有名") || target.contains("人名") || target.contains("地名") || target.contains("机构名")) {
				String verb="";
				if (i-1>0 && items[i-1].endsWith("动词")) {
					verb=items[i-1];
				} else if (i+1<items.length && items[i+1].endsWith("动词")) {
					verb=items[i+1];
				}
				if (verb.equals("")) {
					outcome.add(entity+"\t?\t"+target);
				} else {
					outcome.add(entity+"\t"+verb+"\t"+target);
				}		
			}
		}
		return outcome;
	}

}
