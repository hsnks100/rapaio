package rapaio.ml.simple;

import rapaio.core.ColRange;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.SolidFrame;
import rapaio.data.Vector;
import rapaio.ml.AbstractRegressor;
import rapaio.ml.Regressor;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class RandomValueRegressor extends AbstractRegressor {
	List<String> targets;
	double startValue;
	double stopValue;
	List<Vector> fitValues;

	@Override
	public Regressor newInstance() {
		return new L2ConstantRegressor();
	}

	public double getStartValue() {
		return startValue;
	}

	public RandomValueRegressor setStartValue(double startValue) {
		this.startValue = startValue;
		return this;
	}

	public double getStopValue() {
		return stopValue;
	}

	public RandomValueRegressor setStopValue(double stopValue) {
		this.stopValue = stopValue;
		return this;
	}

	private double getRandomValue() {
		return RandomSource.nextDouble() * (stopValue - startValue) + startValue;
	}

	@Override
	public void learn(Frame df, List<Double> weights, String targetColName) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void learn(Frame df, String targetColNames) {
		ColRange colRange = new ColRange(targetColNames);
		List<Integer> colIndexes = colRange.parseColumnIndexes(df);

		targets = new ArrayList<>();
		for (int i = 0; i < colIndexes.size(); i++) {
			targets.add(df.getColNames()[colIndexes.get(i)]);
		}

		fitValues = new ArrayList<>();
		for (String target : targets) {
			double customValue = getRandomValue();
			fitValues.add(new Numeric(df.getCol(target).getRowCount(), df.getCol(target).getRowCount(), customValue));
		}
	}

	@Override
	public void predict(Frame df) {
		fitValues = new ArrayList<>();
		for (int i = 0; i < targets.size(); i++) {
			fitValues.add(new Numeric(df.getRowCount()));
			for (int j = 0; j < df.getRowCount(); j++) {
				fitValues.get(i).setValue(j, getRandomValue());
			}
		}
	}

	@Override
	public Numeric getFitValues() {
		return (Numeric) fitValues.get(0);
	}

	@Override
	public Frame getAllFitValues() {
		return new SolidFrame(fitValues.get(0).getRowCount(), fitValues, targets);
	}
}