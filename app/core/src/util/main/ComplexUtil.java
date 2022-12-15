package util.main;

import function.ComplexDomainFunctionWrapper;
import function.definition.ComplexFunctionI;
import function.definition.ComplexDomainFunctionI;
import function.definition.DiscreteFunctionI;
import function.definition.FrequencySupportProviderI;
import misc.MathUtil;
import models.ComplexBuilder;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;

import java.util.function.BinaryOperator;
import java.util.function.IntFunction;
import java.util.stream.Stream;

public class ComplexUtil {

    public static final String TAG = "ComplexUtil";

    public static final int SIMPSON_13_N_MIN = 2;       // Must be even
    public static final int SIMPSON_13_N_DEFAULT = 100;

    public static final int SIMPSON_38_N_MIN = 3;       // must be multiple of 3
    public static final int SIMPSON_38_N_DEFAULT = 51;

    /* Fourier Transform */

    /**
     * Defines whether {@link MathUtil#TWO_PI} should be used in Fourier Transform integrand exp term<br>
     * <br>
     * If it is used, then the frequencies decomposed by FT algorithm will be temporal<br>
     * otherwise, angular
     * */
    public static final boolean FOURIER_TRANSFORM_USE_TWO_PI = true;

    /**
     * Defines Fourier Transform integrand exp term sign <br>
     * <strong>
     *     Must be Complimentary to Fourier Series rotor direction
     * </strong>
     * */
    public static final boolean FOURIER_TRANSFORM_CLOCKWISE = true;

    public static int getDirection(boolean clockwise) {
        return clockwise? 1: -1;
    }

    public static final int DIRECTION_FOURIER_TRANSFORM = getDirection(FOURIER_TRANSFORM_CLOCKWISE);
    public static final int DIRECTION_FOURIER_SERIES = getDirection(!FOURIER_TRANSFORM_CLOCKWISE);      // must be opposite to each other


    public static final int FOURIER_TRANSFORM_SIMPSON_13_N_MIN = SIMPSON_13_N_MIN;
    public static final int FOURIER_TRANSFORM_SIMPSON_13_N_DEFAULT = 100000;

    private static int sFtSimpson13NCurrentDefault = FOURIER_TRANSFORM_SIMPSON_13_N_DEFAULT;

    public static boolean setFourierTransformSimpson13NCurrentDefault(int ftSimpson13NCurrentDefault) {
        if (ftSimpson13NCurrentDefault < FOURIER_TRANSFORM_SIMPSON_13_N_MIN)
            return false;

        sFtSimpson13NCurrentDefault = ftSimpson13NCurrentDefault;
        return true;
    }

    public static int getFourierTransformSimpson13NCurrentDefault() {
        return sFtSimpson13NCurrentDefault;
    }


    private ComplexUtil() {
    }

    @NotNull
    public static Complex polarExact(double r, double rad) {
        if (Double.isNaN(r) || Double.isNaN(rad)) {
            return Complex.NaN;
        }

        return new Complex(r * MathUtil.cosexact(rad), r * MathUtil.sinexact(rad));
    }

    @NotNull
    public static Complex polarFast(double r, double rad) {
        return new Complex(r * MathUtil.cosfast(rad), r * MathUtil.sinfast(rad));
    }

    @NotNull
    public static Complex polarUnitExact(double rad) {
        if (Double.isNaN(rad)) {
            return Complex.NaN;
        }

        return new Complex(MathUtil.cosexact(rad), MathUtil.sinexact(rad));
    }

    @NotNull
    public static Complex polarUnitFast(double rad) {
        return new Complex(MathUtil.cosfast(rad), MathUtil.sinfast(rad));
    }



    @NotNull
    public static Complex lerp(@NotNull Complex p0, @NotNull Complex p1, float t) {
        final double _t = 1 - t;
        return new Complex((_t * p0.getReal()) + (t * p1.getReal()), (_t * p0.getImaginary()) + (t * p1.getImaginary()));
    }


    @NotNull
    public static ComplexDomainFunctionI getBaseFunction(@NotNull ComplexDomainFunctionI function) {
        while (function instanceof ComplexDomainFunctionWrapper wrapper) {
            function = wrapper.getBaseFunction();
        }

        return function;
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
        final double range = b - a;
        if (range == 0)
            return Complex.ZERO;        // todo

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
        final double h = range / n;
        final ComplexBuilder s = new ComplexBuilder();

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
        final ComplexBuilder s = new ComplexBuilder();

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

    @NotNull
    public static ComplexBuilder complexExpExact(double x) {
        return new ComplexBuilder(MathUtil.cosexact(x), MathUtil.sinexact(x));
    }

    @NotNull
    public static ComplexBuilder complexExpFast(double x) {
        return new ComplexBuilder(MathUtil.cosfast(x), MathUtil.sinfast(x));
    }

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
            pre *= MathUtil.TWO_PI;
        }

        return pre;
    }

    @NotNull
    public static ComplexFunctionI fourierTransformIntegrand(@NotNull ComplexFunctionI f, double frequency, int direction) {
//        if (f instanceof FrequencySupportProviderI fsp && !fsp.isFrequencySupported(frequency)) {
//            return ComplexFunctionI.ZERO;
//        }

        final double pre = getFourierExpTermPowerCoefficient(direction, frequency);
        return t -> complexExpFast(pre * t).mult(f.compute(t)).toComplex();
    }

    @NotNull
    public static ComplexFunctionI fourierTransformIntegrand(@NotNull ComplexFunctionI f, double frequency) {
        return fourierTransformIntegrand(f, frequency, DIRECTION_FOURIER_TRANSFORM);
    }


    @NotNull
    public static ComplexDomainFunctionI fourierTransformIntegrand(@NotNull ComplexDomainFunctionI f, double frequency, int direction) {
        final ComplexFunctionI ft = fourierTransformIntegrand((ComplexFunctionI) f, frequency, direction);
        return new ComplexDomainFunctionWrapper(f) {
            @Override
            public @NotNull Complex compute(double input) {
                return ft.compute(input);
            }
        };
    }

    @NotNull
    public static ComplexDomainFunctionI fourierTransformIntegrand(@NotNull ComplexDomainFunctionI f, double frequency) {
        return fourierTransformIntegrand(f, frequency, DIRECTION_FOURIER_TRANSFORM);
    }




    /**
     * Core method where integration happens
     *
     * @return fourier transform value of a function for a certain "winding" frequency
     *
     * TODO: implement FASt FOURIER TRANSFORM
     * */
    @NotNull
    public static Complex fourierTransform(@NotNull ComplexFunctionI f, double frequency, double a, double b, int n) {
        // If frequency is not supported
        if (f instanceof FrequencySupportProviderI fsp && !fsp.isFrequencySupported(frequency)) {
            return Complex.ZERO;
        }

        return simpson13(fourierTransformIntegrand(f, frequency), a, b, n > 0? n: getFourierTransformSimpson13NCurrentDefault());


        // todo test
//        final ComplexDomainFunctionI func = new ComplexDomainFunctionI() {
//            @Override
//            public @NotNull Complex compute(double input) {
//                return f.compute(input);
//            }
//
//            @Override
//            public double getDomainStart() {
//                return a;
//            }
//
//            @Override
//            public double getDomainEnd() {
//                return b;
//            }
//        };
//
//        final int N = MathUtil.highestPowOf2(50_000);
//        return FftTest.fft(func, frequency, N);
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


    /* ................................. Fourier Series ................................... */

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
    
    





    // TODO: decrease array overhead
    public static class FftTest {

        private static boolean isInt(double v) {
            return v == Math.floor(v);

    //        return !(Double.isNaN(v) || Double.isInfinite(v)) && v == Math.floor(v);
        }

        /* Single Point FFT */

        public interface SampleProvider {

            SampleProvider EMPTY = new SampleProvider() {
                @Override
                public int sampleCount() {
                    return 0;
                }

                @Override
                @NotNull
                public Complex sampleAt(int index) {
                    throw new IndexOutOfBoundsException(String.format("Index %d out of bounds. Size: 0", index));
                }
            };

            @NotNull
            static SampleProvider create(int sampleCount, @NotNull IntFunction<Complex> sampleProvider) {
                return new SampleProvider() {
                    @Override
                    public int sampleCount() {
                        return sampleCount;
                    }

                    @Override
                    public @NotNull Complex sampleAt(int index) throws IndexOutOfBoundsException {
                        return sampleProvider.apply(index);
                    }
                };
            }


            static SampleProvider single(@NotNull Complex value) {
                return new SampleProvider() {
                    @Override
                    public int sampleCount() {
                        return 1;
                    }

                    @Override
                    public @NotNull Complex sampleAt(int index) throws IndexOutOfBoundsException {
                        if (index != 0)
                            throw new IndexOutOfBoundsException(String.format("Index: %d, Size: %d", index, 1));
                        return value;
                    }
                };
            }

            @NotNull
            static SampleProvider of(@NotNull DiscreteFunctionI discreteFunction) {
                return create(discreteFunction.getSampleCount(), discreteFunction::getSampleAt);
            }

            @NotNull
            static SampleProvider of(@NotNull ComplexDomainFunctionI function, int sampleCount, boolean checkDiscrete) {
                if (checkDiscrete && function instanceof DiscreteFunctionI df) {
                    return of(df);
                }

                if (sampleCount == 0)
                    return EMPTY;

                final double dStart = function.getDomainStart();
                if (sampleCount == 1)
                    return single(function.compute(dStart));

                final double dStep = function.getSampleDomainStep(sampleCount);
                return create(sampleCount, i -> function.compute(dStart + (i * dStep)));
            }



            int sampleCount();

            @NotNull
            Complex sampleAt(int index) throws IndexOutOfBoundsException;

            default boolean isEmpty() {
                return sampleCount() == 0;
            }

            @NotNull
            default Complex reduce(@NotNull BinaryOperator<Complex> reducer) {
                final int n = sampleCount();
                if (n == 0)
                    return Complex.ZERO;

                Complex res = sampleAt(0);
                if (n == 1)
                    return res;

                for (int i=1; i < n; i++) {
                    res = reducer.apply(res, sampleAt(i));
                }

                return res;
            }

            @NotNull
            default SampleProvider withCount(int count) {
                if (count < 0)
                    throw new IllegalArgumentException("Sample count must be >= 0");

                final int myCount = sampleCount();
                final SampleProvider me = this;
                if (myCount == count)
                    return me;

                if (count == 0)
                    return EMPTY;

                return create(count, myCount > count? me::sampleAt: index -> index < myCount? me.sampleAt(index): Complex.ZERO);
            }

            @NotNull
            default SampleProvider halfSamples(boolean even) {
                final int o_count = sampleCount();
                if (o_count < 2)
                    return EMPTY;

                final SampleProvider me = this;
                final int count = o_count % 2 != 0? o_count + 1: o_count;       // multiple of 2

                final IntFunction<Complex> provider;
                if (even) {
                    provider = index -> {
                        final int n = index * 2;
                        return n < o_count? me.sampleAt(n): Complex.ZERO;
                    };
                } else {
                    provider = index -> {
                        final int n = (index * 2) + 1;
                        return n < o_count? me.sampleAt(n): Complex.ZERO;
                    };
                }

                return create(count / 2, provider);
            }
        }


        @NotNull
        public static Complex fftSingleFq(@NotNull SampleProvider sampleProvider, double k) {
            final int N = sampleProvider.sampleCount();
            if (N == 0)
                return Complex.ZERO;

            if (k == 0) {
                return sampleProvider.reduce(Complex::add);     // sum all samples
            }

            sampleProvider = sampleProvider.withCount(MathUtil.lowestPowOf2(N));        // pad with zeroes
            return fftSingleFqInternal(sampleProvider, k);
        }

        @NotNull
        public static Complex fftSingleFqInternal(@NotNull SampleProvider provider, double k) {
            final int N = provider.sampleCount();

            // base case
            if (N == 1) {
                final Complex v = provider.sampleAt(0);
                return isInt(k)? v: complexExpFast(DIRECTION_FOURIER_TRANSFORM * MathUtil.TWO_PI * k).mult(v).toComplex();
            }

            final SampleProvider evenP = provider.halfSamples(true);
            final Complex evenFft = fftSingleFqInternal(evenP, k);

            final SampleProvider oddP = provider.halfSamples(false);
            final Complex oddFft = fftSingleFqInternal(oddP, k);

            // merge   result = even + (wk * odd)
            return complexExpFast(DIRECTION_FOURIER_TRANSFORM * MathUtil.TWO_PI * k / N).mult(oddFft).add(evenFft).toComplex();
        }




        @NotNull
        public static Complex fftSingleFq(@NotNull ComplexDomainFunctionI function, double frequency, int n) {
            return fftSingleFq(SampleProvider.of(function, n, true), frequency * function.getDomainRange()).multiply(function.getDomainRange() / (n > 0? n: 1));

//            return fftSingleFq(function.createSamplesRange(n), function.getDomainRange(), frequency);
        }

        @NotNull
        public static Complex fftSingleFq(@NotNull Complex [] x, double domainRange, double frequency) {
            return fftSingleFq(x, frequency * domainRange).multiply(domainRange / (x.length > 0? x.length: 1));
        }



        /**
         * @param x equally spaced samples
         * @param k fundamental frequency multiplier
         * */
        @NotNull
        public static Complex fftSingleFq(@NotNull Complex [] x, double k) {
            if (x == null || x.length == 0)
                return Complex.ZERO;

            // sum all samples
            if (k == 0) {
                return Stream.of(x).reduce(Complex::add).get();
            }

            // todo auto pad array with zeroes
            if (!MathUtil.isPowOf2(x.length)) {
                throw new IllegalArgumentException("Samples count must be a pow of 2 for a radix-2 Cooley-Tukey FFT");
            }

            return fftSingleFqInternal(x, k);
        }


        @NotNull
        private static Complex fftSingleFqInternal(@NotNull Complex @NotNull[] x, double k) {
            final int N = x.length;

            // base case
            if (N == 1)
                return isInt(k)? x[0]: complexExpFast(DIRECTION_FOURIER_TRANSFORM * MathUtil.TWO_PI * k).mult(x[0]).toComplex();

            final Complex[] half = new Complex[N / 2];

            // even
            int j=0;
            for (int i=0; i < N; i+=2) {
                half[j++] = x[i];
            }

            final Complex evenFft = fftSingleFqInternal(half, k);

            // odd
            j=0;
            for (int i=1; i < N; i+=2) {
                half[j++] = x[i];
            }

            final Complex oddFft = fftSingleFqInternal(half, k);

            // merge   result = even + (wk * odd)
            return complexExpFast(DIRECTION_FOURIER_TRANSFORM * MathUtil.TWO_PI * k / N).mult(oddFft).add(evenFft).toComplex();
        }



        /* Legacy FFT spectrum */

        public static Complex @NotNull[] fftSpectrum(@NotNull Complex [] x) {
            if (x == null || x.length == 0)
                return new Complex[0];

            // todo auto pad array with zeroes
            if (!MathUtil.isPowOf2(x.length)) {
                throw new IllegalArgumentException("Samples count must be a pow of 2 for a radix-2 Cooley-Tukey FFT");
            }

            return fftSpectrumInternal(x);
        }

        private static Complex @NotNull[] fftSpectrumInternal(@NotNull Complex [] x) {
            final int N = x.length;

            // base case
            if (N == 1)
                return new Complex[] { x[0] };

            final int halfN = N / 2;

            final Complex[] half = new Complex[halfN];

            // even
            int j=0;
            for (int i=0; i < N; i+=2) {
                half[j++] = x[i];
            }

            final Complex[] evenFft = fftSpectrumInternal(half);

            // odd
            j=0;
            for (int i=1; i < N; i+=2) {
                half[j++] = x[i];
            }

            final Complex[] oddFft = fftSpectrumInternal(half);

            final Complex[] result = new Complex[N];

            for (int k=0; k < halfN; k++) {
                final Complex left = evenFft[k];
                final Complex right = complexExpFast(DIRECTION_FOURIER_TRANSFORM * MathUtil.TWO_PI * k / N).mult(oddFft[k]).toComplex();

                result[k] = left.add(right);
                result[k + halfN] = left.subtract(right);
            }

            return result;
        }



//        /* TODO: FFT Test (not that fast....yet) */
//
//        private static final int FFT_DEFAULT_NP_HINT = FOURIER_TRANSFORM_SIMPSON_13_N_DEFAULT;
//        private static final int FFT_N_MIN = 4;     // power of 2
//
//        @NotNull
//        public static Complex fft(@NotNull ComplexFunctionI func, double a, double b, double fq, int np_hint) {
//            if (func instanceof FrequencySupportProviderI fsp && !fsp.isFrequencySupported(fq)) {
//                return Complex.ZERO;
//            }
//
//            final double range = b - a;
//            if (range == 0)
//                return Complex.ZERO;
//
//            if (fq == 0)
//                return simpson13(func, a, b, np_hint);
//
//            if (np_hint <= 0) {
//                np_hint = FFT_DEFAULT_NP_HINT;
//            } else if (np_hint < FFT_N_MIN) {
//                np_hint = FFT_N_MIN;
//            }
//
//            final double c = fq * range;            // can be -ve
//            final double absC = Math.abs(c);
//            if (absC < 1 || absC > np_hint)
//                return simpson13(fourierTransformIntegrand(func, fq), a, b, np_hint);
//
//            final double n_hint_d = np_hint / absC;
//            int n_hint = MathUtil.highestPowOf2((int) n_hint_d);
//            if (n_hint < FFT_N_MIN) {
//                n_hint = FFT_N_MIN;              // N_MIN
//            }
//
//            final int n = n_hint;
//
//            final double np_d = absC * n;
//            final double h = range / np_d;
//
//            int np_int = (int) np_d;      // >= N_MIN
//            if (np_int % 2 != 0) {
//                np_int--;           // must be even
//            }
//
//            // building cache
//            final Complex[] cache = new Complex[n];
//            cache[0] = Complex.ONE;
//            cache[n / 4] = Complex.I;
//            cache[n / 2] = Complex.ONE.negate();
//
//            final double ftPre = DIRECTION_FOURIER_TRANSFORM * MathUtil.TWO_PI;
//            final double pre = ftPre * MathUtil.signum(c);
//
//            for (int i=1; i < n / 2; i++) {
//                Complex val = cache[i];
//                if (val == null) {
//                    val = complexExpFast((pre * i) / n).toComplex();
//                    cache[i] = val;
//                }
//
//                cache[i + (n / 2)] = val.negate();
//            }
//
//
//            final ComplexBuilder constant = complexExpFast(ftPre * fq * a);
////        if (Complex.ZERO.equals(constant))
////            return Complex.ZERO;
//
//            final IntFunction<ComplexBuilder> donor = i -> new ComplexBuilder(func.compute(a + (i * h))).mult(cache[i % n]);
//
//            // simpson 13 over [a, a + np_int * h]
//            final ComplexBuilder sum = new ComplexBuilder();
//
//            // end points
//            sum.add(donor.apply(0)).add(donor.apply(np_int));
//
//            // 2. Odd Points
//            for (int i=1; i < np_int; i+=2) {
//                sum.add(donor.apply(i), 4);
//            }
//
//            // 3. Even Points
//            for (int i=2; i < np_int; i+=2) {
//                sum.add(donor.apply(i), 2);
//            }
//
//            // core result
//            sum.mult(constant).mult(h / 3);
//
//            // Left part [a + np_int * h, b)
//            final double bp = a + (np_int * h);
//            if (bp < b) {
//                final Complex left_result = simpson13(fourierTransformIntegrand(func, fq), bp, b, SIMPSON_13_N_MIN);
//                sum.add(left_result);
//            }
//
//            return sum.toComplex();
//        }
//
//        @NotNull
//        public static Complex fft(@NotNull ComplexDomainFunctionI func, double fq) {
//            return fft(func, func.getDomainStart(), func.getDomainEnd(), fq, -1);
//        }


    }
}
