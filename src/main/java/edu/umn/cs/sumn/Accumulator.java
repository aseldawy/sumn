/***********************************************************************
* Copyright (c) 2015 by Regents of the University of Minnesota.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License, Version 2.0 which 
* accompanies this distribution and is available at
* http://www.opensource.org/licenses/apache2.0.php.
*
*************************************************************************/
package edu.umn.cs.sumn;

import scala.Serializable;

/**
 * An interface for an accumulator of double values.
 * 
 * @author Ahmed Eldawy
 */
public interface Accumulator extends Serializable {
    /**
     * Adds the given double value to the accumulator.
     * 
     * @param v
     */
    public void add(double v);

    /**
     * Adds another accumulator, typically, of the same type.
     * 
     * @param a
     */
    public void add(Accumulator a);

    /**
     * Retrieves an approximation of the current double value of the accumulator.
     * 
     * @return
     */
    public double doubleValue();
}
