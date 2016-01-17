package edu.umn.cs.sumn;

import org.apfloat.Apfloat;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName
     *            name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    /**
     * Rigorous Test :-)
     */
    public void testBadPrecision() {
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

        assertTrue(trueSum != approxSum);
        assertTrue(trueSum == accuSum.doubleValue());
    }
}
