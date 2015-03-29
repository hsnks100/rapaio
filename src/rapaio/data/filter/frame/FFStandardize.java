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

package rapaio.data.filter.frame;

import rapaio.data.BoundFrame;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.filter.var.VFStandardize;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/30/15.
 */
public class FFStandardize extends AbstractFF {

    Map<String, VFStandardize> filters = new HashMap<>();

    public FFStandardize(String... varNames) {
        super(varNames);
    }

    @Override
    public void fit(Frame df) {

        filters.clear();
        for (String varName : parse(df, varNames)) {
            VFStandardize filter = new VFStandardize();
            filter.fit(df.getVar(varName));
            filters.put(varName, filter);
        }
    }

    @Override
    public Frame apply(Frame df) {

        Var[] vars = new Var[df.varCount()];
        int pos = 0;
        for (String varName : df.varNames()) {
            if (filters.containsKey(varName)) {
                vars[pos++] = filters.get(varName).apply(df.getVar(varName));
            } else {
                vars[pos++] = df.getVar(varName);
            }
        }
        return BoundFrame.newByVars(vars);
    }
}
