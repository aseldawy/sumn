package edu.umn.cs.sumn;

public class Utils {
    public static final long SIGN_MASK = 0x8000000000000000L;

    public static final int EXPONENT_SIZE = 11;
    public static final long EXPONENT_MASK = 0x7FF0000000000000L;

    public static final int MANTISSA_SIZE = 52;
    public static final long MANTISSA_MASK = 0x000FFFFFFFFFFFFFL;

    public static double build(boolean isNegative, long mantissa, long exponent) {
        long bits = 0;
        if (isNegative) {
            bits |= SIGN_MASK;
        }
        bits |= (mantissa & MANTISSA_MASK);
        bits |= (exponent << MANTISSA_SIZE) & EXPONENT_MASK;
        return Double.longBitsToDouble(bits);
    }

    public static void split(double x, double[] splits) {
        long xbits = Double.doubleToLongBits(x);
        final long lowerMantissa = MANTISSA_MASK >> (MANTISSA_SIZE / 2);

        splits[0] = Double.longBitsToDouble(xbits & (lowerMantissa | EXPONENT_MASK | SIGN_MASK));
        splits[1] = x - splits[0];
    }

    public static int getExponent(double val) {
        return (int) ((Double.doubleToLongBits(val) & EXPONENT_MASK) >> MANTISSA_SIZE);
    }

    public static long getMantissa(double val) {
        return Double.doubleToLongBits(val) & MANTISSA_MASK;
    }

    public static boolean isNegative(double val) {
        return (Double.doubleToLongBits(val) & SIGN_MASK) != 0;
    }

    /**
     * Build a floating point number from true values of mantissa and exponent.
     * The returned value is equal to (-1)^sign + mantissa * 2^exponent
     * where value of (sign) is one for negative numbers and zero otherwise.
     * 
     * @param signedMantissa
     *            The value of mantissa expressed as a signed value
     * @param exponent
     */
    public static double buildFromTrueValues(long signedMantissa, int exponent) {
        if (signedMantissa == 0)
          return 0.0;
        boolean isNegative = signedMantissa < 0;
        if (isNegative)
          signedMantissa = -signedMantissa;
        long mask = 1L << MANTISSA_SIZE;
        exponent += 1023 + 52;
        while (exponent > 0 && (signedMantissa & mask) == 0) {
          signedMantissa <<= 1;
          exponent--;
        }
        if (exponent == 0)
          signedMantissa >>= 1; // Denormalized number
        signedMantissa &= MANTISSA_MASK; // Normalize by removing the leading one
        return build(isNegative, signedMantissa, exponent);
    }

}
