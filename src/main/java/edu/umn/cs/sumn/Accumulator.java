package edu.umn.cs.sumn;

/**
 * An interface for an accumulator of double values.
 * 
 * @author Ahmed Eldawy
 */
public interface Accumulator {
    /**
     * Adds the given double value to the accumulator.
     * 
     * @param v
     */
    public void add(double v);

    /**
     * Retrieves an approximation of the current double value of the accumulator.
     * 
     * @return
     */
    public double doubleValue();
}
