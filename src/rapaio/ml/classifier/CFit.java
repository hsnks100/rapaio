/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

package rapaio.ml.classifier;

import rapaio.data.Frame;
import rapaio.data.Nominal;
import rapaio.data.SolidFrame;
import rapaio.ml.eval.Confusion;
import rapaio.printer.Printable;

import java.util.*;

/**
 * Classification fit result.
 * <p>
 * This object holds the result of a classification fitting.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class CFit implements Printable {

    private final Classifier model;
    private final Frame df;
    private final List<String> targetNames = new ArrayList<>();
    private final boolean withClasses;
    private final boolean withDensities;
    private final Map<String, String[]> dictionaries = new HashMap<>();
    private final Map<String, Nominal> classes = new HashMap<>();
    private final Map<String, Frame> densities = new HashMap<>();

    // builder

    private CFit(final Classifier model, final Frame df, final boolean withClasses, final boolean withDensities) {
        this.model = model;
        this.df = df;
        this.withClasses = withClasses;
        this.withDensities = withDensities;

        for (String target : model.targetNames()) {
            targetNames.add(target);
            dictionaries.put(target, model.targetLevels(target));
            if (withClasses) {
                classes.put(target, Nominal.empty(df.rowCount(), model.targetLevels(target)).withName(target));
            }
            if (withDensities) {
                densities.put(target, SolidFrame.matrix(df.rowCount(), model.targetLevels(target)));
            }
        }
    }

    // private constructor

    public static CFit build(
            final Classifier model,
            final Frame df,
            final boolean withClasses,
            final boolean withDensities) {
        return new CFit(model, df, withClasses, withDensities);
    }

    public boolean isWithClasses() {
        return withClasses;
    }

    public boolean isWithDensities() {
        return withDensities;
    }

    /**
     * Returns target variables built at learning time
     *
     * @return target variable names
     */
    public String[] targetNames() {
        return targetNames.toArray(new String[targetNames.size()]);
    }

    /**
     * Returns first target variable built at learning time
     *
     * @return target variable names
     */
    public String firstTargetName() {
        return targetNames.get(0);
    }

    /**
     * Returns levels used at learning times for target variables
     *
     * @return map with target variable names as key and levels as variables
     */
    public Map<String, String[]> dictionaries() {
        return dictionaries;
    }

    /**
     * Returns levels used at learning times for first target variables
     *
     * @return map with target variable names as key and levels as variables
     */
    public String[] firstDictionary() {
        return dictionaries.get(firstTargetName());
    }

    /**
     * Returns levels used at learning time for the given target variable
     */
    public String[] dictionary(String targetVar) {
        return dictionaries.get(targetVar);
    }

    /**
     * Returns predicted target classes for each target variable name
     *
     * @return map with nominal variables as predicted classes
     */
    public Map<String, Nominal> classes() {
        return classes;
    }

    /**
     * Returns predicted target classes for first target variable name
     *
     * @return nominal variable with predicted classes
     */
    public Nominal firstClasses() {
        return classes.get(firstTargetName());
    }

    /**
     * Returns predicted target classes for given target variable name
     *
     * @param targetVar given target variable name
     * @return nominal variable with predicted classes
     */
    public Nominal classes(String targetVar) {
        return classes.get(targetVar);
    }

    /**
     * Returns predicted class densities frame if is computed,
     * otherwise returns null.
     *
     * @return predicted class densities (frame with one
     * column for each target class, including missing value)
     */
    public Map<String, Frame> densities() {
        return densities;
    }

    /**
     * Returns predicted class density for the first target variable if is computed,
     * otherwise returns null.
     *
     * @return predicted class densities (frame with one
     * column for each target class, including missing value)
     */
    public Frame firstDensity() {
        return densities().get(firstTargetName());
    }

    /**
     * Returns predicted class density for the given variable if densities are computed,
     * otherwise returns null.
     *
     * @param targetVar given target variable name
     * @return map of frames for each target class, with frames having variables for each
     * classification label
     */
    public Frame density(final String targetVar) {
        return densities.get(targetVar);
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();

        sb.append("Classification Result Summary").append("\n");
        sb.append("=============================\n");
        sb.append("\n");

        sb.append("Model type: ").append(model.name()).append("\n");
        sb.append("Model instance: ").append(model.fullName()).append("\n");
        sb.append("\n");

        sb.append("Predicted frame summary:\n");
        sb.append("> rows: ").append(df.rowCount()).append("\n");
        sb.append("> vars: ").append(df.varCount()).append("\n");
        sb.append("> targets: ").append(Arrays.deepToString(model.targetNames())).append("\n");
        sb.append("> inputs: ").append(Arrays.deepToString(model.inputNames())).append("\n");
        sb.append("\n");

        sb.append("Classification results:").append("\n");
        if (Arrays.asList(df.varNames()).contains(firstTargetName())) {
            sb.append(new Confusion(df.var(model.firstTargetName()), firstClasses()).summary());
        } else {
            sb.append("data frame does not contain target variable.");
        }
        return sb.toString();
    }
}
