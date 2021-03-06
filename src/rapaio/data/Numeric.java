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

package rapaio.data;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Builds a numeric variable. Numeric variables stores data as double values
 * and allows modelling of any type of continuous or discrete numeric variable.
 * <p>
 * The placeholder for missing value is Double.NaN. Any form of usage of Double.NaN
 * on set/add operation will result in a missing value.
 * <p>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public final class Numeric extends AbstractVar {

    /**
     * @return new empty numeric variable of size 0
     */
    public static Numeric empty() {
        return new Numeric(0, 0, Double.NaN);
    }

    /**
     * Builds an empty numeric var wil all values set missing
     *
     * @param rows size of the variable
     * @return new instance of numeric var
     */
    public static Numeric empty(int rows) {
        return new Numeric(rows, rows, Double.NaN);
    }

    /**
     * Builds a numeric variable with values copied from given collection
     *
     * @param values given values
     * @return new instance of numeric variable
     */
    public static Numeric copy(Collection<? extends Number> values) {
        final Numeric numeric = new Numeric(0, 0, Double.NaN);
        values.forEach(n -> numeric.addValue(n.doubleValue()));
        return numeric;
    }

    /**
     * Builds a numeric variable with values copied from given array of integer values
     *
     * @param values given numeric values
     * @return new instance of numeric variable
     */
    public static Numeric copy(int... values) {
        Numeric numeric = new Numeric(0, 0, 0);
        numeric.data = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            numeric.data[i] = values[i];
        }
        numeric.rows = values.length;
        return numeric;
    }

    /**
     * Builds new instance of numeric var with values copied from given array of doubles
     *
     * @param values given numeric values
     * @return new instance of numeric variable
     */
    public static Numeric copy(double... values) {
        Numeric numeric = new Numeric(values.length, values.length, 0);
        numeric.data = Arrays.copyOf(values, values.length);
        return numeric;
    }

    /**
     * Builds new numeric variable with values copied from another numeric variable
     *
     * @param source source numeric var
     * @return new instance of numeric variable
     */
    public static Numeric copy(Var source) {
        Numeric numeric = new Numeric(source.rowCount(), source.rowCount(), 0).withName(source.name());
        if (!(source instanceof Numeric)) {
            for (int i = 0; i < source.rowCount(); i++) {
                numeric.setValue(i, source.value(i));
            }
        } else {
            numeric.data = Arrays.copyOf(((Numeric) source).data, source.rowCount());
        }
        return numeric;
    }

    /**
     * Builds new numeric variable as a wrapper around an array of doubles
     *
     * @param values wrapped array of doubles
     * @return new instance of numeric variable
     */
    public static Numeric wrap(double... values) {
        Numeric numeric = new Numeric(0, 0, 0);
        numeric.data = values;
        numeric.rows = values.length;
        return numeric;
    }

    /**
     * Builds new numeric variable filled with 0
     *
     * @param rows size of the variable
     * @return new instance of numeric variable of given size and filled with 0
     */
    public static Numeric fill(int rows) {
        return new Numeric(rows, rows, 0);
    }

    /**
     * Builds new numeric variable filled with given fill value
     *
     * @param rows size of the variable
     * @param fill fill value used to set all the values
     * @return new instance of numeric variable of given size and filled with given value
     */
    public static Numeric fill(int rows, double fill) {
        return new Numeric(rows, rows, fill);
    }

    /**
     * Builds a numeric variable of size 1 filled with given value
     *
     * @param value fill value
     * @return new instance of numeric variable of size 1 and filled with given fill value
     */
    public static Numeric scalar(double value) {
        return new Numeric(1, 1, value);
    }

    public static Numeric seq(double end) {
        return seq(0, end);
    }

    public static Numeric seq(double start, double end) {
        return seq(start, end, 1.0);
    }

    public static Numeric seq(double start, double end, double step) {
        Numeric num = Numeric.empty();
        int i = 0;
        while (start + i * step <= end) {
            num.addValue(start + i * step);
            i++;
        }
        return num;
    }

    public static Numeric from(int rows, Supplier<Double> supplier) {
        Numeric numeric = new Numeric(0, 0, 0);
        numeric.data = new double[rows];
        numeric.rows = rows;
        for (int i = 0; i < rows; i++) {
            numeric.data[i] = supplier.get();
        }
        return numeric;
    }

    private static final long serialVersionUID = -3167416341273129670L;

    private static final double missingValue = Double.NaN;
    private double[] data;
    private int rows;


    // private constructor

    private Numeric(int rows, int capacity, double fill) {
        if (rows < 0) {
            throw new IllegalArgumentException("Illegal row count: " + rows);
        }
        this.data = new double[capacity];
        this.rows = rows;
        if (fill != 0)
            Arrays.fill(data, 0, rows, fill);
    }

    // public static builders

    /**
     * Builds a new numeric variable of a given size and values produced by a function
     * which transforms a row number into a value by a given transformation function.
     *
     * @param rows           number of rows
     * @param transformation transformation function
     * @return new numeric variable which contains the computed values
     */
    public static Numeric from(int rows, Function<Integer, Double> transformation) {
        Numeric numeric = new Numeric(0, 0, 0);
        numeric.data = new double[rows];
        numeric.rows = rows;
        for (int i = 0; i < rows; i++) {
            numeric.data[i] = transformation.apply(i);
        }
        return numeric;
    }

    /**
     * Builds a numeric variable as a transformation of another variable.
     * Each value from the source variable is transformed into a value of a destination variable.
     *
     * @param reference source variable which provides data
     * @param transform transformation applied to source variable
     * @return new numeric variable which contains transformed variables
     */
    public static Numeric from(Var reference, Function<Double, Double> transform) {
        return Numeric.from(reference.rowCount(), i -> transform.apply(reference.value(i)));
    }


    // stream collectors
    public static Collector<Double, Numeric, Numeric> collector() {

        return new Collector<Double, Numeric, Numeric>() {
            @Override
            public Supplier<Numeric> supplier() {
                return Numeric::empty;
            }

            @Override
            public BiConsumer<Numeric, Double> accumulator() {
                return Numeric::addValue;
            }

            @Override
            public BinaryOperator<Numeric> combiner() {
                return (x, y) -> {
                    y.stream().forEach(s -> x.addValue(s.value()));
                    return x;
                };
            }

            @Override
            public Function<Numeric, Numeric> finisher() {
                return Numeric::solidCopy;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return new HashSet<>();
            }
        };
    }

    @Override
    public Numeric withName(String name) {
        return (Numeric) super.withName(name);
    }

    @Override
    public VarType type() {
        return VarType.NUMERIC;
    }

    private void ensureCapacity(int minCapacity) {
        minCapacity = Math.max(10, minCapacity);
        // overflow-conscious code
        if (minCapacity - data.length > 0) {
            // overflow-conscious code
            int oldCapacity = data.length;
            int newCapacity = oldCapacity > 0xFFFF ? oldCapacity << 1 : oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0)
                newCapacity = minCapacity;
            data = Arrays.copyOf(data, newCapacity);
        }
    }

    @Override
    public int rowCount() {
        return rows;
    }

    @Override
    public void addRows(int rowCount) {
        ensureCapacity(this.rows + rowCount + 1);
        for (int i = 0; i < rowCount; i++) {
            data[rows + i] = Numeric.missingValue;
        }
        rows += rowCount;
    }

    @Override
    public double value(int row) {
        return data[row];
    }

    @Override
    public void setValue(int row, double value) {
        data[row] = value;
    }

    @Override
    public void addValue(double value) {
        ensureCapacity(rows + 1);
        data[rows++] = value;
    }

    @Override
    public int index(int row) {
        return (int) Math.rint(value(row));
    }

    @Override
    public void setIndex(int row, int value) {
        setValue(row, value);
    }

    @Override
    public void addIndex(int value) {
        ensureCapacity(rows + 1);
        data[rows++] = value;
    }

    @Override
    public String label(int row) {
        if (missing(row))
            return "?";
        return String.valueOf(value(row));
    }

    @Override
    public void setLabel(int row, String value) {
        if ("?".equals(value)) {
            setMissing(row);
            return;
        }
        if ("Inf".equals(value)) {
            setValue(row, Double.POSITIVE_INFINITY);
            return;
        }
        if ("-Inf".equals(value)) {
            setValue(row, Double.NEGATIVE_INFINITY);
            return;
        }
        setValue(row, Double.parseDouble(value));
    }

    @Override
    public void addLabel(String value) {
        if ("?".equals(value)) {
            addMissing();
            return;
        }
        if ("Inf".equals(value)) {
            addValue(Double.POSITIVE_INFINITY);
            return;
        }
        if ("-Inf".equals(value)) {
            addValue(Double.NEGATIVE_INFINITY);
            return;
        }
        addValue(Double.parseDouble(value));
    }

    @Override
    public String[] levels() {
        throw new RuntimeException("Operation not available for numeric vectors.");
    }

    @Override
    public void setLevels(String[] dict) {
        throw new RuntimeException("Operation not available for numeric vectors.");
    }

    @Override
    public boolean binary(int row) {
        return value(row) == 1.0;
    }

    @Override
    public void setBinary(int row, boolean value) {
        setValue(row, value ? 1 : 0);
    }

    @Override
    public void addBinary(boolean value) {
        addValue(value ? 1 : 0);
    }

    @Override
    public long stamp(int row) {
        return (long) Math.rint(value(row));
    }

    @Override
    public void setStamp(int row, long value) {
        setValue(row, Double.valueOf(String.valueOf(value)));
    }

    @Override
    public void addStamp(long value) {
        addValue(Double.valueOf(String.valueOf(value)));
    }

    @Override
    public boolean missing(int row) {
        return value(row) != value(row);
    }

    @Override
    public void setMissing(int row) {
        setValue(row, missingValue);
    }

    @Override
    public void addMissing() {
        addValue(missingValue);
    }

    @Override
    public void remove(int index) {
        int numMoved = rows - index - 1;
        if (numMoved > 0) {
            System.arraycopy(data, index + 1, data, index, numMoved);
            rows--;
        }
    }

    @Override
    public void clear() {
        rows = 0;
    }

    @Override
    public Var newInstance(int rows) {
        return Numeric.empty(rows);
    }

    @Override
    public String toString() {
        return "Numeric[name:" + name() + ", rowCount:" + rowCount() + "]";
    }

    @Override
    public Numeric solidCopy() {
        return (Numeric) super.solidCopy();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(rowCount());
        for (int i = 0; i < rowCount(); i++) {
            out.writeDouble(data[i]);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        rows = in.readInt();
        data = new double[rows];
        for (int i = 0; i < rows; i++) {
            data[i] = in.readDouble();
        }
    }

}
