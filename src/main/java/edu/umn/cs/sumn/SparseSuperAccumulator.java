package edu.umn.cs.sumn;

/**
 * An implementation of the sparse super accumulator which represents the value
 * of the superaccumulator as a Generalized Signed Digit (GSD) Float representation.
 * The value of the superaccumulator is represented as n digits, y[0..n-1].
 * Each digit has an index[0..n-1] which can be any signed integer.
 * The overall value of the accumulator = sum{y[i] * R^(index[i])}
 * where i = 0 .. n-1, R = 1 << 63, and ^ is the power operator
 * We use a value of R= (1<<63) to be able to most of the bits in the y's as
 * they are represented as 64-bit long values.
 * 
 * Notice that we cannot use R = 1 << 64 because we need to be able to represent
 * all the values in the range [-(R-1), +(R-1)] and the range of long value is
 * [-(1<<63), (1<<63 - 1)] 
 * @author Ahmed Eldawy
 */
public class SparseSuperAccumulator {

    /** All the digits in the value of the accumulator ordered by their significance */
    protected long[] y;

    /**
     * The index of each digit in the value. The index could be negative to
     * represent fractions.
     */
    protected int[] index;
    
    /**
     * The value of the base R for (alpha, beta)-regularized numbers
     */
    protected static final long BASE = 1L << 63;

    
    /**
     * Initialize a new SparseSuperAccumulator with a value of zero
     */
    public SparseSuperAccumulator() {
    }
    
    /**
     * Initialize a new SparseSuperAccumulator with a double floating-point
     * value.
     * @param v
     */
    public SparseSuperAccumulator(double v) {
      
    }
}
