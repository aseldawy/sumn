/***********************************************************************
* Copyright (c) 2015 by Regents of the University of Minnesota.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License, Version 2.0 which 
* accompanies this distribution and is available at
* http://www.opensource.org/licenses/apache2.0.php.
*
*************************************************************************/
package edu.umn.cs.sumn;

/**
 * Accumulates all the values to a single double value.
 * 
 * @author Ahmed Eldawy
 */
public class SimpleSum implements Accumulator {

    /** The current value of the accumulator */
    private double value;

    public void add(double v) {
        value += v;
    }

    public double doubleValue() {
        return value;
    }

    public void add(Accumulator a) {
        // Convert the other accumulator to a double-precision value and add it
        this.value += a.doubleValue();
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }
}
