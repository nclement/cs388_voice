import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import nlp.lm.Bigram;
import nlp.lm.BigramModel;
import nlp.lm.BigramValue;
import nlp.lm.POSTaggedVoiceFile;


public class CalculateBigrams {
	static final int K = 800;
	static final boolean print_all = true;
	static CommandLineOpts cmd;
	
	public static void main(String[] args) {
		cmd = new CommandLineOpts();
		JCommander jc;
		try {
			jc = new JCommander(cmd, args);
			if(cmd.help) {
				jc.usage();
				System.exit(-1);
			}
		}
		catch(ParameterException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		
		List<String> POSTaggedTrainingFiles;
		if(cmd.trainSingle) {
			POSTaggedTrainingFiles = new ArrayList<String>();
			POSTaggedTrainingFiles.add(cmd.taggedTrainFile);
		}
		else {
			POSTaggedTrainingFiles = getLinesFromFile(cmd.taggedTrainFile);
		}
		
		System.err.println("Number of tagged training files: "+POSTaggedTrainingFiles.size());
		for(String fn : POSTaggedTrainingFiles)
			System.err.println("\ttraining file is: "+fn);
		
		// Train all the objects
		BigramModel[] trainedUntaggedObjs = new BigramModel[POSTaggedTrainingFiles.size()];
		for(int i=0; i<POSTaggedTrainingFiles.size(); i++) {
//			System.err.println("Training file "+POSTaggedTrainingFiles.get(i));
			trainedUntaggedObjs[i] = new BigramModel(false);
			trainedUntaggedObjs[i].trainPOSTaggedWord(POSTaggedTrainingFiles.get(i));
		}
		BigramModel[] trainedTaggedObjs = new BigramModel[POSTaggedTrainingFiles.size()];
		for(int i=0; i<POSTaggedTrainingFiles.size(); i++) {
//			System.err.println("Training file "+POSTaggedTrainingFiles.get(i));
			trainedTaggedObjs[i] = new BigramModel(false);
			trainedTaggedObjs[i].trainPOSTaggedToken(POSTaggedTrainingFiles.get(i));
		}

		
		ArrayList<BigramValue> topUntaggedBigrams = getTopBigrams(trainedUntaggedObjs);
		ArrayList<BigramValue> topUntaggedUnigrams = getTopUnigrams(trainedUntaggedObjs);

		ArrayList<BigramValue> topTaggedBigrams = getTopBigrams(trainedTaggedObjs);
		//ArrayList<BigramValue> topTaggedUnigrams = getTopUnigrams(trainedTaggedObjs);

		System.err.println("Found a total of "+(topUntaggedBigrams.size()+topUntaggedUnigrams.size()+topTaggedBigrams.size())+ " total variables");
		System.err.println("  made up of topUntaggedBigrams:"+topUntaggedBigrams.size() + ", topUntaggedUnigrams:"+topUntaggedUnigrams.size()+", topTaggedBigrams:"+topTaggedBigrams.size());
		System.err.println("Printing SVM for tagged file ["+cmd.taggedTestFile+"]");
		if(!cmd.testSingle)
			getTrainForInputFiles(cmd.taggedTestFile, 
					topUntaggedBigrams, topUntaggedUnigrams, topTaggedBigrams, cmd.languageNum);
		else
			getTrainForFileUser(topUntaggedBigrams, topUntaggedUnigrams, topTaggedBigrams, cmd.languageNum, cmd.taggedTestFile);
	}

	private static void getTrainForInputFiles(String testDir,
			ArrayList<BigramValue> topUntaggedBigrams,
			ArrayList<BigramValue> topUntaggedUnigrams, 
			ArrayList<BigramValue> topTaggedBigrams,
			int languageNumber) {
		List<String> allFiles = getLinesFromFile(testDir);
		//int languageNumber = 0;
		for(int i=0; i<allFiles.size(); i++) {
			//languageNumber++;
			getTrainForFileUser(topUntaggedBigrams, topUntaggedUnigrams, 
					topTaggedBigrams, languageNumber, 
					allFiles.get(i));
		}
	}
	
	private static HashMap<Bigram, Integer> getUnigramCounts(List<List<String>> sentences, boolean tagged) {
		HashMap<Bigram, Integer> bigramCounts = new HashMap<Bigram, Integer>();
		
		for(List<String> sentence : sentences) {
			List<Bigram> bigrams;
			if(tagged)
				bigrams = POSTaggedVoiceFile.getSentenceUnigramTags(sentence);
			else
				bigrams = POSTaggedVoiceFile.getSentenceUnigramWords(sentence);
			
			for(Bigram b : bigrams) {
				if(bigramCounts.get(b) == null)
					bigramCounts.put(b, new Integer(1));
				else {
					// objects can be changed
					Integer i = bigramCounts.get(b);
					i = i.intValue()+1;
				}
			}
		}
		
		return bigramCounts;
	}
	
	private static HashMap<Bigram, Integer> getBigramCounts(List<List<String>> sentences, boolean tagged) {
		HashMap<Bigram, Integer> bigramCounts = new HashMap<Bigram, Integer>();
		
		for(List<String> sentence : sentences) {
			List<Bigram> bigrams;
			if(tagged)
				bigrams = POSTaggedVoiceFile.getSentenceBigramTags(sentence);
			else
				bigrams = POSTaggedVoiceFile.getSentenceBigramWords(sentence);
			
			for(Bigram b : bigrams) {
				if(bigramCounts.get(b) == null)
					bigramCounts.put(b, new Integer(1));
				else {
					// objects can be changed
					Integer i = bigramCounts.get(b);
					i = i.intValue()+1;
				}
			}
		}
		
		return bigramCounts;
	}
	
	private static int countWords(List<List<String>> l) {
		int i=0; 
		for(List<String> _l : l) {
			i+=_l.size();
		}
		return i;
	}
	private static void getTrainForFileUser(ArrayList<BigramValue> topUntaggedBigrams,
			ArrayList<BigramValue> topUntaggedUnigrams, 
			ArrayList<BigramValue> topTaggedBigrams, int languageNumber,
			String taggedTestFile) {
		List<List<String>> taggedTestSentences = POSTaggedVoiceFile.convertToTokenTagLists(new File(taggedTestFile));
		List<List<String>> untaggedTestSentences = POSTaggedVoiceFile.convertToTokenWordLists(new File(taggedTestFile));
		
		Map<Bigram, Integer> taggedBigrams = getBigramCounts(taggedTestSentences, true);
		Map<Bigram, Integer> untaggedBigrams = getBigramCounts(untaggedTestSentences, false);
		Map<Bigram, Integer> untaggedUnigrams = getUnigramCounts(untaggedTestSentences, false);
		
		int userWordCount = countWords(taggedTestSentences);
		int svmVal = 1;
		// Print out the language number
		System.out.print(languageNumber+" ");
		
		// Print out the total number of words
		System.out.print(svmVal+":"+userWordCount+" ");
		svmVal++;

		// first, loop over the untagged bigrams
		for(BigramValue bv : topUntaggedBigrams) {
			if(untaggedBigrams.get(bv.getBigram()) != null)
				System.out.print(svmVal+":"+((float)untaggedBigrams.get(bv.getBigram()).intValue()/userWordCount)+" ");
			svmVal++;
		}
		// next, loop over the untagged unigrams
		for(BigramValue bv : topUntaggedUnigrams) {
			if(untaggedUnigrams.get(bv.getBigram()) != null)
				System.out.print(svmVal+":"+((float)untaggedUnigrams.get(bv.getBigram()).intValue()/userWordCount)+" ");
			svmVal++;
		}
		// lastly, loop over the tagged bigrams
		for(BigramValue bv : topTaggedBigrams) {
			if(taggedBigrams.get(bv.getBigram()) != null)
				System.out.print(svmVal+":"+((float)taggedBigrams.get(bv.getBigram()).intValue()/userWordCount)+" ");
			svmVal++;
		}
		// add a comment at the end with the user that this line represents
		System.out.println("# "+taggedTestFile);

	}
	
	private static void getTrainForFileLang(ArrayList<BigramValue> topUntaggedBigrams,
			ArrayList<BigramValue> topUntaggedUnigrams, 
			ArrayList<BigramValue> topTaggedBigrams, int languageNumber,
			String taggedTestFile) {
		
		List<List<String>> taggedTestSentences = POSTaggedVoiceFile.convertToTokenTagLists(new File(taggedTestFile));
		List<List<String>> untaggedTestSentences = POSTaggedVoiceFile.convertToTokenWordLists(new File(taggedTestFile));
		
		for(int i=0; i<untaggedTestSentences.size(); i++) {
			List<String> untaggedSentence = untaggedTestSentences.get(i);
			System.out.print(languageNumber+" ");
			List<Bigram> bigrams = POSTaggedVoiceFile.getSentenceBigramWords(untaggedSentence);
			int svmCount = 1;
			// Use the size as the first variable
			System.out.print(svmCount+":"+untaggedSentence.size()+" ");
			svmCount++;
			
			// Then print out the top Bigrams
			for(int j=0; j<topUntaggedBigrams.size(); j++, svmCount++) {
				if(bigrams.contains(topUntaggedBigrams.get(j).getBigram()))
					System.out.print(svmCount+":"+1+" ");
			}
			
			// Then print out the top Unigrams
			List<Bigram> unigrams = POSTaggedVoiceFile.getSentenceUnigramWords(untaggedSentence);
			for(int j=0; j<topUntaggedUnigrams.size(); j++, svmCount++)
				if(unigrams.contains(topUntaggedUnigrams.get(j).getBigram()))
					System.out.print(svmCount+":"+1+" ");
			
			List<String> taggedSentence = taggedTestSentences.get(i);
			// Then print out the top Unigrams
			List<Bigram> taggedBigrams = POSTaggedVoiceFile.getSentenceBigramTags(taggedSentence);
			for(int j=0; j<topTaggedBigrams.size(); j++, svmCount++)
				if(taggedBigrams.contains(topTaggedBigrams.get(j).getBigram()))
					System.out.print(svmCount+":"+1+" ");

			System.out.println();
			
		}
	}

	private static List<String> getLinesFromFile(String fn) {
		BufferedReader reader;
		List<String> toRet = new ArrayList<String>();
		try {
			reader = new BufferedReader(new FileReader(new File(fn)));
			String line;
			while((line = reader.readLine()) != null)
				toRet.add(line);
		}
		catch(FileNotFoundException e) {
			System.err.println("ERROR:  Could not open file "+fn);
		}
		catch(IOException e) {
			System.err.println("ERROR:  IOEXception ("+e.getMessage()+") on file "+fn);
		}
	
		return toRet;
	}
	
	private static ArrayList<BigramValue> getTopBigrams(BigramModel[] trainedObjs) {
		HashMap<Bigram, Integer> topWordMap = new HashMap<Bigram, Integer>();
		for(int i=0; i<trainedObjs.length; i++) {
			List<Bigram> bigrams = trainedObjs[i].getAllBigrams();
			for(Bigram b : bigrams) {
				if(topWordMap.containsKey(b))
					topWordMap.put(b, topWordMap.get(b)+1);
				else
					topWordMap.put(b, new Integer(1));
			}
		}

		// make it sorted.
		ArrayList<BigramValue> sortedBigrams = new ArrayList<BigramValue>();
		for(Bigram key : topWordMap.keySet()) {
			sortedBigrams.add(new BigramValue(key, topWordMap.get(key)));
		}

		// Set individual counts before sorting
		for(BigramValue bv : sortedBigrams)
			setNgramCounts(trainedObjs, bv, true);
		
		// Sort first on minimized languages, then on maximized occurrences
		Collections.sort(sortedBigrams);
		
		if(cmd.verbose) {

			int[] counts = new int[18];
			for(BigramValue b : sortedBigrams)
				counts[(int)b.getValue()]++;

			int total_count = 0;
			for(int i=17; i>0; i--) {
				total_count += counts[i];
				System.err.println(i+": "+counts[i] + ", "+total_count);
			}

			int c=0;
			for(BigramValue b : sortedBigrams) {
				System.err.print(b);
				printNgramCounts(trainedObjs, b.getBigram(), true);
				System.err.print("\t");
				if(c++ > K && !print_all)
					break;
			}
			System.err.println();
		}

		ArrayList<BigramValue> toRet = new ArrayList<BigramValue>();
		if(!print_all) {
			// Remove the last ones
			for(int i=0; i<K; i++)
				toRet.add(sortedBigrams.get(i));
		}
		else {
			toRet.addAll(sortedBigrams);
		}
		
		return toRet;
	}

	private static ArrayList<BigramValue> getTopUnigrams(BigramModel[] trainedObjs) {
		HashMap<Bigram, Integer> topWordMap = new HashMap<Bigram, Integer>();
		for(int i=0; i<trainedObjs.length; i++) {
			List<Bigram> unigrams = trainedObjs[i].getAllUnigrams();
			for(Bigram b : unigrams) {
				if(topWordMap.containsKey(b))
					topWordMap.put(b, topWordMap.get(b)+1);
				else
					topWordMap.put(b, new Integer(1));
			}
		}
		
		// make it sorted.
		ArrayList<BigramValue> sortedUnigrams = new ArrayList<BigramValue>();
		for(Bigram key : topWordMap.keySet()) {
			sortedUnigrams.add(new BigramValue(key, topWordMap.get(key)));
		}
		
		// Set individual counts before sorting
		for(BigramValue bv : sortedUnigrams)
			setNgramCounts(trainedObjs, bv, false);
		
		Collections.sort(sortedUnigrams);

		/*
		int[] counts = new int[18];
		for(BigramValue b : sortedUnigrams)
			counts[(int)b.getValue()]++;
		int total_count = 0;
		for(int i=17; i>0; i--) {
			total_count += counts[i];
			System.err.println(i+": "+counts[i] + ", "+total_count);
		}
		*/
		
		if(cmd.verbose) {
			int c=0;
			for(BigramValue b : sortedUnigrams) {
				System.err.print(b);
				printNgramCounts(trainedObjs, b.getBigram(), false);
				System.err.print("\t");
				if(c++ > K && !print_all)
					break;
			}
			System.err.println();
		}
		
		ArrayList<BigramValue> toRet = new ArrayList<BigramValue>();
		if(!print_all) {
			// Remove the last ones
			for(int i=0; i<K && i<sortedUnigrams.size(); i++)
				toRet.add(sortedUnigrams.get(i));
		}
		else {
			toRet.addAll(sortedUnigrams);
		}
		return toRet;

	}
	
	private static void printNgramCounts(BigramModel[] trainedObjs, Bigram bigram, boolean isBigram) {
		int max = 0, total=0;
		System.err.print("{");
		for(int i=0; i<trainedObjs.length; i++) {
			Map<Bigram, Integer> temp;
			if(isBigram)
				temp = trainedObjs[i].getBigramCounts();
			else
				temp = trainedObjs[i].getUnigramCounts();
			
			Integer count = temp.get(bigram);
			if(count == null)
				System.err.print("0,");
			else {
				System.err.print(count.intValue()+",");
				max = max > count.intValue() ? max : count.intValue();
				total += count.intValue();
			}
		}
		System.err.print("}[max="+max+",total="+total+"]");
	}
	
	private static void setNgramCounts(BigramModel[] trainedObjs, BigramValue bigramValue, boolean isBigrams) {
		int max = 0, total=0;
		for(int i=0; i<trainedObjs.length; i++) {
			
			Map<Bigram, Integer> temp;
			if(isBigrams)
				temp = trainedObjs[i].getBigramCounts();
			else
				temp = trainedObjs[i].getUnigramCounts();
			Integer count = temp.get(bigramValue.getBigram());
			if(count != null) {
				max = max > count.intValue() ? max : count.intValue();
				total+=count.intValue();
			}
		}
		bigramValue.maxCount = max;
		bigramValue.totalOccurrences = total;
	}
}
