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

package rapaio.data;

import rapaio.data.stream.FSpot;
import rapaio.data.stream.FSpots;

import java.util.stream.IntStream;

/**
 * Base abstract class for a frame, which provides behavior for the utility
 * access methods based on row and column indexes.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public abstract class AbstractFrame implements Frame {

    @Override
    public SolidFrame solidCopy() {
        String[] names = varNames();
        Var[] vars = new Var[names.length];
        for (int i = 0; i < names.length; i++) {
            vars[i] = getVar(names[i]).solidCopy().withName(names[i]);
        }
        return SolidFrame.newWrapOf(vars);
    }

    public FSpots stream() {
        return new FSpots(IntStream.range(0, rowCount()).mapToObj(row -> new FSpot(this, row)), this);
    }
}