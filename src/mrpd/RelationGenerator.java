package mrpd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class RelationGenerator {
	
	public static void main(String[] args) throws Exception {
		//generate relation predicates
		Set<String> predicates=new HashSet<String>();
		BufferedReader br=new BufferedReader(new FileReader(new File("basic_relation.txt")));
		String line;
		while ((line=br.readLine())!=null) {
			String[] items=line.split("\t");
			String verb=items[1];
			predicates.add(verb);
		}
		br.close();
		predicates.add("处于");
		predicates.add("位于");
		predicates.add("参加");
		predicates.add("发生");

		Set<String> extractedRelationSet=new HashSet<String>();
		br=new BufferedReader(new FileReader(new File("basic_remain.txt")));
		while ((line=br.readLine())!=null) {
			String[] items=line.split("\t");
			String subject=items[0];
			String verb=items[1];
			if (items[1].indexOf("/")>=0)
				verb=items[1].substring(0, items[1].indexOf("/"));
			String object=items[2].substring(0, items[2].indexOf("/"));
			String objectPos=items[2].substring(items[2].indexOf("/")+1);
			String extractedRelation=matchCommonSense(subject, verb, object, objectPos);
			if (extractedRelation!=null)
				extractedRelationSet.add(extractedRelation);			
		}
		br.close();
		System.out.println(extractedRelationSet.size());
		
		PrintWriter pw=new PrintWriter("cs_relation.txt");
		for (String s:extractedRelationSet) {
			pw.println(s);
			pw.flush();
		}
		pw.close();
	}
	
	public static String matchCommonSense(String subject, String verb, String object, String objectPos) {
		if (object.endsWith("月") || object.endsWith("世纪") || object.endsWith("年代")) {
			verb="处于";
			return subject+"\t"+verb+"\t"+object;
		} else if (object.endsWith("年")) {
			verb="发生";
			return subject+"\t"+verb+"\t"+object;
		} else if (object.indexOf("年")>=0 && (object.endsWith("会") || object.endsWith("赛"))) {
			verb="参加";
			return subject+"\t"+verb+"\t"+object;
		} else if (objectPos.indexOf("地名")>=0 && object.length()>=2) {
			verb="位于";
			return subject+"\t"+verb+"\t"+object;
		}
		return null;

	}

}
