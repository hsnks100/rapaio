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
 *
 */

package rapaio.core.stat;

import rapaio.WS;
import rapaio.printer.Printable;
import rapaio.data.Var;
import rapaio.printer.Printer;

import static rapaio.WS.formatFlex;

/**
 * Finds the minimum value from a {@link rapaio.data.Var} of values.
 * <p>
 * Ignores missing elements.
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 * Date: 9/7/13
 * Time: 12:36 PM
 */
public class Minimum implements Printable {

    private final String varName;
    private final double value;
    private int completeCount;
    private int missingCount;

    public Minimum(Var var) {
        this.varName = var.name();
        for (int i = 0; i < var.rowCount(); i++) {
            if (var.missing(i)) {
                missingCount++;
            } else {
                completeCount++;
            }
        }
        if (var.stream().complete().count() == 0) {
            value = Double.NaN;
        } else {
            value = var.stream().complete().mapToDouble().min().getAsDouble();
        }
    }

    public double value() {
        return value;
    }

    @Override
    public void buildPrintSummary(StringBuilder sb) {
        sb.append(String.format("> minimum[%s]\n", varName));
        sb.append(String.format("total rows: %d (complete: %d, missing: %d)\n", completeCount + missingCount, completeCount, missingCount));
        sb.append(String.format("minimum: %s\n", formatFlex(value)));
    }
}