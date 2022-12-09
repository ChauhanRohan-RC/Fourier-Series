package test;

import function.ComplexDomainFunctionWrapper;
import function.definition.ComplexDomainFunctionI;
import function.definition.ComplexFunctionI;
import function.definition.FrequencySupportProviderI;
import function.internal.basic.SineSignal;
import models.ComplexBuilder;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import provider.Providers;
import util.main.ComplexUtil;

import java.util.function.IntFunction;


public class Test {


    private static final String TAG = "Test";


    private static final int DEFAULT_NP_HINT = ComplexUtil.FOURIER_TRANSFORM_SIMPSON_13_N_DEFAULT;

    private static final int N_MIN = 4;     // power of 2

    // TODO: bug which causes frequency domain to squeeze by 1/2
    public static Complex fft(@NotNull ComplexFunctionI func, double a, double b, double fq, int np_hint) {
        if (func instanceof FrequencySupportProviderI fsp && !fsp.isFrequencySupported(fq)) {
            return Complex.ZERO;
        }

        final double range = b - a;
        if (range == 0)
            return Complex.ZERO;

        if (fq == 0)
            return ComplexUtil.simpson13(func, a, b, np_hint);

        if (np_hint <= 0) {
            np_hint = DEFAULT_NP_HINT;
        } else if (np_hint < N_MIN) {
            np_hint = N_MIN;
        }

        final double c = fq * range;            // can be -ve
        final double absC = Math.abs(c);
        if (absC < 1 || absC > np_hint)
            return ComplexUtil.simpson13(ComplexUtil.fourierTransformIntegrand(func, fq), a, b, np_hint);

        final double n_hint_d = np_hint / absC;
        int n_hint = ComplexUtil.highestPowOf2((int) n_hint_d);
        if (n_hint < N_MIN) {
            n_hint = N_MIN;              // N_MIN
        }

        final int n = n_hint;

        final double np_d = absC * n;
        final double h = range / np_d;

        int np_int = (int) np_d;      // >= N_MIN
        if (np_int % 2 != 0) {
            np_int--;           // must be even
        }

        // building cache
        final Complex[] cache = new Complex[n];
        cache[0] = Complex.ONE;
        cache[n / 4] = Complex.I;
        cache[n / 2] = Complex.ONE.negate();

        final double ftPre = ComplexUtil.getDirection(ComplexUtil.FOURIER_TRANSFORM_CLOCKWISE) * ComplexUtil.PI;        // todo, working with pi, not with 2pi
        final double pre = ftPre * ComplexUtil.signum(c);

        for (int i=1; i < n / 2; i++) {
            Complex val = cache[i];
            if (val == null) {
                val = ComplexUtil.complexExp((pre * i) / n);
                cache[i] = val;
            }

            cache[i + (n / 2)] = val.negate();
        }


        final Complex constant = ComplexUtil.complexExp(ftPre * fq * a);
//        if (Complex.ZERO.equals(constant))
//            return Complex.ZERO;

        final IntFunction<Complex> donor = i -> new ComplexBuilder(func.compute(a + (i * h))).mult(cache[i % n]).toComplex();

        // simpson 13 over [a, a + np_int * h]
        final ComplexBuilder sum = new ComplexBuilder();

        // end points
        sum.add(donor.apply(0)).add(donor.apply(np_int));

        // 2. Odd Points
        for (int i=1; i < np_int; i+=2) {
            sum.add(donor.apply(i), 4);
        }

        // 3. Even Points
        for (int i=2; i < np_int; i+=2) {
            sum.add(donor.apply(i), 2);
        }

        // core result
        sum.mult(constant).mult(h / 3);

        // Left part [a + np_int * h, b)
        final double bp = a + (np_int * h);
        if (bp < b) {
            final Complex left_result = ComplexUtil.simpson13(ComplexUtil.fourierTransformIntegrand(func, fq), bp, b, ComplexUtil.SIMPSON_13_N_MIN);
            sum.add(left_result);
        }

        return sum.toComplex();
    }


    public static Complex fft(@NotNull ComplexDomainFunctionI func, double fq) {
        return fft(func, func.getDomainStart(), func.getDomainEnd(), fq, -1);
    }


    private static long sStartTime = -1;

    public static void startTime() {
        sStartTime = System.currentTimeMillis();
    }

    public static long endTime() {
        final long start = sStartTime;
        sStartTime = -1;
        if (start <= 0) {
            return -1;
        }

        return System.currentTimeMillis() - start;
    }

    public static void endTimeF() {
        final long delta = endTime();
        if (delta != -1) {
            System.out.printf("Time taken: %d ms%n", delta);
        }
    }

    public static void main(String[] args) {
        final ComplexDomainFunctionI func = new ComplexDomainFunctionWrapper(Providers.SINE_SIGNAL.getFunction()) {
            @Override
            public double getDomainStart() {
                return super.getDomainStart() + 2;
            }

            @Override
            public double getDomainEnd() {
                return super.getDomainEnd() + 2;
            }
        };

        for (float i = -10; i <= 10; i+=0.5f) {
            System.out.printf("%f -> %.8f, %.8f%n", i, ComplexUtil.fourierTransform(func, i).abs(), fft(func, i).abs());
        }



//        startTime();
//        for (float i = -100; i <= 100; i+=0.5f) {
//            ComplexUtil.fourierTransform(func, i);
//        }
//
//        endTimeF();


//        startTime();
//        for (float i = -100; i <= 100; i+=0.5f) {
//            fft(func, i);
//        }
//
//        endTimeF();
    }

}
