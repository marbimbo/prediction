package com.misinski.ai.prediction;

import org.orangepalantir.leastsquares.Fitter;
import org.orangepalantir.leastsquares.Function;
import org.orangepalantir.leastsquares.fitters.LinearFitter;

public class PredictionFitter {

    private final Fitter fit;

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

        fit = new LinearFitter(fun);
    }

    public double[] getFunction(double[][] xs, double[] zs) {

        // TODO: 14.03.18 better approach
        xs = new double[xs.length][1];
        for (int i = 0; i < xs.length; ++i) {
            xs[i][0] = i;
        }

        fit.setData(xs, zs);
        fit.setParameters(new double[]{1, 0, 1});

        fit.fitData();

        return fit.getParameters();
    }
}
