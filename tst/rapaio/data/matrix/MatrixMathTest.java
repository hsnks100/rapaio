/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package rapaio.data.matrix;

import org.junit.Before;
import org.junit.Test;
import rapaio.math.linear.LUDecomposition;
import rapaio.math.linear.LinAlg;
import rapaio.math.linear.QRDecomposition;
import rapaio.math.linear.RMatrix;

import static org.junit.Assert.assertTrue;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class MatrixMathTest {

    RMatrix A, B, At, Bt, C;

    @Before
    public void setUp() throws Exception {
        A = LinAlg.newMatrixWrapOf(2, 3,
                1, 3, 1,
                1, 0, 0);
        B = LinAlg.newMatrixWrapOf(2, 3,
                0, 0, 5,
                7, 5, 0);

        At = LinAlg.newMatrixWrapOf(3, 2,
                1, 1,
                3, 0,
                1, 0);
        Bt = LinAlg.newMatrixWrapOf(3, 2,
                0, 7,
                0, 5,
                5, 0);

        C = LinAlg.newMatrixWrapOf(3, 3,
                1, 2, 3,
                4, 3, 0,
                5, 2, 9);
    }

    @Test
    public void basicOperations() {
//        assertEqualsM(
//                plus(A, B),
//                new Matrix(3, new double[]{
//                        1, 3, 6,
//                        8, 5, 0
//                }));
//
//        assertEqualsM(
//                minus(A, B),
//                new Matrix(3, new double[]{
//                        1, 3, -4,
//                        -6, -5, 0
//                })
//        );
//
//        assertEqualsM(
//                minus(plus(minus(A, B), B), A),
//                new Matrix(3, new double[]{
//                        0, 0, 0,
//                        0, 0, 0
//                })
//        );

        assertTrue(A.t().isEqual(At));
        assertTrue(B.t().isEqual(Bt));
    }

    @Test
    public void testInverse() {

        RMatrix I = LinAlg.newMatrixWrapOf(3, 3,
                1, 0, 0,
                0, 1, 0,
                0, 0, 1);

        RMatrix invC = new QRDecomposition(C).solve(I);
        invC.summary();
        C.mult(invC);
        invC = new LUDecomposition(C).solve(I);
        invC.summary();
        C.mult(invC);
    }
}