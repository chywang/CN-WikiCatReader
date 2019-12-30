package mps;

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

public class ModSegmenter {

	public final static double small = 0.01;
	public final static double alpha = 30;

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
		Map<String, Integer> countMap = new HashMap<String, Integer>();
		Map<String, Double[]> embeddingMap = new HashMap<String, Double[]>();
		final int embeddingSize = 100;

		BufferedReader br = new BufferedReader(new FileReader(new File("uni_count.txt")));
		String line;
		while ((line = br.readLine()) != null) {
			String[] items = line.split("\t");
			countMap.put(items[0], Integer.parseInt(items[1]));
		}
		br.close();
		br = new BufferedReader(new FileReader(new File("bi_count.txt")));
		while ((line = br.readLine()) != null) {
			String[] items = line.split("\t");
			countMap.put(items[0], Integer.parseInt(items[1]));
		}
		br.close();
		br = new BufferedReader(new FileReader(new File("tri_count.txt")));
		while ((line = br.readLine()) != null) {
			String[] items = line.split("\t");
			countMap.put(items[0], Integer.parseInt(items[1]));
		}
		br.close();
		br = new BufferedReader(new FileReader(new File("qdi_count.txt")));
		while ((line = br.readLine()) != null) {
			String[] items = line.split("\t");
			countMap.put(items[0], Integer.parseInt(items[1]));
		}
		br.close();
		br = new BufferedReader(new FileReader(new File("embeddings.txt")));
		while ((line = br.readLine()) != null) {
			String[] items = line.split("\t");
			String entity = items[0];
			Double[] embeddings = new Double[embeddingSize];
			for (int i = 0; i < embeddingSize; i++)
				embeddings[i] = Double.parseDouble(items[i + 1]);
			embeddingMap.put(entity, embeddings);
		}

		PrintWriter pw = new PrintWriter("cat_mod_seg.txt");
		br = new BufferedReader(new FileReader(new File("cat_seg.txt")));
		while ((line = br.readLine()) != null) {
			String[] items = line.split("\t");
			String entity = items[0];
			String category = items[1];
			String tagResult = items[2];
			// if (!tagResult.endsWith("/名词") && !tagResult.endsWith("/专有名"))
			// continue;
			String[] tags = tagResult.split(" ");
			if (tags.length <= 2)
				continue;
			System.out.println(entity + "\t" + category + "\t" + tagResult);
			try {
				String result = graphProcess(tagResult, countMap, embeddingMap);
				pw.println(entity + "\t" + category + "\t" + tagResult + "\t" + result);
				pw.flush();
			} catch (Exception e) {
				continue;
			}
		}
		br.close();
		pw.close();
	}


	public static String graphProcess(String tagResult, Map<String, Integer> countMap, Map<String, Double[]> embeddingMap)
			throws Exception {
		// weight
		final double gamma = 0.5;
		// create entities
		String[] tags = tagResult.split(" ");
		// merge tokens
		tags = mergeBasedOnPositiveRules(tags);

		// create uni-entities
		List<String> entities = new ArrayList<String>();
		for (int i = 0; i < tags.length; i++) {
			String first = tags[i] + "/" + i;
			entities.add(first);
		}
		// create bi-entities
		for (int i = 0; i < tags.length - 1; i++) {
			if (matchNegativeRuleForBiEntity(tags[i], tags[i + 1]))
				continue;
			String first = tags[i] + "/" + i;
			String second = tags[i + 1] + "/" + (i + 1);
			entities.add(first + " " + second);
		}
		// create tri-entities
		for (int i = 0; i < tags.length - 2; i++) {
			if (matchNegativeRuleForTriEntity(tags[i], tags[i + 1], tags[i + 2]))
				continue;
			String first = tags[i] + "/" + i;
			String second = tags[i + 1] + "/" + (i + 1);
			String third = tags[i + 2] + "/" + (i + 2);
			entities.add(first + " " + second + " " + third);
		}

		// build graph
		Map<String, Double> edgeWeights = new HashMap<String, Double>();
		Map<String, Set<String>> graphStructure = new HashMap<String, Set<String>>();
		List<String> entityList = new ArrayList<String>(entities);
		for (int i = 0; i < entityList.size(); i++) {
			for (int j = i + 1; j < entityList.size(); j++) {
				if (i == j)
					continue;
				String ei = entityList.get(i);
				String ej = entityList.get(j);
				if (isOverlap(ei, ej) || matchNegativeRule(ei, ej)) {
					// System.out.println(ei+" "+ej+" overlap");
					continue;
				}
				// System.out.println(ei+"\t"+ej);
				double sim = gamma * entityEdgeWeight(ei, ej, countMap)
						+ (1 - gamma) * entityEmbeddingWeight(ei, ej, embeddingMap);
				if (sim > 0) {
					edgeWeights.put(ei + "\t" + ej, sim);
					edgeWeights.put(ej + "\t" + ei, sim);
					if (!graphStructure.containsKey(ei)) {
						Set<String> set = new HashSet<String>();
						set.add(ej);
						graphStructure.put(ei, set);

					} else {
						Set<String> set = graphStructure.get(ei);
						set.add(ej);
						graphStructure.put(ei, set);
					}
					if (!graphStructure.containsKey(ej)) {
						Set<String> set = new HashSet<String>();
						set.add(ei);
						graphStructure.put(ej, set);

					} else {
						Set<String> set = graphStructure.get(ej);
						set.add(ei);
						graphStructure.put(ej, set);
					}
				}

			}
		}
		CliqueReturnResult result = null;
		double score = 0;
		for (int k = 0; k < 10; k++) {
			CliqueReturnResult temp = getClique(edgeWeights, graphStructure);
			if (temp.conf > score) {
				score = temp.conf;
				result = temp;
			}
		}
		String outcome = generateNewModSeg(result.clique);
		return outcome;
	}

	public static CliqueReturnResult getClique(Map<String, Double> edgeWeights,
			Map<String, Set<String>> graphStructure) {
		// first, select two nodes (one edge)
		String selectedEdge = "";
		double totalWeights = 0;
		double ran = Math.random();
		double currentWeight = 0;
		for (String s : edgeWeights.keySet()) {
			currentWeight += edgeWeights.get(s) / totalWeights / 2;
			if (currentWeight >= ran) {
				// selected
				selectedEdge = s;
				break;
			}
		}
		// System.out.println("edge selected "+selectedEdge);
		String[] items = selectedEdge.split("\t");
		String ei = items[0];
		String ej = items[1];
		// ei, ej selected
		Set<String> cliqueSet = new HashSet<String>();
		cliqueSet.add(ei);
		cliqueSet.add(ei);
		// next, get all possible nodes can be selected
		Set<String> candidateNodes = new HashSet<String>();
		candidateNodes.addAll(graphStructure.get(ei));
		candidateNodes.addAll(graphStructure.get(ej));
		Map<String, Double> canBeSelectedNodes = new HashMap<String, Double>();
		for (String s1 : candidateNodes) {
			if (cliqueSet.contains(s1)) // if explored, discard
				continue;
			// now check clique
			double tempWeight = 0;
			boolean check = true;
			for (String s2 : cliqueSet) {
				if (!edgeWeights.containsKey(s1 + "\t" + s2)) {
					check = false;
					break;
				}
				tempWeight += edgeWeights.get(s1 + "\t" + s2);
			}
			if (check)
				canBeSelectedNodes.put(ei, tempWeight);
		}
		while (!canBeSelectedNodes.isEmpty()) {
			// randomly select a node
			String selectednode1 = "";
			double totalWeights1 = 0;
			for (String s : canBeSelectedNodes.keySet())
				totalWeights1 += canBeSelectedNodes.get(s);
			double ran1 = Math.random();
			double currentWeight1 = 0;
			for (String s : canBeSelectedNodes.keySet()) {
				currentWeight1 += canBeSelectedNodes.get(s) / totalWeights1;
				if (currentWeight1 >= ran1) {
					// selected
					selectednode1 = s;
					break;
				}
			}
			cliqueSet.add(selectednode1);

			// now node selected, find neighbor nodes
			Set<String> tempCandidateNodes = new HashSet<String>();
			tempCandidateNodes.addAll(graphStructure.get(selectednode1));
			Map<String, Double> tempCanBeSelectedNodes = new HashMap<String, Double>();
			for (String s1 : tempCandidateNodes) {
				if (cliqueSet.contains(s1)) // if explored, discard
					continue;
				// now check clique
				double tempWeight = 0;
				boolean check = true;
				for (String s2 : cliqueSet) {
					if (!edgeWeights.containsKey(s1 + "\t" + s2)) {
						check = false;
						break;
					}
					tempWeight += edgeWeights.get(s1 + "\t" + s2);
				}
				if (check)
					tempCanBeSelectedNodes.put(s1, tempWeight);
			}
			canBeSelectedNodes = tempCanBeSelectedNodes;
		}

		// calculate clique weights
		double cliqueWeights = 0;
		List<String> cliqueList = new ArrayList<String>(cliqueSet);
		Collections.sort(cliqueList);
		for (int i = 0; i < cliqueList.size(); i++) {
			for (int j = i + 1; j < cliqueList.size(); j++) {
				if (i == j)
					continue;
				cliqueWeights += edgeWeights.get(cliqueList.get(i) + "\t" + cliqueList.get(j));
			}
		}
		cliqueWeights = cliqueWeights / (1 + alpha * cliqueSet.size());
		return new CliqueReturnResult(cliqueSet, cliqueWeights);
	}

	public static double npmi(String first, String second, Map<String, Integer> map) {
		double pxy = p(first + " " + second, map);
		double px = p(first, map);
		double py = p(second, map);
		return Math.tanh(2 * Math.log(pxy / (px * py)) / (-Math.log(pxy)));
	}

	public static double npmiMod(String first, String second, Map<String, Integer> map) {
		return npmi(first, second, map) / 2 + 0.5;
	}

	public static double p(String x, Map<String, Integer> countMap) {
		int count = 0;
		if (countMap.containsKey(x))
			count = countMap.get(x);
		return (count + small) / (100000 + small);
	}

	public static double entityEdgeWeight(String first, String second, Map<String, Integer> map) {
		if (isNeighbor(first, second))
			return npmiMod(getAllTokens(first), getAllTokens(second), map);// also
																			// consider
																			// rules
		return small;
	}

	public static double entityEmbeddingWeight(String first, String second, Map<String, Double[]> embeddingMap) {
		if (isNeighbor(first, second)) {
			Double[] leftVector = embeddingMap.get(first + second);
			Double[] rightVector = embeddingMap.get(first);
			Double[] secondVector = embeddingMap.get(second);
			for (int i = 0; i < rightVector.length; i++)
				rightVector[i] += secondVector[i];
			return 0.5 * (1 - consineVectorSimilarity(leftVector, rightVector));
		}
		return small;
	}

	public static double consineVectorSimilarity(Double[] leftVector, Double[] rightVector) {
		if (leftVector.length != rightVector.length)
			return 1;
		double dotProduct = 0;
		double leftNorm = 0;
		double rightNorm = 0;
		for (int i = 0; i < leftVector.length; i++) {
			dotProduct += leftVector[i] * rightVector[i];
			leftNorm += leftVector[i] * leftVector[i];
			rightNorm += rightVector[i] * rightVector[i];
		}

		double result = dotProduct / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
		return result;
	}

	public static boolean matchPositiveRule(String first, String second) {
		String firstPos = first.substring(first.indexOf("/") + 1);
		String secondPos = second.substring(second.indexOf("/") + 1);
		if (firstPos.indexOf("#") >= 0)
			firstPos = firstPos.substring(firstPos.lastIndexOf("#") + 1);
		if (secondPos.indexOf("#") >= 0)
			secondPos = secondPos.substring(0, secondPos.indexOf("#"));
		if (firstPos.equals("并列连词") || secondPos.equals("并列连词"))
			return true;
		if (firstPos.equals("序数词") && secondPos.equals("量词"))
			return true;
		if (firstPos.equals("限定词") && secondPos.equals("量词"))
			return true;
		if (firstPos.equals("动词") && secondPos.equals("介词"))
			return true;
		return false;
	}

	public static String[] mergeBasedOnPositiveRules(String[] s) {
		int i = 0;
		String previous = "";
		String current = "";
		while (i < s.length) {
			current = s[i];
			if (matchPositiveRule(previous, current)) {
				previous = mergeTwoRawTokens(previous, current);
				i++;
			} else {
				previous = previous + " " + current;
				i++;
			}
		}
		previous = previous.trim();
		return previous.split(" ");
	}

	public static String mergeTwoRawTokens(String a, String b) {
		String aWord = a.substring(0, a.indexOf("/"));
		String aPos = a.substring(a.indexOf("/") + 1);
		String bWord = b.substring(0, b.indexOf("/"));
		String bPos = b.substring(b.indexOf("/") + 1);
		return aWord + bWord + "/" + aPos + "#" + bPos;
	}

	// implement must-link and must-not-link
	public static boolean matchNegativeRule(String first, String second) {
		String firstPos = first.substring(first.indexOf("/") + 1, first.lastIndexOf("/"));
		String secondPos = second.substring(second.indexOf("/") + 1, second.lastIndexOf("/"));
		if (firstPos.indexOf("#") >= 0)
			firstPos = firstPos.substring(firstPos.lastIndexOf("#") + 1);
		if (secondPos.indexOf("#") >= 0)
			secondPos = secondPos.substring(0, secondPos.indexOf("#"));
		if (firstPos.equals("结构助词") || secondPos.equals("结构助词"))
			return true;
		return false;
	}

	public static boolean matchNegativeRuleForBiEntity(String first, String second) {
		String firstPos = first.substring(first.indexOf("/") + 1);
		String secondPos = second.substring(second.indexOf("/") + 1);
		if (firstPos.indexOf("#") >= 0)
			firstPos = firstPos.substring(firstPos.lastIndexOf("#") + 1);
		if (secondPos.indexOf("#") >= 0)
			secondPos = secondPos.substring(0, secondPos.indexOf("#"));
		if (firstPos.equals("结构助词") || secondPos.equals("结构助词"))
			return true;
		return false;
	}

	public static boolean matchNegativeRuleForTriEntity(String first, String second, String third) {
		String firstPos = first.substring(first.indexOf("/") + 1);
		String secondPos = second.substring(second.indexOf("/") + 1);
		String thirdPos = second.substring(second.indexOf("/") + 1);

		if (firstPos.indexOf("#") >= 0)
			firstPos = firstPos.substring(firstPos.lastIndexOf("#") + 1);
		String secondPos1 = "";
		String secondPos2 = "";
		if (secondPos.indexOf("#") >= 0) {
			secondPos1 = secondPos.substring(0, secondPos.indexOf("#"));
			secondPos2 = secondPos.substring(secondPos.lastIndexOf("#") + 1);
		}
		if (thirdPos.indexOf("#") >= 0)
			thirdPos = thirdPos.substring(0, thirdPos.indexOf("#"));

		if (firstPos.equals("结构助词") || secondPos1.equals("结构助词") || secondPos2.equals("结构助词")
				|| thirdPos.equals("结构助词"))
			return true;
		return false;
	}

	public static boolean isOverlap(String first, String second) {
		Set<Integer> firstIds = new HashSet<Integer>();
		Set<Integer> secondIds = new HashSet<Integer>();
		try {
			String[] items1 = first.split(" ");
			for (String s : items1) {
				String[] temps = s.split("/");
				firstIds.add(Integer.parseInt(temps[2]));
			}
			String[] items2 = second.split(" ");
			for (String s : items2) {
				String[] temps = s.split("/");
				secondIds.add(Integer.parseInt(temps[2]));
			}
		} catch (Exception e) {
			return false;
		}

		for (int i : firstIds) {
			if (secondIds.contains(i))
				return true;
		}
		return false;
	}

	public static boolean isNeighbor(String first, String second) {
		try {
			String[] items1 = first.split(" ");
			int firstStart = Integer.parseInt(items1[0].split("/")[2]);
			int firstEnd = Integer.parseInt(items1[items1.length - 1].split("/")[2]);

			String[] items2 = second.split(" ");
			int secondStart = Integer.parseInt(items2[0].split("/")[2]);
			int secondEnd = Integer.parseInt(items2[items2.length - 1].split("/")[2]);

			if (firstEnd + 1 == secondStart || secondEnd + 1 == firstStart)
				return true;
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public static String getAllTokens(String input) {
		String[] items = input.split(" ");
		String outcome = "";
		for (String s : items)
			outcome += s.split("\t")[0];
		return outcome;
	}

	public static String generateNewModSeg(Set<String> set) {
		String outcome = "";
		List<ComString> list = new ArrayList<ComString>();
		for (String s1 : set)
			list.add(new ComString(s1));
		Collections.sort(list);
		for (ComString s1 : list) {
			outcome += mergeTokens(s1.s) + " ";
		}
		return outcome.trim();
	}

	public static String mergeTokens(String s) {
		String[] items = s.split(" ");
		if (items.length == 1)
			return s.substring(0, s.lastIndexOf("/"));
		else {
			String outcome = "";
			String pos = "";
			for (String s1 : items) {
				outcome += s1.substring(0, s1.indexOf("/"));
				pos += s1.substring(s1.indexOf("/") + 1, s1.lastIndexOf("/")) + "#";
			}
			if (pos.endsWith("#"))
				pos = pos.substring(0, pos.length() - 1);
			return outcome + "/" + pos;
		}
	}

	public static class ComString implements Comparable<ComString> {
		private String s;

		public ComString(String s) {
			super();
			this.s = s;
		}

		public String getS() {
			return s;
		}

		public void setS(String s) {
			this.s = s;
		}

		@Override
		public int compareTo(ComString o) {
			String thisIndex = s.split(" ")[0].split("/")[2];
			String otherIndex = o.getS().split(" ")[0].split("/")[2];
			return thisIndex.compareTo(otherIndex);
		}
	}

}
