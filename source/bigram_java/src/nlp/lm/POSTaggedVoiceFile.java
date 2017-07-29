package nlp.lm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/** 
 *
 * @author Nathan Clement
 * Methods for processing raw text files and returning a list of sentences
 */

public class POSTaggedVoiceFile {
	/** The name of the text file */
	public File file = null;
	/** The I/O reader for accessing the file */
	protected BufferedReader reader = null;
	
	static final String LAUGHTER       = "@@";
	static final String UNINTELLIGIBLE = "FNORD";
	static final String NAME           = "NAME";
	static final String PLACE          = "PLACE";
	static final String ORG            = "ORG";
	static final String THING          = "THING";
	static final String FOREIGN        = "ZZ";
	static final String PVC            = "Z";
	static final String USER           = "SX";
	static final String ONOMATOPOEIA   = "OMP";

	private static String checkWord(String s) {
		if(s.contains("@"))
			return LAUGHTER;
		if(s.startsWith("FNORD"))
			return UNINTELLIGIBLE;
		if(s.startsWith("NAME"))
			return NAME;
		if(s.startsWith("PLACE"))
			return PLACE;
		if(s.startsWith("ORG"))
			return ORG;
		if(s.startsWith("THING"))
			return THING;
		if(s.startsWith("ZZ"))
			return FOREIGN;
		if(s.startsWith("Z"))
			return PVC;
		if(s.contains("\\/"))
			return USER;
		if(s.contains("?"))
			return ONOMATOPOEIA;
		return s;
	}

	/** Create an object for a given LDC POS tagged file */
	public POSTaggedVoiceFile(File file) {
		this.file = file;
		try {
			this.reader = new BufferedReader(new FileReader(file));
		}
		catch (IOException e) {
			System.out.println("\nCould not open text file: " + file);
			System.exit(1);
		}
	}

	/**
	 *  Return a List of sentences each represented as a List of String tokens for 
	 *  the sentences in this file 
	 */
	protected List<List<String>> tokenWordLists() {
		List<List<String>> sentences = new ArrayList<List<String>>();
		try {
			String line;
			while ((line=reader.readLine()) != null) {
				// Ignore empty lines
				if(line.isEmpty())
					continue;
				
				sentences.add(getWordTokens(line));
			}
		}
		catch(IOException e) {
			System.out.println("\nCould not read from TextFileDocument: " + file);
			System.exit(1);
		}

		return sentences;
	}
	
	/**
	 *  Return a List of sentences each represented as a List of String tokens for 
	 *  the sentences in this file 
	 */
	protected List<List<String>> tokenTagLists() {
		List<List<String>> sentences = new ArrayList<List<String>>();
		try {
			String line;
			while ((line=reader.readLine()) != null) {
				// Ignore empty lines
				if(line.isEmpty())
					continue;
				
				sentences.add(getTagTokens(line));
			}
		}
		catch(IOException e) {
			System.out.println("\nCould not read from TextFileDocument: " + file);
			System.exit(1);
		}

		return sentences;
	}

	public static List<String> getTagTokens(String s) {
		List<String> sentence = new ArrayList<String>();
		String words[] = s.split(" ");
		for(String _s : words) {
			// Just add whatever comes after the '_'
			//System.out.println("s: ["+_s+"]");
			//String word = checkWord(_s.split("_")[1]);
			String word = _s.split("_")[1];
			sentence.add(word);
		}
		
		return sentence;
	}

	public static List<String> getWordTokens(String s) {
		List<String> sentence = new ArrayList<String>();
		String words[] = s.split(" ");
		for(String _s : words) {
			// Just add whatever comes before the '_'
			//System.out.println("s: ["+_s+"]");
			String word = checkWord(_s.split("_")[0]);
			sentence.add(word);
		}
		
		return sentence;
	}

	public static List<Bigram> getSentenceBigramTags(String sentence) {
		return getSentenceBigramTags(UntaggedTextFile.getTokens(sentence));
	}
	
	public static List<Bigram> getSentenceBigramTags(List<String> sentence) {
		ArrayList<Bigram> bigrams = new ArrayList<Bigram>();
		String prevToken = "<S>";
		for(String s : sentence) {
			bigrams.add(new Bigram(prevToken, s));
			prevToken = s;
		}
		bigrams.add(new Bigram(prevToken, "</S>"));
		
		return bigrams;
	}
	public static List<Bigram> getSentenceBigramWords(List<String> sentence) {
		ArrayList<Bigram> bigrams = new ArrayList<Bigram>();
		String prevToken = "<S>";
		for(String s : sentence) {
			s = checkWord(s);
			bigrams.add(new Bigram(prevToken, s));
			prevToken = s;
		}
		bigrams.add(new Bigram(prevToken, "</S>"));
		
		return bigrams;
	}
	
	public static List<Bigram> getSentenceUnigramTags(List<String> sentence) {
		ArrayList<Bigram> bigrams = new ArrayList<Bigram>();
		bigrams.add(new Bigram("<S>","<S>"));
		for(String s : sentence) {
			// just add whatever comes after the '_'
			//s = s.split("_")[1];
			
			bigrams.add(new Bigram(s, s));
		}
		bigrams.add(new Bigram("</S>", "</S>"));
		
		return bigrams;
	}
	
	public static List<Bigram> getSentenceUnigramWords(List<String> sentence) {
		ArrayList<Bigram> bigrams = new ArrayList<Bigram>();
		bigrams.add(new Bigram("<S>","<S>"));
		for(String s : sentence) {
			// make sure the word is valid
			s = checkWord(s);
			bigrams.add(new Bigram(s, s));
		}
		bigrams.add(new Bigram("</S>", "</S>"));
		
		return bigrams;
	}

	
	/** Take a list of LDC tagged input files or directories and convert them to a List of sentences
       each represented as a List of token Strings */
	public static List<List<String>> convertToTokenTagLists(File f) { 
		return new POSTaggedVoiceFile(f).tokenTagLists();
	}
	public static List<List<String>> convertToTokenWordLists(File f) { 
		return new POSTaggedVoiceFile(f).tokenWordLists();
	}
}
