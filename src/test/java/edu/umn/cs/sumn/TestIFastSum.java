/***********************************************************************
* Copyright (c) 2015 by Regents of the University of Minnesota.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License, Version 2.0 which 
* accompanies this distribution and is available at
* http://www.opensource.org/licenses/apache2.0.php.
*
*************************************************************************/
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

    public void testSumZero() {
        long[] ivalues = new long[] { -9223240809135623413L, 131227719152395L, -9219116495164954388L,
                -9220980306537547756L, -9219819087487567600L, 1042213837590335L, 1543112924271844L, 1522897075086126L,
                -9220037183202067266L, 3552949367208208L, -9221509017254755752L, 3334853652708542L, 1863019600020056L,
                -9219471257886610259L, -9222329823017185473L, -9221849139779689682L, 3900778968165549L,
                2391730317228052L, 4255541689821420L, -9221828923930503964L, };
        double[] values = new double[ivalues.length + 1];
        for (int i = 0; i < ivalues.length; i++) {
            values[i + 1] = Double.longBitsToDouble(ivalues[i]);
        }

        double accuSum = new IFastSum().iFastSum(values, values.length - 1);
        double trueSum = 0;

        assertEquals(trueSum, accuSum);
    }
}
