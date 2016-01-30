package edu.umn.cs.sumn;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

public class RandomGenerator {
    private static final long MANTISSA_MASK = 0x000FFFFFFFFFFFFFL;
    private static final int EXP_MASK = 0x7FF;
    private static final int MANTISSA_BITS = 52;
    
    public static void main(String[] args) throws FileNotFoundException {
        Random random = new Random();

        final int numRecords = 100000000;
        int delta = 10;

        // Generate positive numbers only
        PrintWriter out = new PrintWriter(new FileOutputStream(new File("Data_R_1.txt")));
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < numRecords ; i++) {
          long value = generateValue(random, true, delta);
          out.println(value);
        }
        out.close();
        long t2 = System.currentTimeMillis();
        System.out.printf("Generated %d records in %f second\n", numRecords, (t2-t1)/1000.0);
        
        // Generate random numbers
        out = new PrintWriter(new FileOutputStream(new File("Data_random.txt")));
        t1 = System.currentTimeMillis();
        for (int i = 0; i < numRecords ; i++) {
          long value = generateValue(random, false, delta);
          out.println(value);
        }
        out.close();
        t2 = System.currentTimeMillis();
        System.out.printf("Generated %d records in %f second\n", numRecords, (t2-t1)/1000.0);
        
        // Anderson's ill-conditioned data
        t1 = System.currentTimeMillis();
        double[] values = new double[numRecords];
        SparseSuperAccumulator sum = new SparseSuperAccumulator();
        for (int i = 0; i < numRecords ; i++) {
          long value = generateValue(random, false, delta);
          values[i] = Double.longBitsToDouble(value);
          sum.add(values[i]);
        }
        double average = sum.doubleValue() / numRecords;
        out = new PrintWriter(new FileOutputStream(new File("Data_Anderson.txt")));
        for (int i = 0; i < numRecords ; i++) {
          values[i] -= average;
          out.println(Double.doubleToLongBits(values[i]));
        }
        out.close();
        t2 = System.currentTimeMillis();
        System.out.printf("Generated %d records in %f second\n", numRecords, (t2-t1)/1000.0);
        values = null;
        
        // Real sum equal to zero
        t1 = System.currentTimeMillis();
        List<Long> vals = new ArrayList<Long>(numRecords);
        for (int i = 0; i < numRecords / 2; i++) {
          long value = generateValue(random, true, delta);
          vals.add(value);
          vals.add(value | 0x8000000000000000L);
        }
        // Shuffle records
        out = new PrintWriter(new FileOutputStream(new File("Data_zero.txt")));
        Collections.shuffle(vals);
        for (int i = 0; i < numRecords; i++) {
          out.println(vals.get(i));
        }
        out.close();
        t2 = System.currentTimeMillis();
        System.out.printf("Generated %d records in %f second\n", numRecords, (t2-t1)/1000.0);
    }
    
    public static long generateValue(Random random, boolean positiveOnly, int delta) {
      boolean negative = positiveOnly ? false : random.nextBoolean(); // Always positive
      long mantissa = Math.abs(random.nextLong()) & MANTISSA_MASK;
      // We don't generate the value 7FF as it represents Infinity and NaN
      int exp = random.nextInt(delta);
      
      long value = negative ? 0x8000000000000000L : 0;
      value |= mantissa;
      value |= exp << MANTISSA_BITS;
      
      return value;
    }
}
