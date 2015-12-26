package edu.umn.cs.sumn;

public class SuperAccumulator {
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
    public void add(double val) {
        int exponent = Utils.getExponent(val);
        long mantissa = Utils.getMantissa(val);
        if (exponent == 0) {
            if (mantissa == 0) {
                // TODO Handle Zero
                throw new RuntimeException("Cannot handle zero");
            } else {
                // TODO Handle Denormalized values - https://en.wikipedia.org/wiki/Denormal_number
                throw new RuntimeException("Cannot handle denormalized numbeers");
            }
        } else if (exponent == 2047) {
            if (mantissa == 0) {
                // TODO Handle Infinity
                throw new RuntimeException("Cannot handle Infinity");
            } else {
                // TODO Handle NaN
                throw new RuntimeException("Cannot handle NaN");
            }
        } else {
            // Handle normal values
            // The hidden ONE is the most significant bit in the mantissa
            // which is not explicitly represented in its value
            int indexOfMostSignificantBit = 53 + exponent; // The hidden ONE
            int indexOfLeastSignificantBit = exponent; // TODO subtract tailing zeros in mantissa
            int indexOfMostSignificantChunk = indexOfMostSignificantBit / 32;
            int indexOfLeastSignificantChunk = indexOfLeastSignificantBit / 32;

            // Correct the value of mantissa by adding the hidden ONE
            long trueMantissa = mantissa | (1L << Utils.MANTISSA_SIZE);
            int chunkToUpdate = indexOfMostSignificantChunk;
            while (trueMantissa != 0) {
                int numOfBitsToSkip = 32 * chunkToUpdate - indexOfLeastSignificantBit;
                chunks[chunkToUpdate] += trueMantissa >> numOfBitsToSkip;
                trueMantissa &= (0xFFFFFFFFFFFFFFFFL >>> (64 - numOfBitsToSkip));
                chunkToUpdate--;
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
        long m1 = chunks[chunk] & 0xFFFFFFFFL;
        int e1 = chunk * 32 - 1023 - 52;
        double x1 = Utils.buildFromTrueValues(isNegative, m1, e1);
        long m2 = chunks[chunk] >>> 32;
        int e2 = chunk * 32 + 32 - 1023 - 52;
        double x2 = Utils.buildFromTrueValues(isNegative, m2, e2);
        return x1 + x2;
    }
}
