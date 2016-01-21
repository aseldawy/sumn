package edu.umn.cs.sumn;

/**
 * An implementation of the sparse super accumulator which represents the value
 * of the superaccumulator as a Generalized Signed Digit (GSD) Float representation.
 * The value of the superaccumulator is represented as n digits, y[0..n-1].
 * Each digit has an index[0..n-1] which can be any signed integer.
 * The overall value of the accumulator = sum{y[i] * R^(index[i])}
 * where i = 0 .. n-1, R = 1 << 63, and ^ is the power operator
 * We use a value of R= (1<<63) to be able to use all the bits in the y's
 * 
 * @author Ahmed Eldawy
 */
public class SparseSuperAccumulator {

    /** All the digits in the value of the accumulator ordered by their significance */
    protected long[] y;

    /** The index of each digit in the value */
    protected int[] index;

}
