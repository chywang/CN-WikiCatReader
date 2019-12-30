package mrpd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CommonSenseStat {

	public static void main(String[] args) throws IOException {

		BufferedReader br=new BufferedReader(new FileReader(new File("basic_remain.txt")));
		String line;
		int count=0;
		int t_count=0;
		int s_count=0;

		while ((line=br.readLine())!=null) {
			String[] items=line.split("\t");
			String subject=items[0];
			String verb=items[1];
			if (items[1].indexOf("/")>=0)
				verb=items[1].substring(0, items[1].indexOf("/"));
			String object=items[2].substring(0, items[2].indexOf("/"));
			String objectPos=items[2].substring(items[2].indexOf("/")+1);

			//temporal
			if (object.endsWith("月")) {
				count++;
				t_count++;
			} else if (object.endsWith("世纪")) {
				count++;
				t_count++;
			} else if (object.endsWith("年")) {
				count++;
				t_count++;
			} else if (object.endsWith("年代")) {
				count++;
				t_count++;
			} else if (object.indexOf("年")>=0 && (object.endsWith("会") || object.endsWith("赛"))) {
				count++;
				verb="参加";
			}
			//spatial
			if (objectPos.indexOf("地名")>=0 && object.length()>=2) {
				count++;
				s_count++;
			}
		}
		br.close();
		System.out.println(count);
		System.out.println(s_count);
		System.out.println(t_count);
	}

}
