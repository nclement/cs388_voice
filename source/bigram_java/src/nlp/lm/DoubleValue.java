package nlp.lm;

import java.util.Comparator;

/**
 * A simple wrapper data structure for storing a double 
 * as an Object that can be put into lists, maps, etc. and then
 * incremented, decremented, and set.
 *
 * @author Ray Mooney
 */

public class DoubleValue implements Comparable<DoubleValue> {
	/**
	 * A numerical value
	 */
	protected double value = 0;


	/**
	 * Constructors for both int and double
	 */
	DoubleValue() {
		setValue(0);
	}
	DoubleValue(int d) {
		setValue(d);
	}
	DoubleValue(double d) {
		setValue(d);
	}

	/**
	 * Increment and return the new count
	 */
	public double increment() {
		return ++value;
	}

	/**
	 * Increment by n and return the new count
	 */
	public double increment(int n) {
		value = value + n;
		return value;
	}

	/**
	 * Increment by n and return the new count
	 */
	public double increment(double n) {
		value = value + n;
		return value;
	}

	/**
	 * Decrement and return the new count
	 */
	public double decrement() {
		return --value;
	}

	/**
	 * Decrement by n and return the new count
	 */
	public double decrement(int n) {
		value = value - n;
		return value;
	}

	/**
	 * Decrement by n and return the new count
	 */
	public double decrement(double n) {
		value = value - n;
		return value;
	}

	/**
	 * Get the current count
	 */
	public double getValue() {
		return value;
	}

	/**
	 * Set the current count
	 */
	public double setValue(int value) {
		this.value = value;
		return value;
	}

	/**
	 * Set the current count
	 */
	public double setValue(double value) {
		this.value = value;
		return value;
	}

	public String toString() {
		return ""+this.value;
	}
	
	@Override
	public int compareTo(DoubleValue o) {
		if(value < o.value) return -1;
		if(value > o.value) return 1;
		return 0;
	}

}
