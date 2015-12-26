package edu.umn.cs.sumn;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for the {@link SuperAccumulator}.
 */
public class TestSuperAccumulator extends TestCase {
    /**
     * Create the test case
     *
     * @param testName
     *            name of the test case
     */
    public TestSuperAccumulator(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TestSuperAccumulator.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testBadPrecision() {
        double x1 = Utils.build(false, 0, 1023); // 1.0
        double x2 = Utils.build(false, 0, 1023 - 53); // 1.0 / 2^-53
        double trueSum = x1 + Utils.build(false, 0, 1023 - 53 + 6);

        SuperAccumulator accuSum = new SuperAccumulator();
        accuSum.add(x1);

        for (int i = 0; i < 64; i++) {
            accuSum.add(x2);
        }

        assertTrue(trueSum == accuSum.doubleValue());
    }
}
