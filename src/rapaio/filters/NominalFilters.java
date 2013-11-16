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

package rapaio.filters;

import rapaio.data.*;
import rapaio.data.Vector;

import java.util.*;

/**
 * Provides filter operations on numeric vectors.
 * <p/>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public final class NominalFilters {

    private NominalFilters() {
    }

    /**
     * Set missing values for all nominal values included
     * in missing values array {@param missingValues}.
     *
     * @param vector        source vector
     * @param missingValues labels for missing values
     * @return original vector with missing value on matched positions
     */
    public static Vector fillMissingValues(Vector vector, String[] missingValues) {
        if (!vector.isNominal()) {
            throw new IllegalArgumentException("Vector is not isNominal.");
        }
        for (int i = 0; i < vector.getRowCount(); i++) {
            String value = vector.getLabel(i);
            for (String missingValue : missingValues) {
                if (value.equals(missingValue)) {
                    vector.setMissing(i);
                    break;
                }
            }
        }
        return vector;
    }

    /**
     * Split a frame into multiple frames, one for each label in
     * the term dictionary of the nominal value given as parameter.
     *
     * @param df           source frame
     * @param nominalIndex index fo the nominal column
     * @return an array of frames, one for each nominal label
     */
    public static Frame[] groupByNominal(final Frame df, final int nominalIndex) {
        if (!df.getCol(nominalIndex).isNominal()) {
            throw new IllegalArgumentException("Index does not specify a nominal attribute");
        }
        int len = df.getCol(nominalIndex).getDictionary().length;
        ArrayList<Integer>[] mappings = new ArrayList[len];
        for (int i = 0; i < len; i++) {
            mappings[i] = new ArrayList<>();
        }
        for (int i = 0; i < df.getRowCount(); i++) {
            mappings[df.getIndex(i, nominalIndex)].add(df.getRowId(i));
        }
        Frame[] frames = new Frame[len];
        for (int i = 0; i < frames.length; i++) {
            frames[i] = new MappedFrame(df.getSourceFrame(), new Mapping(mappings[i]));
        }
        return frames;
    }

    /**
     * Consolidates all the common nominal columns from all frames given as parameter.
     *
     * @param source list of source frames
     * @return list of frames with common nominal columns consolidated
     */
    public static List<Frame> consolidate(List<Frame> source) {

        // build reunion of labels for all columns
        HashMap<String, HashSet<String>> dicts = new HashMap<>();
        for (int i = 0; i < source.size(); i++) {
            for (Frame frame : source) {
                for (String colName : frame.getColNames()) {
                    if (!frame.getCol(colName).isNominal()) {
                        continue;
                    }
                    if (!dicts.containsKey(colName)) {
                        dicts.put(colName, new HashSet<String>());
                    }
                    dicts.get(colName).addAll(Arrays.asList(frame.getCol(colName).getDictionary()));
                }
            }
        }

        // rebuild each frame according with the new consolidated data
        List<Frame> dest = new ArrayList<>();
        for (Frame frame : source) {
            Vector[] vectors = new Vector[frame.getColCount()];
            for (int i = 0; i < frame.getColCount(); i++) {
                Vector v = frame.getCol(i);
                String colName = frame.getColNames()[i];
                if (!v.isNominal()) {
                    vectors[i] = v;
                } else {
                    Vector newv = new NominalVector(colName, v.getRowCount(), dicts.get(colName));
                    for (int k = 0; k < newv.getRowCount(); k++) {
                        newv.setLabel(k, v.getLabel(k));
                    }
                    vectors[i] = newv;
                }
            }
            dest.add(new SolidFrame(frame.getName(), frame.getRowCount(), vectors));
        }

        return dest;
    }

    public static List<Frame> combine(String name, List<Frame> frames, String... combined) {
        Set<String> dict = new HashSet<>();
        dict.add("");
        for (int i = 0; i < frames.size(); i++) {
            if (frames.get(i).isMappedFrame()) {
                throw new IllegalArgumentException("Not allowed mapped frames");
            }
        }
        for (int j = 0; j < combined.length; j++) {
            String[] vdict = frames.get(0).getCol(combined[j]).getDictionary();
            Set<String> newdict = new HashSet<>();
            for (String term : dict) {
                for (int k = 0; k < vdict.length; k++) {
                    newdict.add(term + "." + vdict[k]);
                }
            }
            dict = newdict;
        }
        List<Frame> result = new ArrayList<>();
        for (int i = 0; i < frames.size(); i++) {
            List<Vector> vectors = new ArrayList<>();
            for (int j = 0; j < frames.get(i).getColCount(); j++) {
                vectors.add(frames.get(i).getCol(j));
            }
            Vector col = new NominalVector(name, frames.get(i).getRowCount(), dict);
            for (int j = 0; j < frames.get(i).getRowCount(); j++) {
                StringBuilder sb = new StringBuilder();
                for (int k = 0; k < combined.length; k++) {
                    sb.append(".").append(frames.get(i).getLabel(j, frames.get(i).getColIndex(combined[k])));
                }
                col.setLabel(j, sb.toString());
            }
            vectors.add(col);
            result.add(new SolidFrame(frames.get(i).getName(), frames.get(i).getRowCount(), vectors));
        }
        return result;
    }

}
