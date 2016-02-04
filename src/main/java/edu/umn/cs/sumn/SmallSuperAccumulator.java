/***********************************************************************
* Copyright (c) 2015 by Regents of the University of Minnesota.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License, Version 2.0 which 
* accompanies this distribution and is available at
* http://www.opensource.org/licenses/apache2.0.php.
*
*************************************************************************/
package edu.umn.cs.sumn;

/**
 * Uses the small super accumulator algorithm as described in
 * Radford M. Neal. "Fast Exact Summation Using Small and Large Superaccumulators",
 * arXiv:1505.05571v1 [cs.NA] 21 May 2015
 * The accumulator internally keeps 67 separate fixed-point accumulators with 32-bits
 * overlap between each two consecutive accumulators. Each value is added to the
 * corresponding accumulators by separating it into two values and adding each
 * one to one of the accumulators.
 * 
 * @author Ahmed Eldawy
 */
public class SmallSuperAccumulator implements Accumulator {
    private static int MANTISSA_BITS = 52;
    private static long MANTISSA_MASK = 0x000FFFFFFFFFFFFFL;
    private static int LOW_MANTISSA_BITS = 32;
    private static long LOW_MANTISSA_MASK = 0x00000000FFFFFFFFL;
    private static int EXP_BITS = 11;
    private static int EXP_MASK = 0x7FF;
    private static int LOW_EXP_BITS = 5;
    private static int LOW_EXP_MASK = 0x1F;
    private static int HIGH_EXP_BITS = 6;
    private static int HIGH_EXP_MASK = 0x3F;
    /** A mask that retrieves the sign bit of primitive long value */
    private static final long LONG_SIGN_MASK = 0x8000000000000000L;

    /**
     * The significant bits of all chunks that comprise the value of the
     * accumulator
     */
    private long[] chunks;

    public SmallSuperAccumulator() {
        chunks = new long[67];
    }

    /**
     * Adds the given floating point value to the current value of the accumulator
     * 
     * @param val
     */
    public void add(double value) {
        long ivalue = Double.doubleToLongBits(value);
        long mantissa = ivalue & MANTISSA_MASK;
        int exp = (int) ((ivalue >>> MANTISSA_BITS) & EXP_MASK);

        if (exp != 0 && exp != 2047) {
            // Normalized value
            mantissa |= (1L << Utils.MANTISSA_SIZE);
        } else if (exp == 0) {
            // Denormalized or zero
            if (mantissa == 0)
                return; // Zero
            exp = 1;
        } else {
            // Infinity or NaN
            if (mantissa == 0) {
                // TODO Handle Infinity
                throw new RuntimeException("Cannot handle Infinity");
            } else {
                // TODO Handle NaN
                throw new RuntimeException("Cannot handle NaN");
            }
        }

        int low_exp = exp & LOW_EXP_MASK;
        int high_exp = exp >>> LOW_EXP_BITS;
        long low_mantissa = (mantissa << low_exp) & LOW_MANTISSA_MASK;
        long high_mantissa = mantissa >>> (LOW_MANTISSA_BITS - low_exp);

        if (ivalue < 0) {
            low_mantissa = -low_mantissa;
            high_mantissa = -high_mantissa;
        }
        // Detect overflow and propagate the carry bit to the higher order mantissa
        // An overflow happens if the two numbers have the same sign and
        // their sum has an opposite sign
        long sum = chunks[high_exp] + low_mantissa;
        boolean overflow = (chunks[high_exp] ^ low_mantissa) >= 0 && (low_mantissa ^ sum) < 0;
        int i_exp = high_exp;
        chunks[i_exp++] = sum;
        while (overflow && i_exp < chunks.length) {
            // Notice that due to the 32-bits overlap, the carry bit goes
            // into the middle of the higher chunk
            overflow = chunks[i_exp] >>> LOW_MANTISSA_BITS == 0xFFFFFFFFL;
            chunks[i_exp] += chunks[i_exp - 1] < 0 ? (1L << 32) : -(1L << 32);
            i_exp++;
        }

        // Add the other half
        high_exp++;
        sum = chunks[high_exp] + high_mantissa;
        overflow = (chunks[high_exp] ^ high_mantissa) >= 0 && (high_mantissa ^ sum) < 0;
        i_exp = high_exp;
        chunks[i_exp++] = sum;
        while (overflow && i_exp < chunks.length) {
            overflow = chunks[i_exp] >>> LOW_MANTISSA_BITS == 0xFFFFFFFFL;
            chunks[i_exp] += chunks[i_exp - 1] < 0 ? (1L << 32) : -(1L << 32);
            i_exp++;
        }
    }

    public void add(Accumulator a) {
        if (!(a instanceof SmallSuperAccumulator))
            throw new RuntimeException("Cannot add accumulator of type: " + a.getClass());
        SmallSuperAccumulator ssa = (SmallSuperAccumulator) a;
        for (int i = 0; i < chunks.length; i++) {
            // Add the two corresponding chunks and propagate the carry flag, as needed
            long sum = this.chunks[i] + ssa.chunks[i];
            boolean overflow = (this.chunks[i] ^ ssa.chunks[i]) >= 0 && (this.chunks[i] ^ sum) < 0;
            int i_exp = i;
            this.chunks[i_exp++] = sum;
            while (overflow && i_exp < chunks.length) {
                // Notice that due to the 32-bits overlap, the carry bit goes
                // into the middle of the higher chunk
                overflow = chunks[i_exp] >>> LOW_MANTISSA_BITS == 0xFFFFFFFFL;
                chunks[i_exp] += chunks[i_exp - 1] < 0 ? (1L << 32) : -(1L << 32);
                i_exp++;
            }
        }
    }

    public double doubleValue() {
        int mostSignificantChunk = chunks.length - 1;
        while (mostSignificantChunk >= 0 && chunks[mostSignificantChunk] == 0)
            mostSignificantChunk--;
        if (mostSignificantChunk < 0)
            return 0;
        int leastSignificantChunk = Math.max(0, mostSignificantChunk - 2);
        // We can further increase the index of leastSignificantChunk if we
        // test the significant bits in the mostSignificantChunk but we use
        // this value here for simplicity

        double value = 0;
        for (int chunk = leastSignificantChunk; chunk <= mostSignificantChunk; chunk++) {
            value += getChunkValue(chunk);
        }
        return value;
    }

    protected double getChunkValue(int chunk) {
        // Right half (least significant)
        long m1 = chunks[chunk] & LOW_MANTISSA_MASK;
        int e1 = chunk * 32 - 1023 - 52;
        double x1 = Utils.buildFromTrueValues(m1, e1);
        long m2 = chunks[chunk] >> LOW_MANTISSA_BITS;
        int e2 = chunk * 32 + 32 - 1023 - 52;
        double x2 = Utils.buildFromTrueValues(m2, e2);
        return x1 + x2;
    }

    @Override
    public String toString() {
        return Double.toString(this.doubleValue());
    }
}
