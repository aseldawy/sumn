package edu.umn.cs.sumn;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Random;

public class RandomGenerator {
    private static final long MANTISSA_MASK = 0x000FFFFFFFFFFFFFL;
    private static final int EXP_MASK = 0x7FF;
    private static final int MANTISSA_BITS = 52;
    public static void main(String[] args) throws FileNotFoundException {
        Random random = new Random();

        long numRecords = 10000000;
        
        PrintWriter out = new PrintWriter(new FileOutputStream(new File("big_file.txt")));
        
        while (numRecords-- > 0) {
          boolean negative = false; // Always positive
          long mantissa = Math.abs(random.nextLong()) & MANTISSA_MASK;
          // We don't generate the value 7FF as it represents Infinity and NaN
          int exp = random.nextInt(EXP_MASK);
          
          long value = negative ? 0x8000000000000000L : 0;
          value |= mantissa;
          value |= exp << MANTISSA_BITS;
          
          out.println(value);
        }
        
        out.close();
    }
}
