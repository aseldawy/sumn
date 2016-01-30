package edu.umn.cs.sumn;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for the {@link IFastSum}.
 */
public class TestIFastSum extends TestCase {
    /**
     * Create the test case
     *
     * @param testName
     *            name of the test case
     */
    public TestIFastSum(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TestIFastSum.class);
    }

    /**
     * Rigorous Test :-)
     */
    public void testBadPrecision() {
        double[] values = new double[66];
        values[1] = Utils.build(false, 0, 1023); // 1.0
        for (int i = 2; i < values.length; i++)
            values[i] = Utils.build(false, 0, 1023 - 53); // 1.0 / 2^-53
        double trueSum = values[1] + Utils.build(false, 0, 1023 - 53 + 6);

        double accuSum = new IFastSum().iFastSum(values, values.length - 1);

        assertEquals(trueSum, accuSum);
    }

    public void testCarry() {
        double[] values = new double[(1 << 13) + 1];
        double trueSum = Utils.build(false, 0, 1023 + 13); // 8192.0

        for (int i = 1; i < values.length; i++) {
            values[i] = Utils.build(false, 0, 1023); // 1.0
        }
        double accuSum = new IFastSum().iFastSum(values, values.length - 1);
        assertEquals(trueSum, accuSum);
    }

    public void testNegative() {
        double[] values = new double[(1 << 13) + 1];
        double trueSum = Utils.build(true, 0, 1023 + 13); // -8192.0

        for (int i = 1; i < values.length; i++) {
            values[i] = Utils.build(true, 0, 1023); // -1.0
        }

        double accuSum = new IFastSum().iFastSum(values, values.length - 1);

        assertEquals(trueSum, accuSum);
    }
}
