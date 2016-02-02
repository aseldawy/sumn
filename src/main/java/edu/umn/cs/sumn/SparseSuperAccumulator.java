package edu.umn.cs.sumn;

import java.util.Arrays;

/**
 * An implementation of the sparse super accumulator which represents the value
 * of the superaccumulator as a Generalized Signed Digit (GSD) Float representation.
 * The value of the superaccumulator is represented as n digits, y[0..n-1].
 * Each digit has an index[0..n-1] which can be any signed integer.
 * The overall value of the accumulator = sum{y[i] * R^(index[i])}
 * where i = 0 .. n-1, R = 1 << 62, and ^ is the power operator
 * We use a value of R= (1<<62) which makes it easier to do the calculations as
 * the addition of two digits never overflow.
 * Notice that we cannot use R = 1 << 64 because we need to be able to represent
 * all the values in the range [-(R-1), +(R-1)] and the range of long value is
 * [-(1<<63), (1<<63 - 1)]
 * 
 * @author Ahmed Eldawy
 */
public class SparseSuperAccumulator implements Accumulator {
    protected static int MANTISSA_BITS = 52;
    protected static long MANTISSA_MASK = 0x000FFFFFFFFFFFFFL;
    protected static int EXP_BITS = 11;
    protected static int EXP_MASK = 0x7FF;

    /** Number of bits for each digit in the number */
    protected static final int BITS_PER_DIGIT = 62;

    /** The value of the base R for (alpha, beta)-regularized numbers */
    protected static final long BASE = 1L << BITS_PER_DIGIT;

    /**
     * A mask that keeps only the correct value of a digit
     */
    protected static final long DIGIT_MASK = 0xFFFFFFFFFFFFFFFFL >>> (64 - BITS_PER_DIGIT);

    /**
     * Total number of digits to store to be able to represent largest double-precision
     * floating point numbers
     */
    protected static final int NUM_DIGITS = ((1 << EXP_BITS) + MANTISSA_BITS) / BITS_PER_DIGIT + 1;

    /**
     * All the digits in the value of the accumulator. The digit at index i
     * represents the digit at (i + DIGIT_SHIFT)
     */
    protected long[] digits = new long[NUM_DIGITS];

    /**
     * A temporary array for storing carries while adding two numbers. We keep it
     * as an instance variable to avoid recreating it for every add operation
     */
    protected int[] carries = new int[NUM_DIGITS + 1];
    
    protected int minDigit = NUM_DIGITS;
    
    protected int maxDigit = -1;
    
    /**
     * Initialize a new SparseSuperAccumulator with a value of zero
     */
    public SparseSuperAccumulator() {
    }

    /**
     * Initialize a new SparseSuperAccumulator with a double floating-point
     * value.
     * 
     * @param v
     */
    public SparseSuperAccumulator(double value) {
        long ivalue = Double.doubleToRawLongBits(value);
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

        // Store the lowest significant digit
        int index1 = exp / BITS_PER_DIGIT;
        digits[index1] = mantissa << (exp % BITS_PER_DIGIT) & DIGIT_MASK;

        int index2 = index1 + 1;
        digits[index2] = mantissa >> (BITS_PER_DIGIT - exp % BITS_PER_DIGIT);
        

        if (value < 0) {
            digits[index1] = -digits[index1];
            digits[index2] = -digits[index2];
        }
        minDigit = index1;
        maxDigit = index2;
    }

    public void add(Accumulator a) {
        if (!(a instanceof SparseSuperAccumulator))
          throw new RuntimeException("Cannot add accumulators of type: "+a.getClass());
        SparseSuperAccumulator acc = (SparseSuperAccumulator) a;
        Arrays.fill(carries, 0);
        for (int i = 0; i < NUM_DIGITS; i++) {
            this.digits[i] += acc.digits[i];
            if (this.digits[i] >= BASE - 1) {
                carries[i + 1] = 1;
                this.digits[i] -= BASE;
            } else if (this.digits[i] <= -BASE + 1) {
                carries[i + 1] = -1;
                this.digits[i] += BASE;
            }
        }
        
        for (int i = 1; i < NUM_DIGITS; i++) {
            this.digits[i] += carries[i];
        }
    }

    public void add(double value) {
        long ivalue = Double.doubleToRawLongBits(value);
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
  
        // Store the lowest significant digit
        int index1 = exp / BITS_PER_DIGIT;
        long digit1 = mantissa << (exp % BITS_PER_DIGIT) & DIGIT_MASK;
  
        int index2 = index1 + 1;
        long digit2 = mantissa >> (BITS_PER_DIGIT - exp % BITS_PER_DIGIT);
  
        if (value < 0) {
            digit1 = -digit1;
            digit2 = -digit2;
        }
        
        if (index1 < minDigit)
          minDigit = index1;
        
        if (index2 > maxDigit)
          maxDigit = index2;
        
        // Add tempDigits to digits
        Arrays.fill(carries, minDigit, maxDigit+1, 0);
        
        // Add digit 1
        this.digits[index1] += digit1;
        if (this.digits[index1] >= BASE - 1) {
          carries[index1 + 1] = 1;
          this.digits[index1] -= BASE;
        } else if (this.digits[index1] <= -BASE + 1) {
          carries[index1 + 1] = -1;
          this.digits[index1] += BASE;
        }
        
        // Add digit 2
        this.digits[index2] += digit2;
        if (this.digits[index2] >= BASE - 1) {
          carries[index2 + 1] = 1;
          this.digits[index2] -= BASE;
        } else if (this.digits[index2] <= -BASE + 1) {
          carries[index2 + 1] = -1;
          this.digits[index2] += BASE;
        }
        
        // Add 0 to all remaining digits
        for (int i = index2 + 1; i <= maxDigit; i++) {
          if (this.digits[i] >= BASE - 1) {
            carries[i + 1] = 1;
            this.digits[i] -= BASE;
          } else if (this.digits[i] <= -BASE + 1) {
            carries[i + 1] = -1;
            this.digits[i] += BASE;
          }
        }
        
        // Add carries
        for (int i = minDigit; i <= maxDigit; i++) {
            this.digits[i] += carries[i];
        }
    }

    public double doubleValue() {
        int mostSignificantDigit = NUM_DIGITS - 1;
        while (mostSignificantDigit >= 0 && this.digits[mostSignificantDigit] == 0)
            mostSignificantDigit--;
        if (mostSignificantDigit == -1)
            return 0.0;
        double value = getDigitValue(mostSignificantDigit);
        if (mostSignificantDigit > 0)
            value += getDigitValue(mostSignificantDigit - 1);
        return value;
    }

    /**
     * Get the correctly-rounded double value of a specific digit.
     * The value of a digit i is equal to digits[i] * BASE ^ (i + DIGIT_SHIFT)
     * 
     * @param iDigit
     * @return
     */
    protected double getDigitValue(int iDigit) {
        // Get lowest portion of the digit into a double value
        long m1 = digits[iDigit] & 0xFFFFFFFFL; // mantissa value
        int e1 = BITS_PER_DIGIT * iDigit; // exponent (base 2)
        double x1 = Utils.buildFromTrueValues(m1, e1 - 1023 - 52);

        long m2 = digits[iDigit] >> 32; // mantissa value
        int e2 = e1 + 32; // exponent (base 2)
        double x2 = Utils.buildFromTrueValues(m2, e2 - 1023 - 52);

        return x1 + x2;
    }
}
