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

package rapaio.ml.regressor.tree;

import rapaio.core.RandomSource;
import rapaio.core.stat.Variance;
import rapaio.data.Frame;
import rapaio.data.Var;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
@Deprecated
public interface RTreeNominalMethod extends Serializable {

    String name();

    List<RTreeCandidate> computeCandidates(RTree c, Frame df, Var weights, String testColName, String targetColName, RTreeTestFunction function);

    RTreeNominalMethod IGNORE = new RTreeNominalMethod() {

        private static final long serialVersionUID = 7275580448899976553L;

        @Override
        public String name() {
            return "IGNORE";
        }

        @Override
        public List<RTreeCandidate> computeCandidates(RTree c, Frame df, Var weights, String testColName, String targetColName, RTreeTestFunction function) {
            return new ArrayList<>();
        }
    };

    RTreeNominalMethod FULL = new RTreeNominalMethod() {

        private static final long serialVersionUID = 2733570883914611103L;

        @Override
        public String name() {
            return "FULL";
        }


        @Override
        public List<RTreeCandidate> computeCandidates(RTree c, Frame df, Var weights, String testColName, String targetColName, RTreeTestFunction function) {

            List<RTreeCandidate> result = new ArrayList<>();
            RTreeCandidate best = null;
            for (int i = 1; i < df.var(testColName).levels().length; i++) {

                Var testVar = df.var(testColName);
                String[] testDict = testVar.levels();

                List<String> labels = new ArrayList<>();
                for (int j = 1; j < testVar.levels().length; j++) {
                    String testLabel = testDict[j];
                    if (testVar.stream().filter(s -> s.label().equals(testLabel)).count() >= c.minCount) {
                        labels.add(testLabel);
                    }
                }

                if (labels.size() < 2) {
                    continue;
                }

                double[] variances = new double[labels.size()];
                for (int j = 0; j < variances.length; j++) {
                    String label = labels.get(j);
                    Var v = df.stream().filter(s -> s.label(testColName).equals(label)).toMappedFrame().var(targetColName);
                    variances[j] = new Variance(v).value();
                }

                double value = c.function.computeTestValue(variances);

                RTreeCandidate candidate = new RTreeCandidate(value, testColName);
                if (best == null) {
                    best = candidate;
                    for (String label : labels) {
                        best.addGroup(testColName + " == " + label, spot -> spot.label(testColName).equals(label));
                    }
                } else {
                    int comp = best.compareTo(candidate);
                    if (comp < 0) continue;
                    if (comp == 0 && RandomSource.nextDouble() > 0.5) continue;
                    best = candidate;
                    for (String label : labels) {
                        best.addGroup(testColName + " == " + label, spot -> spot.label(testColName).equals(label));
                    }
                }
            }
            if (best != null)
                result.add(best);
            return result;
        }
    };

    RTreeNominalMethod BINARY = new RTreeNominalMethod() {

        @Override
        public String name() {
            return "BINARY";
        }


        @Override
        public List<RTreeCandidate> computeCandidates(RTree c, Frame df, Var weights, String testColName, String targetColName, RTreeTestFunction function) {

            List<RTreeCandidate> result = new ArrayList<>();
            RTreeCandidate best = null;
            for (int i = 1; i < df.var(testColName).levels().length; i++) {
                String testLabel = df.var(testColName).levels()[i];

                if (df.stream()
                        .filter(s -> !s.missing(testColName) && s.label(testColName).equals(testLabel))
                        .count() < c.minCount ||
                        df.stream()
                                .filter(s -> !s.missing(testColName) && !s.label(testColName).equals(testLabel))
                                .count() < c.minCount) {
                    continue;
                }

                Var in = df.stream()
                        .filter(s -> !s.missing(testColName) && s.label(testColName).equals(testLabel))
                        .toMappedFrame()
                        .var(targetColName);
                Var out = df.stream()
                        .filter(s -> !s.missing(testColName) && !s.label(testColName).equals(testLabel))
                        .toMappedFrame()
                        .var(targetColName);


                double left = new Variance(in).value();
                double right = new Variance(out).value();
                double value = c.function.computeTestValue(left, right);

                RTreeCandidate candidate = new RTreeCandidate(value, testColName);
                if (best == null) {
                    best = candidate;
                    best.addGroup(testColName + " == " + testLabel, spot -> spot.label(testColName).equals(testLabel));
                    best.addGroup(testColName + " != " + testLabel, spot -> !spot.label(testColName).equals(testLabel));
                } else {
                    int comp = best.compareTo(candidate);
                    if (comp < 0) continue;
                    if (comp == 0 && RandomSource.nextDouble() > 0.5) continue;
                    best = candidate;
                    best.addGroup(testColName + " == " + testLabel, spot -> spot.label(testColName).equals(testLabel));
                    best.addGroup(testColName + " != " + testLabel, spot -> !spot.label(testColName).equals(testLabel));
                }
            }
            if (best != null)
                result.add(best);
            return result;
        }
    };
}