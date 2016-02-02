package edu.umn.cs.sumn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apfloat.Apfloat;

import edu.umn.cs.sumn.Parallel.RunnableRange;
import scala.Tuple2;

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

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Please provide input file name");
            System.exit(1);
        }
        final String filename = args[0];
        long t1, t2;
        Function<String, Double> parser = new Function<String, Double>() {
            public Double call(String v1) throws Exception {
                return Double.longBitsToDouble(Long.parseLong(v1));
            }
        };

        // Accumulate using iFastSum
        if (args.length > 1 && (args[1].equalsIgnoreCase("ifastsum") || args[1].equalsIgnoreCase("both"))) {
            // Count input size;
            int size = 0;
            BufferedReader in = new BufferedReader(new FileReader(new File(filename)));
            while (in.readLine() != null) {
                size++;
            }
            in.close();

            final double[] numbers = new double[size];
            in = new BufferedReader(new FileReader(new File(filename)));
            String line;
            int i = 0;
            while ((line = in.readLine()) != null) {
                numbers[i++] = parser.call(line);
            }
            in.close();

            double[] copyNumbers = new double[numbers.length + 1];
            System.arraycopy(numbers, 0, copyNumbers, 1, numbers.length);
            t1 = System.currentTimeMillis();
            double ifsum = new IFastSum().iFastSum(copyNumbers, i - 1);
            t2 = System.currentTimeMillis();
            System.out.printf("---- Computed the accurate sum %g using iFastSum in %f seconds\n", ifsum,
                    (t2 - t1) / 1000.0);

            // Accumulate using Parallel.forEach
            t1 = System.currentTimeMillis();
            List<SparseSuperAccumulator> accs = Parallel.forEach(numbers.length,
                    new RunnableRange<SparseSuperAccumulator>() {
                        @Override
                        public SparseSuperAccumulator run(int i1, int i2) {
                            SparseSuperAccumulator acc = new SparseSuperAccumulator();
                            for (int i = i1; i < i2; i++)
                                acc.add(numbers[i]);
                            return acc;
                        }
                    });
            SparseSuperAccumulator finalResult = new SparseSuperAccumulator();
            for (SparseSuperAccumulator acc : accs) {
                finalResult.add(acc);
            }
            t2 = System.currentTimeMillis();
            System.out.printf("---- Computed the accurate sum %g using SparseSuperAccumulator(forEach) in %f seconds\n",
                    finalResult.doubleValue(), (t2 - t1) / 1000.0);
        }

        // Accumulate using Spark
        if (args.length <= 1 || args[1].equalsIgnoreCase("spark") || args[1].equalsIgnoreCase("both")) {
            JavaSparkContext sc;
            if (args.length > 2 && args[2].equals("-local")) {
                sc = new JavaSparkContext("local", "SumN");
            } else {
                sc = new JavaSparkContext();
            }

            JavaRDD<Double> input = sc.textFile(filename).map(parser).cache();

            // An initial query to warm up
            Double sumn = input.aggregate(new Double(0), simple1, simple2);
            t1 = System.currentTimeMillis();
            sumn = input.aggregate(new Double(0), simple1, simple2);
            t2 = System.currentTimeMillis();
            System.out.println("----- Computed the inaccurate sum: " + sumn.doubleValue() + " in " + (t2 - t1) / 1000.0
                    + " seconds");

            //        t1 = System.currentTimeMillis();
            //        Apfloat sumapfloat = input.aggregate(new Apfloat(0), apfloat1, apfloat2);
            //        t2 = System.currentTimeMillis();
            //        System.out.println("Computed the correct sum using Apfloat: " + sumapfloat.doubleValue() + " in " + (t2 - t1) / 1000.0 + " seconds");

            t1 = System.currentTimeMillis();
            SmallSuperAccumulator sumn2 = input.aggregate(new SmallSuperAccumulator(), small1, small2);
            t2 = System.currentTimeMillis();
            System.out.println("----- Computed the correct sum using SmallSuperAccumulator: " + sumn2.doubleValue()
                    + " in " + (t2 - t1) / 1000.0 + " seconds");

            t1 = System.currentTimeMillis();
            SparseSuperAccumulator sumn3 = input.aggregate(new SparseSuperAccumulator(), sparse1, sparse2);
            t2 = System.currentTimeMillis();
            System.out.println("----- Computed the correct sum using SparseSuperAccumulator: " + sumn3.doubleValue()
                    + " in " + (t2 - t1) / 1000.0 + " seconds");

            t1 = System.currentTimeMillis();
            JavaPairRDD<Integer, Double> partitioned = input.mapToPair(new PairFunction<Double, Integer, Double>() {
                private static final long serialVersionUID = 1L;

                @Override
                public Tuple2<Integer, Double> call(Double x) throws Exception {
                    return new Tuple2(Utils.getExponent(x), x);
                }
            });
            JavaPairRDD<Integer, SparseSuperAccumulator> partialSum = partitioned
                    .aggregateByKey(new SparseSuperAccumulator(), sparse1, sparse2);
            sumn3 = partialSum.aggregate(new SparseSuperAccumulator(),
                    new Function2<SparseSuperAccumulator, Tuple2<Integer, SparseSuperAccumulator>, SparseSuperAccumulator>() {
                        @Override
                        public SparseSuperAccumulator call(SparseSuperAccumulator sum,
                                Tuple2<Integer, SparseSuperAccumulator> t) throws Exception {
                            sum.add(t._2);
                            return sum;
                        }

                    }, sparse2);
            t2 = System.currentTimeMillis();
            System.out.println("----- Computed the correct sum using SparseSuperAccumulator (partitioned): "
                    + sumn3.doubleValue() + " in " + (t2 - t1) / 1000.0 + " seconds");
            sc.close();
        }
    }

}
