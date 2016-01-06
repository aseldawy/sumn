package edu.umn.cs.sumn;

/**
 * Accumulates all the values to a single double value.
 * 
 * @author Ahmed Eldawy
 */
public class SimpleSum implements Accumulator {

    /** The current value of the accumulator */
    private double value;

    public void add(double v) {
        value += v;
    }

    public double doubleValue() {
        return value;
    }

}
