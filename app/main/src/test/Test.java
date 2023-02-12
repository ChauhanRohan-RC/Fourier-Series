package test;

import app.R;
import app.Settings;
import function.definition.ComplexDomainFunctionI;
import function.internal.basic.SineSignal;
import misc.Format;
import misc.MathUtil;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.Nullable;
import util.main.ComplexUtil;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;


public class Test {

    private static final String TAG = "Test";

    private static long sStartTime = -1;

    public static void startTime() {
        sStartTime = System.nanoTime();
    }

    public static void startTimeF(@Nullable String title) {
        System.out.println("\nTiming Task: " + (Format.notEmpty(title)? title: "Unknown"));
        startTime();
    }

    public static void startTimeF() {
        startTimeF(null);
    }

    public static long endTime() {
        final long start = sStartTime;
        sStartTime = -1;
        if (start <= 0) {
            return -1;
        }

        return System.nanoTime() - start;
    }

    public static void endTimeF(@Nullable String title) {
        final long delta = endTime();
        if (delta != -1) {
            System.out.printf("%s: %.2f ms (%d ns)%n", Format.notEmpty(title)? title: "Time Taken", delta / 1E6, delta);
        }
    }

    public static void endTimeF() {
        endTimeF(null);
    }


//    private static void testFftOld() {
//        final ComplexDomainFunctionI func = new SineSignal(2, 100);
//
////        for (float i = -10; i <= 10; i+=0.5f) {
////            System.out.printf("%f -> %.8f, %.8f%n", i, ComplexUtil.fourierTransform(func, i).abs(), fft(func, i).abs());
////        }
//
//        startTime();
//        double error = 0;
//
//        for (float i = -100; i <= 100; i+=0.5f) {
//            double ft = ComplexUtil.fourierTransform(func, i).abs();
//            double fft = ComplexUtil.FftTest.fft(func, i).abs();
//
//            double err = Math.abs(fft - ft) * 100 / ft;
//            System.out.println("Error: " + err);
//
//            error += err;
//        }
//
//        endTimeF();
//        System.out.println("Average error: " + (error / 400));
//
//
////        startTime();
////        for (float i = -100; i <= 100; i+=0.5f) {
////            ComplexUtil.fft(func, i);
////        }
////
////        endTimeF();
//    }


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


    private static void testFft() {
        final ComplexDomainFunctionI func = new SineSignal(2, 4);

//        final Complex[] samples = func.createSamplesRange(MathUtil.lowestPowOf2(ComplexUtil.FOURIER_TRANSFORM_SIMPSON_13_N_MIN));
//
//        startTime();
//        final Complex res = FftTest.fft(samples, 20);
//        endTimeF();
//
//        System.out.println("Result: " + res.abs());
//        System.out.println("Trivial result: " + ComplexUtil.fourierTransform(func, 2).abs());

//        final Complex[] samples = func.createSamplesRange(MathUtil.highestPowOf2(50000));

//        final Complex[] resultP = FFTPrinceton.fft(samples);
//        final Complex[] result = FftTest.fft(samples);

//        for (int k=0; k < result.length; k++) {
//            System.out.printf("%.2f -> %.2f, %.2f%n", (k * func.getFundamentalFrequency()), result[k].abs(), resultP[k].abs());
//        }


        final int N = MathUtil.highestPowOf2(100000);
        final Complex[] samples = func.createSamplesRange(N);

        startTimeF("Continuous FT");
        for (int i=0; i < 100; i++) {
            ComplexUtil.fourierTransform(func, i, N);
        }
        endTimeF("Continuous FT");

        startTimeF("FFT");
        for (int i=0; i < 100; i++) {
            ComplexUtil.FftTest.fftSingleFq(func, i, N);
        }

        endTimeF("FFT");
    }


    private static void testNotation() {
        final DecimalFormat df = Format.createScientificDecimalFormat(1, 2);
//        System.out.println(df.format(1200));
//        System.out.println(df.format(4027366862L));
//        System.out.println(df.format(-4027366862d));
//        System.out.println(df.format(-4027366862L));
//        System.out.println(df.format(0.00000000000004027366862d));
//        System.out.println(df.format(0.004027366862));

        System.out.println(Format.formatScientific(df,0.06));
//        System.out.println(Format.formatScientific(df,4027366862L));
//        System.out.println(Format.formatScientific(df,-4027366862d));
//        System.out.println(Format.formatScientific(df,-4027366862L));
//        System.out.println(Format.formatScientific(df,0.00000000000004027366862d));
//        System.out.println(Format.formatScientific(df,0.004027366862));

//        System.out.printf("%.2f%n", 0.00000001034634734f);
//        System.out.print(String.format("%100.2f%n", 1034634734f).length());
    }

    public static void main(String[] args) {
//        MathUtil.initFast();
//
//        testFft();

//        testNotation();
    }

}
