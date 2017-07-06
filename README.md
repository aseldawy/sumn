Sparse SuperAccumulator
=======================

This project provides different algorithms for exactly and efficiently
summing n floating point numbers. The main challenge is to ensure a correct
rounding of the true sum of the input numbers. This cannot be trivially done
using a standard double-precision floating-point accumulator as its precision
is limited and it carries out a rounding step with every addition operation.
With a huge list of numbers, these errors might propagate causing a significant
round-off error. Currently, it contains mainly three implementations:

+ **Sparse SuperAccumulator:**
  This algorithm creates a superaccumulator which consists of several chunks
  that cover all the significant digits of the double-precision floating-point
  domain as defined by [IEEE 754 standard]
  (https://en.wikipedia.org/wiki/IEEE_floating_point).
  This superaccumulator is designed with a logic that completely avoids the
  propagation of carry bits making it more suitable for parallel architectures.
+ **Small SuperAccumulator:**
  This algorithm creates a superaccumulator which consists of several
  /overlapping/ chunks that cover all significant digits. The overlap is
  designed to minimize the propagation of carry bits without completely
  avoiding it.
+ **iFastSum:**
  The iFastSum is an inherently sequential algorithm that sums n-floating point
  numbers by accumulating numbers and corresponding errors.

The output of all these algorithms is a single standard double-precision
floating point number that represent the correct-rounding of the true sum of
the input dataset.

Compiling
=========

The source code is in Java and can be compiled using [Maven]
(http://maven.apache.org/). Once you have JDK, and Maven installed, you need
to type the following command to build the code.

    mvn install

Random Generator
================

This project also contains a random generator that can be used to generate
large datasets for testing. It generates four types of datasets:

1. Well-conditioned data with all positive numbers.
2. A mix of positive and negative random numbers.
3. Anderson's ill-conditioned data which generates a set of numbers and then
   subtracts their arithmetic mean from each number.
4. An extremely ill-conditioned data where the sum is equal to zero.

In all these datasets, an additional parameter (&#x3B4;) can be configured which
defines the range of exponents for all the generated numbers.

Driver
======

To be able to run some experiments on large datasets, we included a driver that
executes the following.

1. Loads a dataset from disk and runs the sequential iFastSum algorithm on it.
2. Loads a dataset from disk and runs the sparse superaccumulator algorithm in
   parallel using the available cores in the machine.
3. A MapReduce implementation of the sparse superaccumulator algorithm that
   uses Spark to sum each block separately, and then sums up all the results
   on a single machine.
4. A similar MapReduce implementation that uses small suepraccumulator instead
   of sparse superaccumulator.

Paper
=====

This code was used in the following paper:

Michael T. Goodrich and Ahmed Eldawy. "Parallel Algorithms for Summing Floating-Point Numbers". In Proceedings of the 28th ACM Symposium on Parallelism in Algorithms and Architectures, ACM SPAA 2016, Asilomar State Beach, CA, July 11 - 13, 2016.
Available on the author's website at
http://www.cs.ucr.edu/~eldawy/

Acknowledgement
===============

The idea of the sparse superaccumulator algorithm was designed by
[Michael T. Goodrich](http://www.ics.uci.edu/~goodrich/) from
[University of California, Irvine](http://www.uci.edu/) and
[Ahmed Eldawy](http://www-users.cs.umn.edu/~eldawy/) from
[University of Minnesota, Twin Cities](http://twin-cities.umn.edu/).

The small superaccumulator algorithm was designed by
[Radford M. Neal](http://www.cs.toronto.edu/~radford/) from
[University of Toronto](http://www.utoronto.ca/). The provided Java algorithm
is a direct translation of the C code snippets that appeared in the following
paper.

R. M. Neal, Fast Exact Summation using Small and Large Superaccumulators.
arXiv ePrint,abs/1505.05571, 2015.

We had to expand those snippets to add carry-propagation logic, addition of
two small superaccumulators, and converting the small superaccumulator into
a correctly-rounded double-precision value.

The iFastSum algorithm was originally invented by Yong-Kang Zhu and
[Wayne B. Hayes](http://www.ics.uci.edu/~wayne)
from [University of California, Irvine](http://www.uci.edu/).
The provided Java implementation is a direct translation of the original
C++ code provided as a supplementary material to the following paper.

Y. K. Zhu and W. B. Hayes. Algorithm 908: Online Exact Summation of Floating-
Point Streams. ACM Transactions on Mathematical Software (ACM TOMS),
37(3):37:1-37:13, September 2010.
DOI=http://dx.doi.org/10.1145/1824801.1824815
