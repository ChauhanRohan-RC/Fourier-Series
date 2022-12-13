package test;

import app.R;
import function.definition.ComplexDomainFunctionI;
import function.internal.basic.SineSignal;
import misc.MathUtil;
import util.main.ComplexUtil;


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
            System.out.printf("Time taken: %.2f ms (%d ns)%n", delta / 1E6, delta);
        }
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


        final int N = MathUtil.highestPowOf2(50000);

        final double freq = 2;
        System.out.printf("%n%n At Fq = %.2f -> %f, %f", freq, ComplexUtil.FftTest.fft(func, freq, N).abs(), ComplexUtil.fourierTransform(func, freq, N).abs());
    }

    public static void main(String[] args) {
        MathUtil.initFast();

        testFft();
    }

}
