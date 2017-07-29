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

public class UntaggedTextFile {
	/** The name of the text file */
	public File file = null;
	/** The I/O reader for accessing the file */
	protected BufferedReader reader = null;
	
	static final String LAUGHTER = "@@";
	static final String UNINTELLIGIBLE = "XX";

	/** Create an object for a given LDC POS tagged file */
	public UntaggedTextFile(File file) {
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
	protected List<List<String>> tokenLists() {
		List<List<String>> sentences = new ArrayList<List<String>>();
		try {
			String line;
			while ((line=reader.readLine()) != null) {
				// Ignore empty lines
				if(line.isEmpty())
					continue;
				
				sentences.add(getTokens(line));
			}
		}
		catch(IOException e) {
			System.out.println("\nCould not read from TextFileDocument: " + file);
			System.exit(1);
		}

		return sentences;
	}

	public static List<String> getTokens(String s) {
		List<String> sentence = new ArrayList<String>();
		String words[] = s.split(" ");
		for(String _s : words) {
			s = s.toLowerCase();
			if(s.contains("@"))
				_s = LAUGHTER;
			if(s.contains("xx") || s.equals("x"))
				_s = UNINTELLIGIBLE;
			sentence.add(_s);
		}
		
		return sentence;
	}
	

	public static List<Bigram> getSentenceBigrams(String sentence) {
		return getSentenceBigrams(UntaggedTextFile.getTokens(sentence));
	}
	
	public static List<Bigram> getSentenceBigrams(List<String> sentence) {
		ArrayList<Bigram> bigrams = new ArrayList<Bigram>();
		String prevToken = "<S>";
		for(String s : sentence) {
			bigrams.add(new Bigram(prevToken, s));
			prevToken = s;
		}
		bigrams.add(new Bigram(prevToken, "</S>"));
		
		return bigrams;
	}
	
	public static List<Bigram> getSentenceUnigrams(List<String> sentence) {
		ArrayList<Bigram> bigrams = new ArrayList<Bigram>();
		bigrams.add(new Bigram("<S>","<S>"));
		for(String s : sentence) {
			bigrams.add(new Bigram(s, s));
		}
		bigrams.add(new Bigram("</S>", "</S>"));
		
		return bigrams;
	}
	
	/** Take a list of LDC tagged input files or directories and convert them to a List of sentences
       each represented as a List of token Strings */
	public static List<List<String>> convertToTokenLists(File f) { 
		return new UntaggedTextFile(f).tokenLists();
	}
}
