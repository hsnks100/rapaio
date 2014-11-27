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

package rapaio.ml.varselect;

import rapaio.data.Frame;
import rapaio.data.VarRange;

import java.io.Serializable;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public interface VarSelector extends Serializable {

    String name();

    void initialize(Frame df, VarRange except);

    String[] nextVarNames();
}