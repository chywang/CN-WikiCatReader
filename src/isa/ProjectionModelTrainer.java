package isa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import com.ansj.vec.Word2VEC;

import Jama.Matrix;

public class ProjectionModelTrainer {

	private Matrix mMatrixPos;
	private Matrix mMatrixNeg;
	private Matrix bMatrixPos;
	private Matrix bMatrixNeg;
	private Word2VEC word2vec;
	private Map<Matrix, Matrix> positiveMap;
	private Map<Matrix, Matrix> negativeMap;
	
	private final int dimension=50;
	private final String w2vModel="/Users/bear/Documents/workspace/Hearst/javaSkip50.model";
	
	public ProjectionModelTrainer() throws IOException {
		word2vec = new Word2VEC();
		word2vec.loadJavaModel(w2vModel);
		System.out.println("word2vec load");
		mMatrixPos=new Matrix(dimension, dimension, Math.random());
		mMatrixNeg=new Matrix(dimension, dimension, Math.random());
		bMatrixPos=new Matrix(dimension, 1, Math.random());
		bMatrixNeg=new Matrix(dimension, 1, Math.random());
		positiveMap=new HashMap<Matrix, Matrix>();
		negativeMap=new HashMap<Matrix, Matrix>();
		loadTrainingSet();
		System.out.println("training set load");
	}
	
	private void loadTrainingSet() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File("positive.txt")));
		String line;
		while ((line = br.readLine()) != null) {
			String[] items=line.split("\t");
			if (items.length!=2)
				continue;
			String hypo=items[0];
			String hyper=items[1];
			float[] hf1=word2vec.getWordVector(hypo);
			double[][] ds1=new double[hf1.length][1];
			for (int i=0;i<hf1.length;i++)
				ds1[i][0]=hf1[i];
			Matrix hypoM=new Matrix(ds1);
			float[] hf2=word2vec.getWordVector(hyper);
			double[][] ds2=new double[hf2.length][1];
			for (int i=0;i<hf2.length;i++)
				ds2[i][0]=hf2[i];
			Matrix hyperM=new Matrix(ds2);
			positiveMap.put(hypoM, hyperM);
		}
		br.close();
		br = new BufferedReader(new FileReader(new File("negative.txt")));
		while ((line = br.readLine()) != null) {
			String[] items=line.split("\t");
			if (items.length!=2)
				continue;
			String hypo=items[0];
			String hyper=items[1];
			float[] hf1=word2vec.getWordVector(hypo);
			double[][] ds1=new double[hf1.length][1];
			for (int i=0;i<hf1.length;i++)
				ds1[i][0]=hf1[i];
			Matrix hypoM=new Matrix(ds1);
			float[] hf2=word2vec.getWordVector(hyper);
			double[][] ds2=new double[hf2.length][1];
			for (int i=0;i<hf2.length;i++)
				ds2[i][0]=hf2[i];
			Matrix hyperM=new Matrix(ds2);
			negativeMap.put(hypoM, hyperM);
		}
		br.close();
	}
	
	public void trainPositive() throws IOException {
		System.out.println("start training...");
		double lambda=0.0001;
		double eta=0.00025;
		int iterNo=0;
		while (true) {
			Matrix mUpdate=new Matrix(dimension,dimension,0);
			Matrix bUpdate=new Matrix(dimension,1,0);
			for (Matrix x:positiveMap.keySet()) {
				Matrix y=positiveMap.get(x);
				mUpdate=mUpdate.plus(mMatrixPos.times(x).times(x.transpose()).minus(y.times(x.transpose())).plus(bMatrixPos.times(x.transpose())));
				bUpdate=bUpdate.plus(mMatrixPos.times(x).minus(y).plus(bMatrixPos));
			}
			mUpdate=mUpdate.plus(mMatrixPos.times(lambda));
			bUpdate=bUpdate.plus(bMatrixPos.times(lambda));

			Matrix nNew=mMatrixPos.minus(mUpdate.times(eta));
			Matrix bNew=bMatrixPos.minus(bUpdate.times(eta));

			System.out.println(mMatrixPos.minus(nNew).normF());
			System.out.println(bMatrixPos.minus(bNew).normF());

			if (mMatrixPos.minus(nNew).normF()<0.01 && bMatrixPos.minus(bNew).normF()<0.01) {
				printM(mMatrixPos, "m_positive");
				printM(bMatrixPos, "b_positive");
				break;
			} else {
				mMatrixPos=nNew;
				bMatrixPos=bNew;
			}
			iterNo++;
			System.out.println("iter no. "+iterNo);
		}
		System.out.println("end training...");
	}
	
	public void trainNegative() throws IOException {
		System.out.println("start training...");
		double lambda=0.0001;
		double eta=0.00025;
		int iterNo=0;
		while (true) {
			Matrix mUpdate=new Matrix(dimension,dimension,0);
			Matrix bUpdate=new Matrix(dimension,1,0);
			for (Matrix x:negativeMap.keySet()) {
				Matrix y=negativeMap.get(x);
				mUpdate=mUpdate.plus(mMatrixNeg.times(x).times(x.transpose()).minus(y.times(x.transpose())).plus(bMatrixNeg.times(x.transpose())));
				bUpdate=bUpdate.plus(mMatrixNeg.times(x).minus(y).plus(bMatrixNeg));
			}
			mUpdate=mUpdate.plus(mMatrixNeg.times(lambda));
			bUpdate=bUpdate.plus(bMatrixNeg.times(lambda));

			Matrix nNew=mMatrixNeg.minus(mUpdate.times(eta));
			Matrix bNew=bMatrixNeg.minus(bUpdate.times(eta));

			System.out.println(mMatrixPos.minus(nNew).normF());
			System.out.println(bMatrixPos.minus(bNew).normF());
			
			if (mMatrixNeg.minus(nNew).normF()<0.01 && bMatrixNeg.minus(bNew).normF()<0.01) {
				printM(mMatrixNeg, "m_negative");
				printM(bMatrixNeg, "b_negative");
				break;
			} else {
				mMatrixNeg=nNew;
				bMatrixNeg=bNew;
			}
			iterNo++;
			System.out.println("iter no. "+iterNo);
		}
		System.out.println("end training...");
	}
	
	public void printM(Matrix mMatrix, String filename) throws IOException {
		PrintWriter pw=new PrintWriter("proj/"+filename+".txt");
		for (int i=0;i<mMatrix.getRowDimension();i++) {
			for (int j=0;j<mMatrix.getColumnDimension();j++)
				pw.print(mMatrix.get(i, j)+" ");
			pw.println();
			pw.flush();
		}
		pw.close();
	}
	
	public static void main(String[] args) throws IOException {
		ProjectionModelTrainer model=new ProjectionModelTrainer();
		model.trainPositive();
		model.trainNegative();
	}

}
