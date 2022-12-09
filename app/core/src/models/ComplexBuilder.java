package models;

import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;

public class ComplexBuilder {

    private double real;
    private double img;

    public ComplexBuilder(double real, double imaginary) {
        this.real = real;
        img = imaginary;
    }

    public ComplexBuilder(@NotNull Complex complex) {
        this(complex.getReal(), complex.getImaginary());
    }

    public ComplexBuilder() {
    }

    public double getReal() {
        return real;
    }

    public double getImaginary() {
        return img;
    }

    public ComplexBuilder add(@NotNull Complex c, double realMultiplier, double imaginaryMultiplier) {
        real += (c.getReal() * realMultiplier);
        img += (c.getImaginary() * imaginaryMultiplier);
        return this;
    }

    public ComplexBuilder add(@NotNull Complex c, double multiplier) {
        return add(c, multiplier, multiplier);
    }

    public ComplexBuilder add(@NotNull Complex c) {
        real += c.getReal();
        img += c.getImaginary();
        return this;
    }

    public ComplexBuilder addReal(double real) {
        this.real += real;
        return this;
    }

    public ComplexBuilder addImaginary(double imaginary) {
        img += imaginary;
        return this;
    }

    public ComplexBuilder addComponent(@NotNull Complex c, boolean real) {
        return real? addReal(c.getReal()): addImaginary(c.getImaginary());
    }

    public ComplexBuilder mult(double a) {
        real *= a;
        img *= a;
        return this;
    }

    public ComplexBuilder div(double a) {
        real /= a;
        img /= a;
        return this;
    }

    public ComplexBuilder mult(@NotNull Complex factor) {
        final double fr = factor.getReal();
        final double fi = factor.getImaginary();

        real = (real * fr) - (img * fi);
        img = (real * fi) + (img * fr);
        return this;
    }


    @NotNull
    public Complex toComplex() {
        return new Complex(real, img);
    }
}
