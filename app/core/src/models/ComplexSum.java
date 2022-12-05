package models;

import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;

public class ComplexSum {

    private double re;
    private double im;

    public ComplexSum(double real, double imaginary) {
        re = real;
        im = imaginary;
    }

    public ComplexSum() {
    }

    public double getReal() {
        return re;
    }

    public double getImaginary() {
        return im;
    }

    public ComplexSum add(@NotNull Complex c, double realMultiplier, double imaginaryMultiplier) {
        re += (c.getReal() * realMultiplier);
        im += (c.getImaginary() * imaginaryMultiplier);
        return this;
    }

    public ComplexSum add(@NotNull Complex c, double multiplier) {
        return add(c, multiplier, multiplier);
    }

    public ComplexSum add(@NotNull Complex c) {
        re += c.getReal();
        im += c.getImaginary();
        return this;
    }

    public ComplexSum addReal(double real) {
        re += real;
        return this;
    }

    public ComplexSum addImaginary(double imaginary) {
        im += imaginary;
        return this;
    }

    public ComplexSum addComponent(@NotNull Complex c, boolean real) {
        return real? addReal(c.getReal()): addImaginary(c.getImaginary());
    }

    public ComplexSum mult(double a) {
        re *= a;
        im *= a;
        return this;
    }

    public ComplexSum div(double a) {
        re /= a;
        im /= a;

        return this;
    }


    @NotNull
    public Complex toComplex() {
        return new Complex(re, im);
    }
}
