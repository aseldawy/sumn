package edu.umn.cs.sumn;

import scala.Serializable;

/**
 * An interface for an accumulator of double values.
 * 
 * @author Ahmed Eldawy
 */
public interface Accumulator extends Serializable {
    /**
     * Adds the given double value to the accumulator.
     * 
     * @param v
     */
    public void add(double v);

    /**
     * Adds another accumulator, typically, of the same type.
     * 
     * @param a
     */
    public void add(Accumulator a);

    /**
     * Retrieves an approximation of the current double value of the accumulator.
     * 
     * @return
     */
    public double doubleValue();
}
