package isa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ansj.vec.Word2VEC;

import Jama.Matrix;
import edu.fudan.nlp.cn.tag.POSTagger;

public class ProjectionBasedIsAGenerator {
	
	private POSTagger tag;
	private Word2VEC word2vec;
	private Matrix mMatrixPos;
	private Matrix mMatrixNeg;
	private Matrix bMatrixPos;
	private Matrix bMatrixNeg;
	private Map<String, Set<String>> map;
	
	private final int dimension=50;
	private final String w2vModel="/Users/bear/Documents/workspace/Hearst/javaSkip50.model";
	
	public ProjectionBasedIsAGenerator() throws Exception {
		tag = new POSTagger("fdnlp/seg.m","fdnlp/pos.m");
		//load word2vec
		word2vec = new Word2VEC();
		word2vec.loadJavaModel(w2vModel);
		System.out.println("word2vec load ok!");
		//load matrix for projection
		loadMatrix();
		System.out.println("matrix load ok!");
		//load unlabeled data
		map=new HashMap<String,Set<String>>();
		BufferedReader br = new BufferedReader(new FileReader(new File("cat-1.txt")));
		String line;
		while ((line = br.readLine()) != null) {
			String[] items=line.split("\t");
			String entity=items[0];
			Set<String> categories=new HashSet<String>();
			for (int i=1;i<items.length;i++) {
				String category=items[i];
				categories.add(category);
			}
			map.put(entity, categories);
		}
		br.close();
	}
	
	private void loadMatrix() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File("proj/m_positive.txt")));
		String line;
		double[][] ds=new double[dimension][dimension];
		int count=0;
		while ((line = br.readLine()) != null) {
			String[] items=line.split(" ");
			for (int i=0;i<items.length;i++)
				ds[count][i]=Double.parseDouble(items[i]);
			count++;
		}
		br.close();
		mMatrixPos=new Matrix(ds);
		
		br = new BufferedReader(new FileReader(new File("proj/m_negative.txt")));
		ds=new double[dimension][dimension];
		count=0;
		while ((line = br.readLine()) != null) {
			String[] items=line.split(" ");
			for (int i=0;i<items.length;i++)
				ds[count][i]=Double.parseDouble(items[i]);
			count++;
		}
		br.close();
		mMatrixNeg=new Matrix(ds);
		
		br = new BufferedReader(new FileReader(new File("proj/b_positive.txt")));
		ds=new double[dimension][1];
		count=0;
		while ((line = br.readLine()) != null) {
			ds[count][0]=Double.parseDouble(line);
			count++;
		}
		br.close();
		bMatrixPos=new Matrix(ds);
		
		br = new BufferedReader(new FileReader(new File("proj/b_negative.txt")));
		ds=new double[dimension][1];
		count=0;
		while ((line = br.readLine()) != null) {
			ds[count][0]=Double.parseDouble(line);
			count++;
		}
		br.close();
		bMatrixNeg=new Matrix(ds);
	}
	
	public void predict() throws FileNotFoundException {
		PrintWriter pw=new PrintWriter("proj-predict-scores.txt");
		for (String s:map.keySet()) {
			Set<String> categories=map.get(s);
			for (String c:categories) {
				try {
					String hypo=s;
					if (hypo.indexOf(" (")>=0 )
						hypo=hypo.substring(0, hypo.indexOf(" ("));
					String hyper=getHead(c);
					float[] hf1=word2vec.getWordVector(hypo);
					if (hf1==null)
						continue;
					double[][] ds1=new double[hf1.length][1];
					for (int i=0;i<hf1.length;i++)
						ds1[i][0]=hf1[i];
					Matrix hypoM=new Matrix(ds1);
					float[] hf2=word2vec.getWordVector(hyper);
					if (hf2==null)
						continue;
					double[][] ds2=new double[hf2.length][1];
					for (int i=0;i<hf2.length;i++)
						ds2[i][0]=hf2[i];
					Matrix hyperM=new Matrix(ds2);
					
					double positiveScore=mMatrixPos.times(hypoM).plus(bMatrixPos).minus(hyperM).normF();
					double negaticeScore=mMatrixNeg.times(hypoM).plus(bMatrixNeg).minus(hyperM).normF();
					double prob=Math.tanh(negaticeScore-positiveScore);
					
					pw.println(s+"\t"+c+"\t"+prob);
					pw.flush();
				} catch (Exception e) {
					continue;
				}
				
			}
		}
		pw.close();
	}

	private String getHead(String category)  {
		String[] items=tag.tag(category).split(" ");
		String keyword=items[items.length-1];
		String word=keyword.substring(0, keyword.indexOf("/"));
		String tag=keyword.substring(keyword.indexOf("/")+1);
		if (tag.equals("动词"))
			return null;
		if (word.equals("划") && category.indexOf("行政区划")>=0)
			return "行政区划";
		else
			return word;
	}
	
	public void finalPredict() throws Exception {
		class Stat {
			int count;
			double sum;
			public Stat(int count, double sum) {
				super();
				this.count = count;
				this.sum = sum;
			}
			public void addWeight(double weight) {
				count++;
				sum+=weight;
			}
			public double getAvg() {
				return Math.log1p(count)*sum/count;
			}
		}
		Map<String, Stat> map=new HashMap<String, Stat>();
		
		//projection based scores
		BufferedReader br = new BufferedReader(new FileReader(new File("proj-predict-scores.txt")));
		String line;
		while ((line = br.readLine()) != null) {
			String[] items=line.split("\t");
			String category=items[1];
			String head=getHead(category);
			double weight=Double.parseDouble(items[2]);
			if (!map.containsKey(head))
				map.put(head, new Stat(1, weight));
			else {
				Stat stat=map.get(head);
				stat.addWeight(weight);
				map.put(head, stat);
			}
		}
		br.close();
		
		//rule based scores
		br = new BufferedReader(new FileReader(new File("rule-based-isa.txt")));
		while ((line = br.readLine()) != null) {
			String[] items=line.split("\t");
			for (int i=1;i<items.length;i++) {
				String category=items[i];
				String head=getHead(category);
				if (!map.containsKey(head))
					map.put(head, new Stat(1, 1));
				else {
					Stat stat=map.get(head);
					stat.addWeight(1);
					map.put(head, stat);
				}
			}
		}
		br.close();
		/*
		for (String s:map.keySet()) {
			System.out.println(s+"\t"+map.get(s).getAvg());
		}
		*/
			
		double lambda=0.7;
		br = new BufferedReader(new FileReader(new File("proj-predict-scores.txt")));
		PrintWriter pw=new PrintWriter("proj-predict-scores.txt");
		while ((line = br.readLine()) != null) {
			String[] items=line.split("\t");
			String entity=items[0];
			String category=items[1];
			double initialScore=Double.parseDouble(items[2]);
			String head=getHead(category);
			double globalScore=map.get(head).getAvg();
			double finalScore=lambda*initialScore+(1-lambda)*globalScore;
			pw.println(entity+"\t"+category+"\t"+finalScore);
			pw.flush();
		}
		pw.close();
	}
	
	
	
	public static void main(String[] args) throws Exception {
		ProjectionBasedIsAGenerator generator=new ProjectionBasedIsAGenerator();
		generator.predict();
		generator.finalPredict();
	}

}
