package edu.umn.cs.sumn;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for the {@link SmallSuperAccumulator}.
 */
public class TestSmallSuperAccumulator extends TestCase {
    /**
     * Create the test case
     *
     * @param testName
     *            name of the test case
     */
    public TestSmallSuperAccumulator(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TestSmallSuperAccumulator.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testBadPrecision() {
        double x1 = Utils.build(false, 0, 1023); // 1.0
        double x2 = Utils.build(false, 0, 1023 - 53); // 1.0 / 2^-53
        double trueSum = x1 + Utils.build(false, 0, 1023 - 53 + 6);

        SmallSuperAccumulator accuSum = new SmallSuperAccumulator();
        accuSum.add(x1);

        for (int i = 0; i < 64; i++)
            accuSum.add(x2);

        assertEquals(trueSum, accuSum.doubleValue());
    }

    public void testCarry() {
        double x1 = Utils.build(false, 0, 1023); // 1.0 
        double trueSum = Utils.build(false, 0, 1023 + 13); // 8192.0

        SmallSuperAccumulator accuSum = new SmallSuperAccumulator();
        for (int i = 0; i < (1 << 13); i++) {
            accuSum.add(x1);
        }
        assertEquals(trueSum, accuSum.doubleValue());
    }
}
