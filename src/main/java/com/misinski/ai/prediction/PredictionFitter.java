package com.misinski.ai.prediction;

import org.orangepalantir.leastsquares.Fitter;
import org.orangepalantir.leastsquares.Function;
import org.orangepalantir.leastsquares.fitters.LinearFitter;

public class PredictionFitter {

    private final Fitter mFit;

    public PredictionFitter() {
        Function fun = new Function() {
            @Override
            public double evaluate(double[] values, double[] parameters) {
                double A = parameters[0];
                double B = parameters[1];
                double C = parameters[2];
                double x = values[0];
                return A * x * x + B * x + C;
            }

            @Override
            public int getNParameters() {
                return 3;
            }

            @Override
            public int getNInputs() {
                return 1;
            }
        };

        mFit = new LinearFitter(fun);
    }

    public double[] getParameters(double[][] xs, double[] zs) {
        mFit.setData(xs, zs);
        mFit.setParameters(new double[]{1, 0, 1});

        mFit.fitData();

        return mFit.getParameters();
    }
}
