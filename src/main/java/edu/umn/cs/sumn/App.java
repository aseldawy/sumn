package edu.umn.cs.sumn;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        double x1 = Utils.build(false, 0, 1023); // 1.0
        double x2 = Utils.build(false, 0, 1023 - 53); // 1.0 / 2^-53

        double trueSum = x1 + Utils.build(false, 0, 1023 - 53 + 6);

        double approxSum = x1;
        Apfloat accuSum = new Apfloat(0);
        accuSum = accuSum.add(new Apfloat(x1, 1000));
        for (int i = 0; i < 64; i++) {
            approxSum += x2;
            accuSum = accuSum.add(new Apfloat(x2, 1000));
        }

        if (trueSum != approxSum)
            System.out.println("tozz");
        if (trueSum != accuSum.doubleValue())
            System.out.println("tozzen");

    }

}
