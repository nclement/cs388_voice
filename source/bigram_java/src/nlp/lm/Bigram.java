package nlp.lm;

public class Bigram implements Comparable<Bigram> {
	String first, second;
	
	public Bigram(String f, String s) {
		first = f;
		second = s;
	}
	
	String getFirst() {
		return first;
	}
	String getSecond() {
		return second;
	}
	
	public String toString() {
		return first+","+second;
	}

	@Override
	public boolean equals(Object o) {
		return this.compareTo((Bigram)o) == 0;
	}
	
	@Override
	public int hashCode() {
		String mine = first+","+second;
		return mine.hashCode();
	}
	
	@Override
	public int compareTo(Bigram o) {
		//System.err.println("Using compareTo!");
		String mine = first+","+second;
		String theirs = o.first+","+o.second;
		return mine.compareTo(theirs);
	}

}
