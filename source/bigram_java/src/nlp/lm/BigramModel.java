package nlp.lm;

import java.io.*;
import java.util.*;

/** 
 * @author Ray Mooney
 * A simple bigram language model that uses simple fixed-weight interpolation
 * with a unigram model for smoothing.
 */

public class BigramModel {

	public boolean useUNK = true;
	
	/** Unigram model that maps a token to its unigram probability */
	public Map<String, DoubleValue> unigramMap = null; 
	public Map<Bigram, Integer> unigramCounts = null;
	
	/**  Bigram model that maps a bigram as a string "A\nB" to the
	 *   P(B | A) */
	public Map<String, DoubleValue> bigramMap = null;
	public Map<Bigram, Integer> bigramCounts = null;
	
	/** Total count of tokens in training data */
	public double tokenCount = 0;

	/** Interpolation weight for unigram model */
	public double lambda1 = 0.1;

	/** Interpolation weight for bigram model */
	public double lambda2 = 0.9;
	
	public Map<Bigram, Integer> getUnigramCounts() { return unigramCounts; }
	public Map<Bigram, Integer> getBigramCounts() { return bigramCounts; }
	
	/** Initialize model with empty hashmaps with initial
	 *  unigram entries for setence start (<S>), sentence end (</S>)
	 *  and unknown tokens */
	public BigramModel(boolean unk) {
		useUNK = unk;
		unigramMap = new HashMap<String, DoubleValue>();
		unigramCounts = new HashMap<Bigram, Integer>();
		bigramMap = new HashMap<String, DoubleValue>();
		bigramCounts = new HashMap<Bigram, Integer>();
		unigramMap.put("<S>", new DoubleValue());
		unigramMap.put("</S>", new DoubleValue());
		unigramMap.put("<UNK>", new DoubleValue());
	}
	public BigramModel() {
		unigramMap = new HashMap<String, DoubleValue>();
		unigramCounts = new HashMap<Bigram, Integer>();
		bigramMap = new HashMap<String, DoubleValue>();
		bigramCounts = new HashMap<Bigram, Integer>();
		unigramMap.put("<S>", new DoubleValue());
		unigramMap.put("</S>", new DoubleValue());
		unigramMap.put("<UNK>", new DoubleValue());
	}

	/** Train the model on a List of sentences represented as
	 *  Lists of String tokens */
	public void train (List<List<String>> sentences) {
		// Accumulate unigram and bigram counts in maps
		trainSentences(sentences);
		// Compure final unigram and bigram probs from counts
		calculateProbs();
	}

	/** Accumulate unigram and bigram counts for these sentences */
	public void trainSentences (List<List<String>> sentences) {
		for (List<String> sentence : sentences) {
			//System.out.println(sentence);
			trainSentence(sentence);
		}
	}

	/** Accumulate unigram and bigram counts for this sentence */
	public void trainSentence (List<String> sentence) {
		// First count an initial start sentence token
		String prevToken = "<S>";
		DoubleValue unigramValue = unigramMap.get("<S>");
		unigramValue.increment();
		tokenCount++;
		// For each token in sentence, accumulate a unigram and bigram count
		for (String token : sentence) {
			unigramValue = unigramMap.get(token);
			// If this is the first time token is seen then count it
			// as an unkown token (<UNK>) to handle out-of-vocabulary 
			// items in testing
			if (unigramValue == null) {
				// Store token in unigram map with 0 count to indicate that
				// token has been seen but not counted
				unigramMap.put(token, new DoubleValue());
				if(useUNK)
					token = "<UNK>";
				unigramValue = unigramMap.get(token);
			}
			unigramValue.increment();    // Count unigram
			tokenCount++;               // Count token
			// Make bigram string 
			String bigram = bigram(prevToken, token);
			DoubleValue bigramValue = bigramMap.get(bigram);
			
			if (bigramValue == null) {
				// If previously unseen bigram, then
				// initialize it with a value
				bigramValue = new DoubleValue();
				bigramMap.put(bigram, bigramValue);
			}
			// Count bigram
			bigramValue.increment();
			prevToken = token;
		}
		// Account for end of sentence unigram
		unigramValue = unigramMap.get("</S>");
		unigramValue.increment();
		tokenCount++;
		// Account for end of sentence bigram
		String bigram = bigram(prevToken, "</S>");
		DoubleValue bigramValue = bigramMap.get(bigram);
		if (bigramValue == null) {
			bigramValue = new DoubleValue();
			bigramMap.put(bigram, bigramValue);
		}
		bigramValue.increment();
	}

	/** Compute unigram and bigram probabilities from unigram and bigram counts */
	public void calculateProbs() {
		// Set bigram values to conditional probability of second token given first
		for (Map.Entry<String, DoubleValue> entry : bigramMap.entrySet()) {
			// An entry in the HashMap maps a token to a DoubleValue
			String bigram = entry.getKey();
			// The value for the token is in the value of the DoubleValue
			DoubleValue value = entry.getValue();
			double bigramCount = value.getValue();
			
			// Put it in the BigramCounts map to save for later
			bigramCounts.put(new Bigram(bigramToken1(bigram), bigramToken2(bigram)), new Integer((int)bigramCount));
			
			String token1 = bigramToken1(bigram); // Get first token of bigram
			// Prob is ratio of bigram count to token1 unigram count
			double condProb = bigramCount / unigramMap.get(token1).getValue();
			// Set map value to conditional probability 
			value.setValue(condProb);
		}
		// Store unigrams with zero count to remove from map
		List<String> zeroTokens = new ArrayList<String>();
		// Set unigram values to unigram probability
		for (Map.Entry<String, DoubleValue> entry : unigramMap.entrySet()) {
			// An entry in the HashMap maps a token to a DoubleValue
			String token = entry.getKey();
			// Uniggram count is the current map value
			DoubleValue value = entry.getValue();
			double count = value.getValue();
			
			// Put it in the unigramCounts map to save for later
			unigramCounts.put(new Bigram(token,token), new Integer((int)count));
			
			if (count == 0) 
				// If count is zero (due to first encounter as <UNK>)
				// then remove save it to remove from map
				zeroTokens.add(token);
			else
				// Set map value to prob of unigram
				value.setValue(count / tokenCount);
		}
		// Remove zero count unigrams from map
		for (String token : zeroTokens) 
			unigramMap.remove(token);
	}

	/** Return bigram string as two tokens separated by a newline */
	public String bigram (String prevToken, String token) {
		return prevToken + "\n" + token;
	}

	/** Return fist token of bigram (substring before newline) */
	public String bigramToken1 (String bigram) {
		int newlinePos = bigram.indexOf("\n");
		return bigram.substring(0,newlinePos);
	}

	/** Return second token of bigram (substring after newline) */
	public String bigramToken2 (String bigram) {
		int newlinePos = bigram.indexOf("\n");
		return bigram.substring(newlinePos + 1, bigram.length());
	}

	/** Print model as lists of unigram and bigram probabilities */
	public void print() {
		System.out.println("Unigram probs:");
		for (Map.Entry<String, DoubleValue> entry : unigramMap.entrySet()) {
			// An entry in the HashMap maps a token to a DoubleValue
			String token = entry.getKey();
			// The value for the token is in the value of the DoubleValue
			DoubleValue value = entry.getValue();
			System.out.println(token + " : " + value.getValue());
		}
		System.out.println("\nBigram probs:");
		for (Map.Entry<String, DoubleValue> entry : bigramMap.entrySet()) {
			// An entry in the HashMap maps a token to a DoubleValue
			String bigram = entry.getKey();
			// The value for the token is in the value of the DoubleValue
			DoubleValue value = entry.getValue();
			if(value.getValue() > 0.9)
				System.out.println(bigramToken2(bigram) + " given " + bigramToken1(bigram) + 
						" : " + value.getValue());
		}
	}

	/** Use sentences as a test set to evaluate the model. Print out perplexity
	 *  of the model for this test data */
	public double test (List<List<String>> sentences) {
		// Compute log probability of sentence to avoid underflow
		double totalLogProb = 0;
		// Keep count of total number of tokens predicted
		double totalNumTokens = 0;
		// Accumulate log prob of all test sentences
		for (List<String> sentence : sentences) {
			// Num of tokens in sentence plus 1 for predicting </S>
			totalNumTokens += sentence.size() + 1;
			// Compute log prob of sentence
			double sentenceLogProb = sentenceLogProb(sentence);
			//	    System.out.println(sentenceLogProb + " : " + sentence);
			// Add to total log prob (since add logs to multiply probs)
			totalLogProb += sentenceLogProb;
		}
		// Given log prob compute perplexity
		return Math.exp(totalLogProb/totalNumTokens);

		//double perplexity = Math.exp(-totalLogProb / totalNumTokens);
		//System.out.print(perplexity);
		//return perplexity;
	}

	/* Compute log probability of sentence given current model */
	public double sentenceLogProb (List<String> sentence) {
		// Set start-sentence as initial token
		String prevToken = "<S>";
		// Maintain total sentence prob as sum of individual token
		// log probs (since adding logs is same as multiplying probs)
		double sentenceLogProb = 0;
		// Check prediction of each token in sentence
		for (String token : sentence) {
			// Retrieve unigram prob
			DoubleValue unigramVal = unigramMap.get(token);
			if (unigramVal == null) {
				// If token not in unigram model, treat as <UNK> token
				if(useUNK)
					token = "<UNK>";
				unigramVal = unigramMap.get(token);
			}
			// Get bigram prob
			String bigram = bigram(prevToken, token);
			DoubleValue bigramVal = bigramMap.get(bigram);
			// Compute log prob of token using interpolated prob of unigram and bigram
			double logProb = Math.log(interpolatedProb(unigramVal, bigramVal));
			// Add token log prob to sentence log prob
			sentenceLogProb += logProb;
			// update previous token and move to next token
			prevToken = token;
		}
		// Check prediction of end of sentence token
		DoubleValue unigramVal = unigramMap.get("</S>");
		String bigram = bigram(prevToken, "</S>");
		DoubleValue bigramVal = bigramMap.get(bigram);
		double logProb = Math.log(interpolatedProb(unigramVal, bigramVal));
		// Update sentence log prob based on prediction of </S>
		sentenceLogProb += logProb;
		return sentenceLogProb;
	}

	/** Like test1 but excludes predicting end-of-sentence when computing perplexity */
	public void test2 (List<List<String>> sentences) {
		double totalLogProb = 0;
		double totalNumTokens = 0;
		for (List<String> sentence : sentences) {
			totalNumTokens += sentence.size();
			double sentenceLogProb = sentenceLogProb2(sentence);
			//	    System.out.println(sentenceLogProb + " : " + sentence);
			totalLogProb += sentenceLogProb;
		}
		double perplexity = Math.exp(-totalLogProb / totalNumTokens);
		System.out.println("Word Perplexity = " + perplexity );
	}

	/** Like sentenceLogProb but excludes predicting end-of-sentence when computing prob */
	public double sentenceLogProb2 (List<String> sentence) {
		String prevToken = "<S>";
		double sentenceLogProb = 0;
		for (String token : sentence) {
			DoubleValue unigramVal = unigramMap.get(token);
			if (unigramVal == null) {
				if(useUNK)
					token = "<UNK>";
				unigramVal = unigramMap.get(token);
			}
			String bigram = bigram(prevToken, token);
			DoubleValue bigramVal = bigramMap.get(bigram);
			double logProb = Math.log(interpolatedProb(unigramVal, bigramVal));
			sentenceLogProb += logProb;
			prevToken = token;
		}
		return sentenceLogProb;
	}

	/** Returns vector of probabilities of predicting each token in the sentence
	 *  including the end of sentence */
	public double[] sentenceTokenProbs (List<String> sentence) {
		// Set start-sentence as initial token
		String prevToken = "<S>";
		// Vector for storing token prediction probs
		double[] tokenProbs = new double[sentence.size() + 1];
		// Token counter
		int i = 0;
		// Compute prob of predicting each token in sentence
		for (String token : sentence) {
			DoubleValue unigramVal = unigramMap.get(token);
			if (unigramVal == null) {
				if(useUNK)
					token = "<UNK>";
				unigramVal = unigramMap.get(token);
			}
			String bigram = bigram(prevToken, token);
			DoubleValue bigramVal = bigramMap.get(bigram);
			// Store prediction prob for i'th token
			tokenProbs[i] = interpolatedProb(unigramVal, bigramVal);
			prevToken = token;
			i++;
		}
		// Check prediction of end of sentence
		DoubleValue unigramVal = unigramMap.get("</S>");
		String bigram = bigram(prevToken, "</S>");
		DoubleValue bigramVal = bigramMap.get(bigram);
		// Store end of sentence prediction prob
		tokenProbs[i] = interpolatedProb(unigramVal, bigramVal);
		return tokenProbs;
	}

	/** Interpolate bigram prob using bigram and unigram model predictions */	 
	public double interpolatedProb(DoubleValue unigramVal, DoubleValue bigramVal) {
		double bigramProb = 0;
		// In bigram unknown then its prob is zero
		if (bigramVal != null)
			bigramProb = bigramVal.getValue();
		// Linearly combine weighted unigram and bigram probs
		return lambda1 * unigramVal.getValue() + lambda2 * bigramProb;
	}

	public static int wordCount (List<List<String>> sentences) {
		int wordCount = 0;
		for (List<String> sentence : sentences) {
			wordCount += sentence.size();
		}
		return wordCount;
	}
	
	public List<Bigram> getAllBigrams() {
		List<Bigram> bigrams = new ArrayList<Bigram>();
		for(String s : bigramMap.keySet())
			bigrams.add(new Bigram(bigramToken1(s), bigramToken2(s)));
		return bigrams;
	}
	public List<Bigram> getAllUnigrams() {
		List<Bigram> unigrams = new ArrayList<Bigram>();
		for(String s : unigramMap.keySet())
			unigrams.add(new Bigram(s, s));
		return unigrams;
	}
	
	public List<Bigram> getTopKBigrams(int k) {
		List<Bigram> topBigrams = new ArrayList<Bigram>();

		// make it sorted.
		MapComparator mpc = new MapComparator(bigramMap);
		TreeMap<String, DoubleValue> sortedBigrams = new TreeMap<String, DoubleValue>(mpc);
		sortedBigrams.putAll(bigramMap);
		
		// Just get the top k
		int i=0;
		for(String key : sortedBigrams.keySet()) {
			if(++i > k)
				break;
			Bigram b = new Bigram(bigramToken1(key), bigramToken2(key));
			//System.err.println("Bigram "+b+" has value "+sortedBigrams.get(key));
			topBigrams.add(b);
		}
		
		return topBigrams;
	}
	
	public List<Bigram> getTopKUnigrams(int k) {
		List<Bigram> topUnigrams = new ArrayList<Bigram>();

		// make it sorted.
		MapComparator mpc = new MapComparator(unigramMap);
		TreeMap<String, DoubleValue> sortedUnigrams = new TreeMap<String, DoubleValue>(mpc);
		sortedUnigrams.putAll(unigramMap);
		
		// Just get the top k
		int i=0;
		for(String key : sortedUnigrams.keySet()) {
			if(++i > k)
				break;
			Bigram b = new Bigram(key, key);
			//System.err.println("Bigram "+b+" has value "+sortedBigrams.get(key));
			topUnigrams.add(b);
		}
		
		return topUnigrams;
	}

	public void trainPOSTaggedWord(String fileName) {
		File train = new File(fileName);
		List<List<String>> trainSentences = POSTaggedVoiceFile.convertToTokenWordLists(train);
		this.train(trainSentences);
	}

	public void trainPOSTaggedToken(String fileName) {
		File train = new File(fileName);
		List<List<String>> trainSentences = POSTaggedVoiceFile.convertToTokenTagLists(train);
		this.train(trainSentences);
	}

	
	/** Train and test a bigram model.
	 *  Command format: "nlp.lm.BigramModel [DIR]* [TestFrac]" where DIR 
	 *  is the name of a file or directory whose LDC POS Tagged files should be 
	 *  used for input data; and TestFrac is the fraction of the sentences
	 *  in this data that should be used for testing, the rest for training.
	 *  0 < TestFrac < 1
	 *  Uses the last fraction of the data for testing and the first part
	 *  for training.
	 */
	public static void main(String[] args) throws IOException {
		// All but last arg is a file/directory of LDC tagged input data
		File train = new File(args[0]);
		File test = new File(args[1]);
	
		// Get list of sentences from the LDC POS tagged input files
		List<List<String>> trainSentences = 	UntaggedTextFile.convertToTokenLists(train);
		List<List<String>> testSentences = UntaggedTextFile.convertToTokenLists(test);
		
		//System.out.println("# Train Sentences = " + trainSentences.size() + 
		//		" (# words = " + wordCount(trainSentences) + 
		//		") \n# Test Sentences = " + testSentences.size() +
		//		" (# words = " + wordCount(testSentences) + ")");
		
		// Create a bigram model and train it.
		BigramModel model = new BigramModel();
		//System.out.println("Training...");
		model.train(trainSentences);
		//model.print();
		
		List<Bigram> topBigrams = model.getTopKBigrams(20);
		for(Bigram b : topBigrams)
			System.out.println(b);
		
		// Test on training data using test and test2
		//model.test(trainSentences);
		//model.test2(trainSentences);
		//System.out.println("Testing...");
		
		// Test on test data using test and test2
		//double trainPerp = model.test(trainSentences);
		double testPerp = model.test(testSentences);
		System.out.print(testPerp);
		//model.test2(testSentences);
	}

}
