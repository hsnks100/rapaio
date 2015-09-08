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

package rapaio.experiment;

import java.io.IOException;

import rapaio.core.CoreTools;
import rapaio.data.Var;
import rapaio.sys.*;

import static rapaio.graphics.Plotter.bins;
import static rapaio.graphics.Plotter.hist;

public class Sandbox {

    public static void main(String[] args) throws IOException {


        Var p = CoreTools.distNormal(0.5, 0.2).sample(10_000).stream().transValue(x -> 1 - Math.pow(x, 2)).toMappedVar();

        WS.draw(hist(p, 0, 1, bins(100)));
    }

}
