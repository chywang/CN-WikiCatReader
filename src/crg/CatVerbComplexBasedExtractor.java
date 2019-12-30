package crg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class CatVerbComplexBasedExtractor {

	public static void main(String[] args) throws IOException {
		Set<String> set=new HashSet<String>();
		BufferedReader br=new BufferedReader(new FileReader(new File("step1_verb_count.txt")));
		String line;
		while ((line=br.readLine())!=null) {
			set.add(line.substring(0, line.indexOf("\t")));
		}
		br.close();
		
		br=new BufferedReader(new FileReader(new File("step1_remain.txt")));
		PrintWriter pw=new PrintWriter("step2_verb_rel_extraction.txt");
		PrintWriter pw1=new PrintWriter("step2_remain.txt");

		int count=0;
		while ((line=br.readLine())!=null) {
			String[] items=line.split("\t");
			String subject=items[0];
			boolean find=false;
			if (items[1].equals("?")) {
			//	String verb=items[1].substring(0, items[1].indexOf("/"));
				String object=items[2];
				String countStr=items[3];
				String objectEntity=object.substring(0, object.indexOf("/"));
				String objectPos=object.substring(object.indexOf("/")+1);
				if (objectPos.equals("专有名#动词")) {
					String trueObj=objectEntity.substring(0, objectEntity.length()-2);
					String trueVerb=objectEntity.substring(objectEntity.length()-2);
					if (set.contains(trueVerb)) {
						pw.println(subject+"\t"+trueVerb+"/动词\t"+trueObj+"/专有名\t"+items[3]);
						pw.flush();
						find=true;
						count++;
						continue;
					}
				//	System.out.println(trueObj+"\t"+trueVerb);
				} else if (objectPos.equals("专有名#动词#结构助词")) {
					String trueObj=objectEntity.substring(0, objectEntity.length()-3);
					String trueVerb=objectEntity.substring(objectEntity.length()-3, objectEntity.length()-1);
				//	System.out.println(trueObj+"\t"+trueVerb);
					if (set.contains(trueVerb)) {
						pw.println(subject+"\t"+trueVerb+"/动词\t"+trueObj+"/专有名\t"+items[3]);
						pw.flush();
						find=true;
						count++;
						continue;
					}
				//	System.out.println(trueObj+"\t"+trueVerb);
				} else if (objectPos.equals("动词#专有名")) {
					String trueVerb=objectEntity.substring(0, objectEntity.length()-2);
					String trueObj=objectEntity.substring(objectEntity.length()-2);
					if (set.contains(trueVerb)) {
					//	System.out.println(trueObj+"\t"+trueVerb);
						pw.println(subject+"\t"+trueVerb+"/动词\t"+trueObj+"/专有名\t"+items[3]);
						pw.flush();
						find=true;
						count++;
						continue;
					}
				} else if (objectPos.equals("动词#专有名#结构助词")) {
					String trueVerb=objectEntity.substring(0, objectEntity.length()-3);
					String trueObj=objectEntity.substring(objectEntity.length()-3, objectEntity.length()-1);
					if (set.contains(trueVerb)) {
					//	System.out.println(trueObj+"\t"+trueVerb);
						pw.println(subject+"\t"+trueVerb+"/动词\t"+trueObj+"/专有名\t"+items[3]);
						pw.flush();
						find=true;
						count++;
						continue;
					}
				} else if (objectPos.equals("地名#动词")) {
					String trueObj=objectEntity.substring(0, objectEntity.length()-2);
					String trueVerb=objectEntity.substring(objectEntity.length()-2);
					if (set.contains(trueVerb)) {
						pw.println(subject+"\t"+trueVerb+"/动词\t"+trueObj+"/专有名\t"+items[3]);
						pw.flush();
						find=true;
						count++;
						continue;
					}
				}
			}
			if (!find) {
				pw1.println(line);
				pw1.flush();
			}
		}
		br.close();
		pw.close();
		pw1.close();
		System.out.println(count);
	}

}
