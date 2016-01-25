package edu.umn.cs.sumn;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for the {@link SparseSuperAccumulator}.
 */
public class TestSparseSuperAccumulator extends TestCase {
    /**
     * Create the test case
     *
     * @param testName
     *            name of the test case
     */
    public TestSparseSuperAccumulator(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TestSparseSuperAccumulator.class);
    }

    public void testConstruction() {
        double x1 = Utils.build(false, 0, 1023); // 1.0

        SparseSuperAccumulator acc = new SparseSuperAccumulator(x1);
        assertEquals(x1, acc.doubleValue());
    }

    /**
     * Rigorous Test :-)
     */
    public void testBadPrecision() {
        double x1 = Utils.build(false, 0, 1023); // 1.0
        double x2 = Utils.build(false, 0, 1023 - 53); // 1.0 / 2^-53
        double trueSum = x1 + Utils.build(false, 0, 1023 - 53 + 6);

        SparseSuperAccumulator accuSum = new SparseSuperAccumulator();
        accuSum.add(x1);

        for (int i = 0; i < 64; i++)
            accuSum.add(x2);

        assertEquals(trueSum, accuSum.doubleValue());
    }

    public void testCarry() {
        double x1 = Utils.build(false, 0, 1023); // 1.0 
        double trueSum = Utils.build(false, 0, 1023 + 13); // 8192.0

        SparseSuperAccumulator accuSum = new SparseSuperAccumulator();
        for (int i = 0; i < (1 << 13); i++) {
            accuSum.add(x1);
        }
        assertEquals(trueSum, accuSum.doubleValue());
    }

    public void testNegative() {
        double x1 = Utils.build(true, 0, 1023); // -1.0 
        double trueSum = Utils.build(true, 0, 1023 + 13); // -8192.0

        SparseSuperAccumulator accuSum = new SparseSuperAccumulator();
        for (int i = 0; i < (1 << 13); i++) {
            accuSum.add(x1);
        }
        assertEquals(trueSum, accuSum.doubleValue());
    }
}
