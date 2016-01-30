package edu.umn.cs.sumn;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apfloat.Apfloat;

/**
 * Hello world!
 */
public class App {
    static Function2<Double, Double, Double> simple1 = new Function2<Double, Double, Double>() {
      public Double call(Double acc, Double v) throws Exception {
        return acc.doubleValue() + v.doubleValue();
      }
    };
  
    static Function2<Double, Double, Double> simple2 = new Function2<Double, Double, Double>() {
      public Double call(Double v1, Double v2) throws Exception {
        return v1.doubleValue() + v2.doubleValue();
      }
    };
  
    static Function2<SmallSuperAccumulator, Double, SmallSuperAccumulator> small1 = new Function2<SmallSuperAccumulator, Double, SmallSuperAccumulator>() {
      public SmallSuperAccumulator call(SmallSuperAccumulator acc, Double v) throws Exception {
        acc.add(v);
        return acc;
      }
  
    };
  
    static Function2<SmallSuperAccumulator, SmallSuperAccumulator, SmallSuperAccumulator> small2 = new Function2<SmallSuperAccumulator, SmallSuperAccumulator, SmallSuperAccumulator>() {
      public SmallSuperAccumulator call(SmallSuperAccumulator v1, SmallSuperAccumulator v2) throws Exception {
        v1.add(v2);
        return v1;
      }
    };
  
    static Function2<SparseSuperAccumulator, Double, SparseSuperAccumulator> sparse1 = new Function2<SparseSuperAccumulator, Double, SparseSuperAccumulator>() {
      public SparseSuperAccumulator call(SparseSuperAccumulator acc, Double v) throws Exception {
        acc.add(v);
        return acc;
      }
  
    };
  
    static Function2<SparseSuperAccumulator, SparseSuperAccumulator, SparseSuperAccumulator> sparse2 = new Function2<SparseSuperAccumulator, SparseSuperAccumulator, SparseSuperAccumulator>() {
      public SparseSuperAccumulator call(SparseSuperAccumulator v1, SparseSuperAccumulator v2) throws Exception {
        v1.add(v2);
        return v1;
      }
    };

    static Function2<Apfloat, Double, Apfloat> apfloat1 = new Function2<Apfloat, Double, Apfloat>() {
      public Apfloat call(Apfloat acc, Double v) throws Exception {
        return acc.add(new Apfloat(v));
      }
    };
  
    static Function2<Apfloat, Apfloat, Apfloat> apfloat2 = new Function2<Apfloat, Apfloat, Apfloat>() {
      public Apfloat call(Apfloat v1, Apfloat v2) throws Exception {
        return v1.add(v2);
      }
    };
  
    public static void main(String[] args) {
        Function<String, Double> parser = new Function<String, Double>() {
            public Double call(String v1) throws Exception {
                return Double.longBitsToDouble(Long.parseLong(v1));
            }
        };

        JavaSparkContext sc = new JavaSparkContext("local", "SumN");

        JavaRDD<Double> input = sc.textFile("Data_random.txt").map(parser).cache();

        // An initial query to warm up
        Double sumn = input.aggregate(new Double(0), simple1, simple2);
        long t1 = System.currentTimeMillis();
        sumn = input.aggregate(new Double(0), simple1, simple2);
        long t2 = System.currentTimeMillis();
        System.out.println("Computed the inaccurate sum: " + sumn.doubleValue() + " in " + (t2 - t1) / 1000.0 + " seconds");

//        t1 = System.currentTimeMillis();
//        Apfloat sumapfloat = input.aggregate(new Apfloat(0), apfloat1, apfloat2);
//        t2 = System.currentTimeMillis();
//        System.out.println("Computed the correct sum using Apfloat: " + sumapfloat.doubleValue() + " in " + (t2 - t1) / 1000.0 + " seconds");

        t1 = System.currentTimeMillis();
        SmallSuperAccumulator sumn2 = input.aggregate(new SmallSuperAccumulator(), small1, small2);
        t2 = System.currentTimeMillis();
        System.out.println("Computed the correct sum using SmallSuperAccumulator: " + sumn2.doubleValue() + " in " + (t2 - t1) / 1000.0 + " seconds");

        t1 = System.currentTimeMillis();
        SparseSuperAccumulator sumn3 = input.aggregate(new SparseSuperAccumulator(), sparse1, sparse2);
        t2 = System.currentTimeMillis();
        System.out.println("Computed the correct sum using SparseSuperAccumulator: " + sumn3.doubleValue() + " in " + (t2 - t1) / 1000.0 + " seconds");
        
        sc.close();
    }

}
