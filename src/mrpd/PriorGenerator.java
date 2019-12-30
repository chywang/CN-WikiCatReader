package mrpd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class PriorGenerator {

	public static void main(String[] args) throws IOException {
		Map<String, Double> relationMap=new HashMap<String, Double>();
		BufferedReader br=new BufferedReader(new FileReader(new File("basic_relation.txt")));
		String line;
		while ((line=br.readLine())!=null) {
			String[] items=line.split("\t");
			String verb=items[1];
			if (!relationMap.containsKey(verb))
				relationMap.put(verb, 10d);
			else {
				double c=relationMap.get(verb);
				c++;
				relationMap.put(verb, c);
			}
		}
		br.close();

		relationMap.put("SPATIAL", 93687d);
		relationMap.put("TEMPORAL", 74911d);

		double count=0;
		for (String verb:relationMap.keySet()) {
			count+=relationMap.get(verb);
		}
		for (String verb:relationMap.keySet()) {
			double c=relationMap.get(verb);
			c=c/count;
			relationMap.put(verb, c);
		}
		
		PrintWriter pw=new PrintWriter("prior.txt");
		for (String verb:relationMap.keySet()) {
			pw.println(verb+"\t"+relationMap.get(verb));
			pw.flush();
		}
		pw.close();
	}

}
