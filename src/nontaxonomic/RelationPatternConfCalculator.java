package nontaxonomic;

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

import com.ansj.vec.Word2VEC;

import edu.fudan.nlp.cn.tag.POSTagger;


public class RelationPatternConfCalculator {

	private Map<String, Set<String>> map;
	private Word2VEC word2vec;
	private POSTagger tag;
	
	public RelationPatternConfCalculator() throws Exception {
		tag = new POSTagger("fdnlp/seg.m","fdnlp/pos.m");
		//load word2vec
		word2vec = new Word2VEC();
		word2vec.loadJavaModel("/Users/bear/Documents/workspace/Hearst/javaSkip50.model");
		System.out.println("word2vec load ok!");
				
		map=new HashMap<String, Set<String>>();
		BufferedReader br=new BufferedReader(new FileReader(new File("cat.txt")));
		String line;
		while ((line=br.readLine())!=null) {
			String[] items=line.split("\t");
			String entity=items[0];
			if (entity.indexOf(" (")>=0 )
				entity=entity.substring(0, entity.indexOf(" ("));
			Set<String> categories=new HashSet<String>();
			for (int i=1;i<items.length;i++)
				categories.add(items[i]);
 			map.put(entity, categories);
		}
		br.close();
	}
	
	public CliqueReturnResult calculatePatternConf(String pattern, Set<String> entities) {
		int size=entities.size();
		if (entities.size()>20) {
			Set<String> newEntitySet=new HashSet<String>();
			for (String s:entities) {
				//reduce running time, can be removed
				if (Math.random()<(double)(50d/size))
					newEntitySet.add(s);
			}
			entities=newEntitySet;
		}
		double weights=0;
		Set<String> cliques=new HashSet<String>();
		int numOfIter=(int)(3*Math.random())+1;
		System.out.println(numOfIter+"\titerations");
		for (int i=0;i<numOfIter;i++) {
			System.out.println("iteration:\t"+i);
			CliqueReturnResult result=maxClique(pattern, entities);
			if (result.getConf()>=weights) {
				weights=result.getConf();
				cliques=result.getClique();
			}
		}
		return new CliqueReturnResult(cliques, weights);
	}
	
	private CliqueReturnResult maxClique(String pattern, Set<String> entities) {
		//detect a max clique
		double thres=0.7;
		Map<String, Double> edgeWeights=new HashMap<String, Double>();
		
		List<String> entityList=new ArrayList<String>(entities);
		Set<String> tempEntitySet=new HashSet<String>(entities);
		Collections.sort(entityList);
		double totalGraphWeights=0;
		for (int i=0;i<entityList.size();i++) {
			for (int j=i+1;j<entityList.size();j++) {
				if (i==j)
					continue;
				String ei=entityList.get(i);
				String ej=entityList.get(j);
				double sim=entitySim(ei, ej);
				if (sim>thres) {
					edgeWeights.put(ei+"\t"+ej, sim);
					totalGraphWeights+=sim;
				}
			}
		}
		Map<String, Double> edgeWeightsCopy=new HashMap<String, Double>(edgeWeights);

		Set<String> cliqueSet=new HashSet<String>();
		Map<String, Double> tempEdgeWeights=new HashMap<String, Double>(edgeWeights);
		//select edges
		while (!edgeWeights.isEmpty()) {
			//randomly select
			String selectedEdge="";
			double totalWeights=0;
			for (String s:edgeWeights.keySet())
				totalWeights+=edgeWeights.get(s);
			double ran=Math.random();
			double currentWeight=0;
			for (String s:edgeWeights.keySet()) {
				currentWeight+=edgeWeights.get(s)/totalWeights;
				if (currentWeight>=ran) {
					//selected
					selectedEdge=s;
					break;
				}
			}
			//now edge selected, remove it from the original
			String[] items=selectedEdge.split("\t");
			String ei=items[0];
			String ej=items[1];
			tempEntitySet.remove(ei);
			tempEntitySet.remove(ej);
			tempEdgeWeights.remove(selectedEdge);
			cliqueSet.add(ei);
			cliqueSet.add(ej);
			//remove non-clique edges
			for (String s:edgeWeights.keySet()) {
				String[] items1=s.split("\t");
				String i=items1[0];
				String j=items1[1];
				if (!cliqueSet.contains(i) && !cliqueSet.contains(j))
					tempEdgeWeights.remove(s);
			}
			edgeWeights=new HashMap<String, Double>(tempEdgeWeights);
		}
		
		//calculate weights
		edgeWeights=edgeWeightsCopy;
		
		double cliqueWeights=0;
		List<String> cliqueList=new ArrayList<String>(cliqueSet);
		Collections.sort(cliqueList);
		for (int i=0;i<cliqueList.size();i++) {
			for (int j=i+1;j<cliqueList.size();j++) {
				if (i==j)
					continue;
				if (edgeWeights.containsKey(cliqueList.get(i)+"\t"+cliqueList.get(j)))
					cliqueWeights+=edgeWeights.get(cliqueList.get(i)+"\t"+cliqueList.get(j));
			}
		}
		System.out.println("clique:\t"+cliqueWeights);
		System.out.println("total:\t"+totalGraphWeights);
		cliqueWeights=cliqueWeights/totalGraphWeights*Math.log(1+cliqueList.size());
		return new CliqueReturnResult(cliqueSet, cliqueWeights);
	}
	
	private double entitySim(String ei,String ej) {
		Set<String> ci=map.get(ej);
		Set<String> cj=map.get(ei);
		if (ci==null || cj==null)
			return 0;
		double totalSim=0;
		int totalCount=0;
		for (String si:ci) {
			for (String sj:cj) {
				float[] hf1=word2vec.getWordVector(getHead(si));
				float[] hf2=word2vec.getWordVector(getHead(sj));
				if (hf1==null || hf2==null)
					continue;
				totalSim+=cosineSim(hf1, hf2);
				totalCount++;
			}
		}
	//	System.out.println(ei+"\t"+ej);
	//	System.out.println("sim:\t"+totalSim/totalCount);
		return totalSim/totalCount;
	}
	
	private String getHead(String category) {
		String[] items=tag.tag(category).split(" ");
		String keyword=items[items.length-1];
		String word=keyword.substring(0, keyword.indexOf("/"));
		if (word.equals("划") && category.indexOf("行政区划")>=0)
			return "行政区划";
		else
			return word;
	}
	
	private double cosineSim(float[] hf1,float[] hf2) {
		double prod=0;
		for (int i=0;i<hf1.length;i++)
			prod+=hf1[i]*hf2[i];
		double s1=0;
		for (int i=0;i<hf1.length;i++)
			s1+=hf1[i]*hf1[i];
		double s2=0;
		for (int i=0;i<hf2.length;i++)
			s2+=hf2[i]*hf2[i];
		return prod/Math.sqrt(s1*s2);
	}
	
	public static class CliqueReturnResult {
		private Set<String> clique;
		private double conf;
		public CliqueReturnResult(Set<String> clique, double conf) {
			super();
			this.clique = clique;
			this.conf = conf;
		}
		public Set<String> getClique() {
			return clique;
		}
		public void setClique(Set<String> clique) {
			this.clique = clique;
		}
		public double getConf() {
			return conf;
		}
		public void setConf(double conf) {
			this.conf = conf;
		}
	}
	
	public static void main(String[] args) throws Exception {
		PrintWriter pw=new PrintWriter("patterns-relation-conf.txt");
		RelationPatternConfCalculator calculator=new RelationPatternConfCalculator();
		BufferedReader br=new BufferedReader(new FileReader(new File("patterns-relation.txt")));
		String line;
		while ((line=br.readLine())!=null) {
			System.out.println("now do this");
			System.out.println(line);
			String[] items=line.split("\t");
			String pattern=items[0];
			Set<String> entities=new HashSet<String>();
			for (int i=1;i<items.length;i++)
				entities.add(items[i]);
			if (entities.size()<10)
				continue;
			CliqueReturnResult result=calculator.calculatePatternConf(pattern, entities);
			pw.print(pattern+"\t"+result.getConf());
			System.out.println("confidence\t"+result.getConf());
			for (String s:result.getClique()) {
				System.out.print(s+"\t");
				pw.print("\t"+s);
			}
			System.out.println();
			pw.println();
			pw.flush();
		}
		br.close();
		pw.close();
	}

}
