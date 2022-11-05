package util.main;

import function.ComplexDomainFunctionWrapper;
import function.definition.ComplexFunctionI;
import function.definition.ComplexDomainFunctionI;
import models.ComplexSum;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;


public class ComplexUtil {

    public static final double PI = Math.PI;
    public static final double HALF_PI = PI / 2;
    public static final double TWo_PI = PI * 2;

    public static final int SIMPSON_13_N_MIN = 2;       // Must be even
    public static final int SIMPSON_13_N_DEFAULT = 100;

    public static final int SIMPSON_38_N_MIN = 3;       // must be multiple of 3
    public static final int SIMPSON_38_N_DEFAULT = 51;

    /* Fourier Transform */
    public static final boolean FOURIER_TRANSFORM_CLOCKWISE = true;
    public static final boolean FOURIER_TRANSFORM_USE_TWO_PI = true;
    public static final int FOURIER_TRANSFORM_SIMPSON_13_N_DEFAULT = 100000;          // TODO: accuracy

    private ComplexUtil() {
    }

    @NotNull
    public static Complex polar(double r, double thetaRadians) {
        if (Double.isNaN(r) || Double.isNaN(thetaRadians)) {
            return Complex.NaN;
        }

        return new Complex(r * Math.cos(thetaRadians), r * Math.sin(thetaRadians));
    }

    public static double constraint(double min, double max, double value) {
        return Math.max(min, Math.min(max, value));
    }

    public static float constraint(float min, float max, float value) {
        return Math.max(min, Math.min(max, value));
    }

    public static long constraint(long min, long max, long value) {
        return Math.max(min, Math.min(max, value));
    }

    public static int constraint(int min, int max, int value) {
        return Math.max(min, Math.min(max, value));
    }

    public static double map(double val, double s0, double e0, double s1, double e1) {
        return s1 + (((val - s0) / (e0 - s0)) * (e1  - s1));
    }

    public static double norm(double start, double end, double value) {
        return ((value - start) / (end - start));
    }

    public static float normF(double start, double end, double value) {
        return (float) norm(start, end, value);
    }

    public static double norm(float start, float end, double value) {
        return (value - start) / (end - start);
    }

    public static float normF(float start, float end, double value) {
        return (float) norm(start, end, value);
    }

    public static double lerp(double start, double end, float t) {
        return start + ((end - start) * t);
    }

    public static float lerp(float start, float end, float t) {
        return start + ((end - start) * t);
    }

    public static double lerp(long start, long end, float t) {
        return (start + ((end - start) * t));
    }


    @NotNull
    public static Complex lerp(@NotNull Complex p0, @NotNull Complex p1, float t) {
        final double _t = 1 - t;
        return new Complex((_t * p0.getReal()) + (t * p1.getReal()), (_t * p0.getImaginary()) + (t * p1.getImaginary()));
    }

    public static double @NotNull[] negate(double @NotNull[] data) {
        for (int i=0; i < data.length; i++) {
            data[i] = -data[i];
        }

        return data;
    }

    public static double @NotNull[] negateCopy(double @NotNull[] data) {
        final double[] newData = new double[data.length];

        for (int i=0; i < data.length; i++) {
            newData[i] = -data[i];
        }

        return newData;
    }

    public static double @NotNull[] scale(double @NotNull[] data, double scale) {
        for (int i=0; i < data.length; i++) {
            data[i] = scale * data[i];
        }

        return data;
    }


    @NotNull
    public static ComplexDomainFunctionI getBaseFunction(@NotNull ComplexDomainFunctionI function) {
        while (function instanceof ComplexDomainFunctionWrapper wrapper) {
            function = wrapper.getBaseFunction();
        }

        return function;
    }




    public static int getDirection(boolean clockwise) {
        return clockwise? 1: -1;
    }


    /**
     * Integrates a complex function using Simpson 1/3 rule
     * In most cases, it is more accurate than Simpson 3/8 and far more than Trapezoid
     *
     * It uses a quadratic (degree 2) interpolation
     *
     * @param f function to integrate
     * @param a lower limit
     * @param b upper limit
     * @param n number of intervals integration range will be divided in, must be EVEN (0 or -ve to use default)
     *
     * @see #simpson38(ComplexFunctionI, double, double, int)
     * */
    @NotNull
    public static Complex simpson13(final @NotNull ComplexFunctionI f, final double a, final double b, int n) {
        // N
        if (n <= 0) {
            n = SIMPSON_13_N_DEFAULT;
        } else if (n < SIMPSON_13_N_MIN) {
            n = SIMPSON_13_N_MIN;
        }

        if ((n % 2) != 0) {
            n++;   /* must be even */
        }

        // main
        final double h = (b - a) / n;
        final ComplexSum s = new ComplexSum();

        // 1. End Points
        s.add(f.compute(a));
        s.add(f.compute(b));

        // 2. Odd Points
        for (int i=1; i < n; i+=2) {
            s.add(f.compute(a + (i * h)), 4);
        }

        // 3. Even Points
        for (int i=2; i < n; i+=2) {
            s.add(f.compute(a + (i * h)), 2);
        }

        return s.mult(h / 3).toComplex();
    }

    @NotNull
    public static Complex simpson13(final @NotNull ComplexFunctionI f, final double a, final double b) {
        return simpson13(f, a, b, 0);
    }


    /**
     * Integrates a complex function using Simpson 3/8 rule
     * In most cases, it is less accurate than Simpson 1/3 but can be twice as accurate in some cases
     * <p>
     * It uses a cubic (degree 3) interpolation
     *
     * @param f function to integrate
     * @param a lower limit
     * @param b upper limit
     * @param n number of intervals integration range will be divided in, must be MULTIPLE OF 3 (0 or -ve to use default)
     *
     * @see #simpson13(ComplexFunctionI, double, double, int)
     * */
    @NotNull
    public static Complex simpson38(final @NotNull ComplexFunctionI f, final double a, final double b, int n) {
        // N
        if (n <= 0) {
            n = SIMPSON_38_N_DEFAULT;
        } else if (n < SIMPSON_38_N_MIN) {
            n = SIMPSON_38_N_MIN;
        }

        int r = n % 3;
        if (r != 0) {
            n += (3 - r);           // must be a multiple of 3
        }

        // main.Main
        final double h = (b - a) / n;
        final ComplexSum s = new ComplexSum();

        // 1. End Points
        s.add(f.compute(a));
        s.add(f.compute(b));

        // 2. Mid Points
        double x = a;
        for (int i=1; i < n; i++) {
            x += h;
            s.add(f.compute(x), (i % 3) != 0? 3: 2);
        }

        return s.mult(h * 0.375 /* 3/8 */).toComplex();
    }

    @NotNull
    public static Complex simpson38(final @NotNull ComplexFunctionI f, final double a, final double b) {
        return simpson38(f, a, b, 0);
    }



    /* ........................................ Fourier Transform ................................ */

    /**
     * gets constant part of power of Fourier Transform exponential term
     * <br>
     * Typically, the exponential term is <code>e<sup>-2.pi.f.t</sup></code>
     * then, constant part of power (independent of t) is -2.pi.f
     *
     * @param direction directional factor, usually given by {@link #getDirection(boolean)}
     * @param frequency the winding frequency
     * @return constant part of power of Fourier Transform exponential term
     * */
    public static double getFourierExpTermPowerCoefficient(int direction, double frequency) {
        double pre = direction * frequency;
        if (FOURIER_TRANSFORM_USE_TWO_PI) {
            pre *= TWo_PI;
        }

        return pre;
    }

    @NotNull
    public static ComplexFunctionI fourierTransformIntegrand(@NotNull ComplexFunctionI f, double frequency, boolean clockwise) {
        final double pre = getFourierExpTermPowerCoefficient(getDirection(clockwise), frequency);
        return t -> f.compute(t).multiply(new Complex(0,pre * t).exp());
    }

    @NotNull
    public static ComplexFunctionI fourierTransformIntegrand(@NotNull ComplexFunctionI f, double frequency) {
        return fourierTransformIntegrand(f, frequency, FOURIER_TRANSFORM_CLOCKWISE);
    }


    @NotNull
    public static ComplexDomainFunctionI fourierTransformIntegrand(@NotNull ComplexDomainFunctionI f, double frequency, boolean clockwise) {
        final ComplexFunctionI func = fourierTransformIntegrand((ComplexFunctionI) f, frequency, clockwise);
        return new ComplexDomainFunctionWrapper(f) {
            @Override
            public @NotNull Complex compute(double input) {
                return func.compute(input);
            }
        };
    }

    @NotNull
    public static ComplexDomainFunctionI fourierTransformIntegrand(@NotNull ComplexDomainFunctionI f, double frequency) {
        return fourierTransformIntegrand(f, frequency, FOURIER_TRANSFORM_CLOCKWISE);
    }


    /* ................................. Fourier Series ................................... */

    // Must be complimentary of coefficient direction
    public static int getFourierSeriesRotorTipDirection() {
        return getDirection(!FOURIER_TRANSFORM_CLOCKWISE);
    }

    @NotNull
    public static Complex fourierTransform(@NotNull ComplexFunctionI f, double frequency, double a, double b, int n) {
        return simpson13(fourierTransformIntegrand(f, frequency), a, b, n > 0? n: FOURIER_TRANSFORM_SIMPSON_13_N_DEFAULT);
    }

    @NotNull
    public static Complex fourierTransform(@NotNull ComplexFunctionI f, double frequency, double a, double b) {
        return fourierTransform(f, frequency, a, b, -1);
    }

    @NotNull
    public static Complex fourierTransform(@NotNull ComplexDomainFunctionI f, double frequency, double a, double b) {
        return fourierTransform(f, frequency, a, b, f.getNumericalIntegrationIntervalCount());
    }

    @NotNull
    public static Complex fourierTransform(@NotNull ComplexDomainFunctionI f, double frequency, int n) {
        return fourierTransform(f, frequency, f.getDomainStart(), f.getDomainEnd(), n);
    }

    @NotNull
    public static Complex fourierTransform(@NotNull ComplexDomainFunctionI f, double frequency) {
        return fourierTransform(f, frequency, f.getNumericalIntegrationIntervalCount());
    }



    /**
     * Fourier Transform output -> Fourier Series coefficient
     * */
    @NotNull
    public static Complex ftToFsCoefficient(@NotNull Complex complex, double domainRange) {
        return complex.divide(domainRange);
    }

    /**
     * Fourier Series Coefficient -> Fourier Transform output
     * */
    @NotNull
    public static Complex fsCoefficientToFt(@NotNull Complex complex, double domainRange) {
        return complex.multiply(domainRange);
    }

    @NotNull
    public static Complex fourierSeriesCoefficient(@NotNull ComplexFunctionI f, double frequency, double a, double b, int n) {
        return ftToFsCoefficient(fourierTransform(f, frequency, a, b, n), b - a);
    }

    @NotNull
    public static Complex fourierSeriesCoefficient(@NotNull ComplexFunctionI f, double frequency, double a, double b) {
        return fourierSeriesCoefficient(f, frequency, a, b, -1);
    }

    @NotNull
    public static Complex fourierSeriesCoefficient(@NotNull ComplexDomainFunctionI f, double frequency, double a, double b) {
        return fourierSeriesCoefficient(f, frequency, a, b, f.getNumericalIntegrationIntervalCount());
    }

    @NotNull
    public static Complex fourierSeriesCoefficient(@NotNull ComplexDomainFunctionI f, double frequency, int n) {
        return fourierSeriesCoefficient(f, frequency, f.getDomainStart(), f.getDomainEnd(), n);
    }

    @NotNull
    public static Complex fourierSeriesCoefficient(@NotNull ComplexDomainFunctionI f, double frequency) {
        return fourierSeriesCoefficient(f, frequency, f.getNumericalIntegrationIntervalCount());
    }

}
