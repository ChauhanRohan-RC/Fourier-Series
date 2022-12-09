package test;

import app.R;
import function.ComplexDomainFunctionWrapper;
import function.definition.ComplexDomainFunctionI;
import function.definition.ComplexFunctionI;
import function.definition.FrequencySupportProviderI;
import function.internal.basic.SineSignal;
import misc.MathUtil;
import models.ComplexBuilder;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import provider.Providers;
import util.main.ComplexUtil;

import java.util.function.IntFunction;


public class Test {


    private static final String TAG = "Test";





    private static long sStartTime = -1;

    public static void startTime() {
        sStartTime = System.nanoTime();
    }

    public static long endTime() {
        final long start = sStartTime;
        sStartTime = -1;
        if (start <= 0) {
            return -1;
        }

        return System.nanoTime() - start;
    }

    public static void endTimeF() {
        final long delta = endTime();
        if (delta != -1) {
            System.out.printf("Time taken: %.2f (%d ns)%n", delta / 1E6, delta);
        }
    }


    private static void testFft() {
        final ComplexDomainFunctionI func = new ComplexDomainFunctionWrapper(new SineSignal(2, 10)) {
            @Override
            public double getDomainStart() {
                return super.getDomainStart() + 2;
            }

            @Override
            public double getDomainEnd() {
                return super.getDomainEnd() + 2;
            }
        };

//        for (float i = -10; i <= 10; i+=0.5f) {
//            System.out.printf("%f -> %.8f, %.8f%n", i, ComplexUtil.fourierTransform(func, i).abs(), fft(func, i).abs());
//        }


        startTime();
        for (float i = -100; i <= 100; i+=0.5f) {
            ComplexUtil.fourierTransform(func, i);
        }

        endTimeF();


        startTime();
        for (float i = -100; i <= 100; i+=0.5f) {
            ComplexUtil.fft(func, i);
        }

        endTimeF();
    }


    private static void testSin() {
        final float[] arr = new float[100_000_00];

        for (int i=0; i < arr.length; i++) {
            arr[i] = R.RANDOM.nextFloat(-MathUtil.TWO_PI, MathUtil.TWO_PI);
        }

        System.out.println("Exact");
        startTime();
        for (float s: arr) {
            ComplexUtil.complexExpExact(s);
        }
        endTimeF();

        System.out.println("Fast");
        startTime();
        for (float s: arr) {
            ComplexUtil.complexExpFast(s);
        }
        endTimeF();
    }

    public static void main(String[] args) {
        MathUtil.initFast();

        testSin();
//        testFft();
    }

}
