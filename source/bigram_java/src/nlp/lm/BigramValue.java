package nlp.lm;

public class BigramValue implements Comparable<BigramValue> {
	public Bigram bigram;
	public double value;
	public int maxCount;
	public int totalOccurrences;
	
	public BigramValue(Bigram b, double v) {
		bigram = b;
		value = v;
	}
	
	public Bigram getBigram() {
		return bigram;
	}
	public double getValue() {
		return value;
	}
	
	public String toString() {
		return "" + bigram + ":" + (int)value;
	}
	
	public void setValue(double v) {
		value = v;
	}
	
	@Override
	public int compareTo(BigramValue o) {
		if(value == o.value) {
			if(totalOccurrences == o.totalOccurrences)
				return bigram.compareTo(o.bigram);

			// maximize this value
			if(totalOccurrences > o.totalOccurrences) return -1;
			if(totalOccurrences < o.totalOccurrences) return 1;
		}
		
		// minimize this value
		if(value > o.value) return 1;
		if(value < o.value) return -1;
		return 0;
	}

}
