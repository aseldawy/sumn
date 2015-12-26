package edu.umn.cs.sumn;

public class SuperAccumulator {
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

    /**
     * The significant bits of all chunks that comprise the value of the
     * accumulator
     */
    private long[] chunks;

    /** A flag set to true when the value of the SuperAccumulator is negative */
    private boolean isNegative;

    public SuperAccumulator() {
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
        int exp = (int) ((ivalue >> MANTISSA_BITS) & EXP_MASK);

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
        int high_exp = exp >> LOW_EXP_BITS;
        long low_mantissa = (mantissa << low_exp) & LOW_MANTISSA_MASK;
        long high_mantissa = mantissa >> (LOW_MANTISSA_BITS - low_exp);

        if (ivalue < 0) {
            chunks[high_exp] -= low_mantissa;
            chunks[high_exp + 1] -= high_mantissa;
        } else {
            chunks[high_exp] += low_mantissa;
            chunks[high_exp + 1] += high_mantissa;
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
        long m1 = chunks[chunk] & 0xFFFFFFFFL;
        int e1 = chunk * 32 - 1023 - 52;
        double x1 = Utils.buildFromTrueValues(isNegative, m1, e1);
        long m2 = chunks[chunk] >>> 32;
        int e2 = chunk * 32 + 32 - 1023 - 52;
        double x2 = Utils.buildFromTrueValues(isNegative, m2, e2);
        return x1 + x2;
    }
}
