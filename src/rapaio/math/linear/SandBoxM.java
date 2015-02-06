/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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

package rapaio.math.linear;

import rapaio.data.Frame;
import rapaio.datasets.Datasets;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/4/15.
 */
public class SandBoxM {

    public static void main(String[] args) throws IOException, URISyntaxException {

        Frame df = Datasets.loadIrisDataset().removeVars("class");

        M m = MV.newMCopyOf(df);
        m.summary();

        long start = System.currentTimeMillis();
        LUDecomposition lu1 = new LUDecomposition(m);
        System.out.println((System.currentTimeMillis() - start) / 1000 + " seconds");
        lu1.summary();

        start = System.currentTimeMillis();
        LUDecomposition lu2 = new LUDecomposition(m, LUDecomposition.Method.GAUSSIAN_ELIMINATION);
        System.out.println((System.currentTimeMillis() - start) / 1000 + " seconds");
        lu2.summary();

        System.out.println(lu1.getL().isEqual(lu2.getL()));
        System.out.println(lu1.getU().isEqual(lu2.getU()));

        System.out.println(lu1.getU().det());
    }
}
