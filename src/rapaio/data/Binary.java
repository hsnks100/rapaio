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
import java.util.BitSet;
import java.util.function.Function;

/**
 * Numerical variable which store only 1,0 and missing values.
 * This is a storage-optimized version of a binary variable
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public final class Binary extends AbstractVar {

    /**
     * Builds an empty binary var
     *
     * @return new instance of binary var
     */
    public static Binary empty() {
        return new Binary(0, false, false);
    }

    /**
     * Builds a binary variable of given size with filled missing values
     *
     * @param rows size of variable
     * @return new instance of binary var
     */
    public static Binary empty(int rows) {
        return new Binary(rows, true, false);
    }

    /**
     * Builds a new binary variable of given size filled with given value
     *
     * @param rows      size of variable
     * @param fillValue fill value
     * @return new instance of binary var
     */
    public static Binary fill(int rows, boolean fillValue) {
        return new Binary(rows, false, fillValue);
    }

    /**
     * Builds a new binary variable with values copied from given array of values
     *
     * @param values given array of values
     * @return new instance of binary var
     */
    public static Binary copy(int... values) {
        final Binary b = new Binary(values.length, false, false);
        for (int i = 0; i < values.length; i++) {
            if (values[i] == 0) continue;
            if (values[i] == 1) {
                b.setBinary(i, true);
                continue;
            }
            b.setMissing(i);
        }
        return b;
    }

    /**
     * Builds a new binary variable with values copied from the given array of boolean values
     *
     * @param values source values
     * @return new instance of binary var
     */
    public static Binary copy(boolean... values) {
        final Binary b = new Binary(values.length, false, false);
        for (int i = 0; i < values.length; i++) {
            if (values[i]) {
                b.setBinary(i, true);
            }
        }
        return b;
    }

    public static Binary fromIndex(int rows, Function<Integer, Integer> supplier) {
        int[] data = new int[rows];
        for (int i = 0; i < data.length; i++) {
            data[i] = supplier.apply(i);
        }
        return Binary.copy(data);
    }

    public static Binary from(int rows, Function<Integer, Boolean> supplier) {
        boolean[] data = new boolean[rows];
        for (int i = 0; i < data.length; i++) {
            data[i] = supplier.apply(i);
        }
        return Binary.copy(data);
    }


    private static final long serialVersionUID = -4977697633437126744L;
    private int rows;
    private BitSet missing;
    private BitSet values;

    /**
     * Private constructor to avoid instantiation from outside, other than statical builders.
     */
    private Binary(final int rows, final boolean fillMissing, final boolean fillValue) {
        this.rows = rows;
        this.missing = new BitSet(rows);
        this.values = new BitSet(rows);
        if (fillMissing)
            this.missing.flip(0, rows);
        else if (fillValue)
            this.values.flip(0, rows);
    }

    @Override
    public VarType type() {
        return VarType.BINARY;
    }

    @Override
    public Binary withName(String name) {
        return (Binary) super.withName(name);
    }

    void increaseCapacity(int minCapacity) {
        if (minCapacity <= values.size()) {
            return;
        }
        if (minCapacity > rows) {
            minCapacity = Math.max(minCapacity, rows + (rows >> 1));
            BitSet missingCopy = new BitSet(minCapacity);
            BitSet valuesCopy = new BitSet(minCapacity);
            missingCopy.or(missing);
            valuesCopy.or(values);
            missing = missingCopy;
            values = valuesCopy;
        }
    }

    @Override
    public int rowCount() {
        return rows;
    }

    @Override
    public void addRows(int rowCount) {
        increaseCapacity(rows + rowCount);
        for (int i = 0; i < rowCount; i++) {
            missing.set(i + rows);
        }
        rows += rowCount;
    }

    @Override
    public double value(int row) {
        if (missing(row)) return -1.0;
        return values.get(row) ? 1.0 : 0.0;
    }

    @Override
    public void setValue(int row, double value) {
        if (value == 1.0) {
            setBinary(row, true);
            return;
        }
        if (value == 0.0) {
            setBinary(row, false);
            return;
        }
        if (value == -1.0) {
            setMissing(row);
            return;
        }
        throw new IllegalArgumentException(String.format("Value %f is not a valid binary value", value));
    }

    @Override
    public void addValue(double value) {
        if (Math.abs(value - 1.0) <= 10e-3) {
            addBinary(true);
            return;
        }
        if (Math.abs(value) <= 10e-3) {
            addBinary(false);
            return;
        }
        if (Math.abs(value + 1.0) <= 10e-3) {
            addMissing();
            return;
        }
        throw new IllegalArgumentException(String.format("Value %f is not a valid binary value", value));
    }

    @Override
    public int index(int row) {
        if (missing(row))
            return -1;
        return binary(row) ? 1 : 0;
    }

    @Override
    public void setIndex(int row, int value) {
        if (value == 1) {
            setBinary(row, true);
            return;
        }
        if (value == 0) {
            setBinary(row, false);
            return;
        }
        if (value == -1) {
            setMissing(row);
            return;
        }
        throw new IllegalArgumentException(String.format("Value %d is not a valid binary value", value));
    }

    @Override
    public void addIndex(int value) {
        if (value == 1) {
            addBinary(true);
            return;
        }
        if (value == 0) {
            addBinary(false);
            return;
        }
        if (value == -1) {
            addMissing();
            return;
        }
        throw new IllegalArgumentException(String.format("Value %d is not a valid binary value", value));
    }

    @Override
    public String label(int row) {
        return missing(row) ? "?" : (binary(row) ? "true" : "false");
    }

    @Override
    public void setLabel(int row, String value) {
        if ("?".equals(value)) {
            setMissing(row);
            return;
        }
        if ("true".equalsIgnoreCase(value) || "1".equals(value)) {
            setBinary(row, true);
            return;
        }
        if ("false".equalsIgnoreCase(value) || "0".equals(value)) {
            setBinary(row, false);
            return;
        }
        throw new IllegalArgumentException(
                String.format("The value %s could not be converted to a binary value", value));
    }

    @Override
    public void addLabel(String value) {
        if ("?".equals(value)) {
            addMissing();
            return;
        }
        if ("true".equalsIgnoreCase(value) || "1".equals(value)) {
            addBinary(true);
            return;
        }
        if ("false".equalsIgnoreCase(value) || "0".equals(value)) {
            addBinary(false);
            return;
        }
        throw new IllegalArgumentException(
                String.format("The value %s could not be converted to a binary value", value));
    }

    @Override
    public String[] levels() {
        return new String[]{"?", "true", "false"};
    }

    @Override
    public void setLevels(String... dict) {
        throw new IllegalArgumentException("Operation not implemented on binary variables");
    }

    @Override
    public boolean binary(int row) {
        return values.get(row);
    }

    @Override
    public void setBinary(int row, boolean value) {
        if (missing(row))
            missing.set(row, false);
        values.set(row, value);
    }

    @Override
    public void addBinary(boolean value) {
        increaseCapacity(rows + 1);
        setBinary(rows, value);
        rows++;
    }

    @Override
    public long stamp(int row) {
        return binary(row) ? 1L : 0L;
    }

    @Override
    public void setStamp(int row, long value) {
        if (value == 1) {
            setBinary(row, true);
            return;
        }
        if (value == 0) {
            setBinary(row, false);
            return;
        }
        if (value == -1) {
            setMissing(row);
            return;
        }
        throw new IllegalArgumentException(String.format("This value %d is not a valid binary value", value));
    }

    @Override
    public void addStamp(long value) {
        if (value == 1) {
            addBinary(true);
            return;
        }
        if (value == 0) {
            addBinary(false);
            return;
        }
        if (value == -1) {
            addMissing();
            return;
        }
        throw new IllegalArgumentException(String.format("This value %d is not a valid binary value", value));
    }

    @Override
    public boolean missing(int row) {
        return missing.get(row);
    }

    @Override
    public void setMissing(int row) {
        missing.set(row);
    }

    @Override
    public void addMissing() {
        increaseCapacity(rows + 1);
        missing.set(rows);
        rows++;
    }

    @Override
    public void remove(int row) {
        if (row < 0 || row >= rows) {
            throw new IllegalArgumentException();
        }
        for (int i = row + 1; i < rows; i++) {
            values.set(i - 1, values.get(i));
            missing.set(i - 1, missing.get(i));
        }
        rows--;
    }

    @Override
    public void clear() {
        this.rows = 0;
    }

    @Override
    public Var newInstance(int rows) {
        return Binary.empty(rows).withName(name());
    }

    @Override
    public Binary solidCopy() {
        return (Binary) super.solidCopy();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(rowCount());
        byte[] buff = values.toByteArray();
        out.writeInt(buff.length);
        out.write(buff);
        buff = missing.toByteArray();
        out.writeInt(buff.length);
        out.write(buff);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        rows = in.readInt();
        byte[] buff = new byte[in.readInt()];
        in.readFully(buff);
        values = BitSet.valueOf(buff);
        buff = new byte[in.readInt()];
        in.readFully(buff);
        missing = BitSet.valueOf(buff);
    }
}
