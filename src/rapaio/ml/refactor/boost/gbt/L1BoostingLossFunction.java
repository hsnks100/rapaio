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

package rapaio.ml.refactor.boost.gbt;

import rapaio.core.stat.Quantiles;
import rapaio.data.Numeric;
import rapaio.data.Var;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
@Deprecated
public class L1BoostingLossFunction implements BoostingLossFunction {

    @Override
    public double findMinimum(Var y, Var fx) {
        Numeric values = Numeric.newEmpty();
        for (int i = 0; i < y.rowCount(); i++) {
            values.addValue(y.value(i) - fx.value(i));
        }
        return new Quantiles(values, new double[]{0.5}).values()[0];
    }

    @Override
    public Numeric gradient(Var y, Var fx) {
        Numeric gradient = Numeric.newEmpty();
        for (int i = 0; i < y.rowCount(); i++) {
            gradient.addValue(y.value(i) - fx.value(i) < 0 ? -1. : 1.);
        }
        return gradient;
    }
}