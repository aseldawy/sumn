package edu.umn.cs.sumn;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        Function<String, Double> parser = new Function<String, Double>() {
            public Double call(String v1) throws Exception {
                return Double.longBitsToDouble(Long.parseLong(v1));
            }
        };

        JavaSparkContext sc = new JavaSparkContext("local", "SumN");

        JavaRDD<Double> input = sc.textFile("big_file.txt").map(parser).cache();

        Function2<Double, Double, Double> simple1 = new Function2<Double, Double, Double>() {
            public Double call(Double acc, Double v) throws Exception {
                return acc.doubleValue() + v.doubleValue();
            }
        };

        Function2<Double, Double, Double> simple2 = new Function2<Double, Double, Double>() {
            public Double call(Double v1, Double v2) throws Exception {
                return v1.doubleValue() + v2.doubleValue();
            }
        };
        
        Function2<SmallSuperAccumulator, Double, SmallSuperAccumulator> small1 = new Function2<SmallSuperAccumulator, Double, SmallSuperAccumulator>() {
            public SmallSuperAccumulator call(SmallSuperAccumulator acc, Double v) throws Exception {
                acc.add(v);
                return acc;
            }

        };

        Function2<SmallSuperAccumulator, SmallSuperAccumulator, SmallSuperAccumulator> small2 = new Function2<SmallSuperAccumulator, SmallSuperAccumulator, SmallSuperAccumulator>() {
            public SmallSuperAccumulator call(SmallSuperAccumulator v1, SmallSuperAccumulator v2) throws Exception {
                v1.add(v2);
                return v1;
            }
        };
        
        Function2<SparseSuperAccumulator, Double, SparseSuperAccumulator> sparse1 = new Function2<SparseSuperAccumulator, Double, SparseSuperAccumulator>() {
          public SparseSuperAccumulator call(SparseSuperAccumulator acc, Double v) throws Exception {
            acc.add(v);
            return acc;
          }
          
        };
        
        Function2<SparseSuperAccumulator, SparseSuperAccumulator, SparseSuperAccumulator> sparse2 = new Function2<SparseSuperAccumulator, SparseSuperAccumulator, SparseSuperAccumulator>() {
          public SparseSuperAccumulator call(SparseSuperAccumulator v1, SparseSuperAccumulator v2) throws Exception {
            v1.add(v2);
            return v1;
          }
        };

        // An initial query to warm up
        Double sumn = input.aggregate(new Double(0), simple1, simple2);
        long t1 = System.currentTimeMillis();
        sumn = input.aggregate(new Double(0), simple1, simple2);
        long t2 = System.currentTimeMillis();
        System.out.println("Computed the inaccurate sum: " + sumn.doubleValue() + " in " + (t2 - t1) / 1000.0 + " seconds");

        t1 = System.currentTimeMillis();
        SmallSuperAccumulator sumn2 = input.aggregate(new SmallSuperAccumulator(), small1, small2);
        t2 = System.currentTimeMillis();
        System.out.println("Computed the correct sum using SmallSuperAccumulator: " + sumn2.doubleValue() + " in " + (t2 - t1) / 1000.0 + " seconds");

        t1 = System.currentTimeMillis();
        SparseSuperAccumulator sumn3 = input.aggregate(new SparseSuperAccumulator(), sparse1, sparse2);
        t2 = System.currentTimeMillis();
        System.out.println("Computed the correct sum using SparseSuperAccumulator: " + sumn3.doubleValue() + " in " + (t2 - t1) / 1000.0 + " seconds");
    }

}
