package crg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RawRelationCount {

	public static void main(String[] args) throws IOException {
		Map<String, Integer> coutMap=new HashMap<String, Integer>();
		BufferedReader br=new BufferedReader(new FileReader(new File("can_relation_raw.txt")));
		String line;
		while ((line=br.readLine())!=null) {
			if (!coutMap.containsKey(line))
				coutMap.put(line, 1);
			else {
				int count=coutMap.get(line);
				count++;
				coutMap.put(line, count);
			}
		}
		br.close();
		List<String> list=new ArrayList<String>(coutMap.keySet());
		Collections.sort(list);
		System.out.println(list.size());
		PrintWriter pw=new PrintWriter("can_relation_raw_count.txt");
		for (String s:list) {
			pw.println(s+"\t"+coutMap.get(s));
			pw.flush();
		}
		pw.close();
	}

}
