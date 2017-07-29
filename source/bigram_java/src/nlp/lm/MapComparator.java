package nlp.lm;

import java.util.Comparator;
import java.util.Map;

public class MapComparator implements Comparator<Object> {

	Map base;
	public MapComparator(Map base) {
		this.base = base;
	}

	public int compare(Object a, Object b) {
		if(((Comparable)base.get(a)).compareTo(base.get(b)) < 0) {
			return 1;
		} else if(((Comparable)base.get(a)).compareTo(base.get(b)) == 0) {
			return 0;
		} else {
			return -1;
		}
	}
}