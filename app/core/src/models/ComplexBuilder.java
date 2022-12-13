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

    @NotNull
    public ComplexBuilder buildUpon() {
        return new ComplexBuilder(real, img);
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

    public ComplexBuilder add(@NotNull ComplexBuilder c, double realMultiplier, double imaginaryMultiplier) {
        real += (c.getReal() * realMultiplier);
        img += (c.getImaginary() * imaginaryMultiplier);
        return this;
    }

    public ComplexBuilder add(@NotNull Complex c, double multiplier) {
        return add(c, multiplier, multiplier);
    }

    public ComplexBuilder add(@NotNull ComplexBuilder c, double multiplier) {
        return add(c, multiplier, multiplier);
    }


    public ComplexBuilder add(@NotNull Complex c) {
        real += c.getReal();
        img += c.getImaginary();
        return this;
    }

    public ComplexBuilder add(@NotNull ComplexBuilder c) {
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




    public ComplexBuilder subtract(@NotNull Complex c, double realMultiplier, double imaginaryMultiplier) {
        real -= (c.getReal() * realMultiplier);
        img -= (c.getImaginary() * imaginaryMultiplier);
        return this;
    }

    public ComplexBuilder subtract(@NotNull ComplexBuilder c, double realMultiplier, double imaginaryMultiplier) {
        real -= (c.getReal() * realMultiplier);
        img -= (c.getImaginary() * imaginaryMultiplier);
        return this;
    }

    public ComplexBuilder subtract(@NotNull Complex c, double multiplier) {
        return subtract(c, multiplier, multiplier);
    }

    public ComplexBuilder subtract(@NotNull ComplexBuilder c, double multiplier) {
        return subtract(c, multiplier, multiplier);
    }


    public ComplexBuilder subtract(@NotNull Complex c) {
        real -= c.getReal();
        img -= c.getImaginary();
        return this;
    }

    public ComplexBuilder subtract(@NotNull ComplexBuilder c) {
        real -= c.getReal();
        img -= c.getImaginary();
        return this;
    }

    public ComplexBuilder subtractReal(double real) {
        this.real -= real;
        return this;
    }

    public ComplexBuilder subtractImaginary(double imaginary) {
        img -= imaginary;
        return this;
    }

    public ComplexBuilder subtractComponent(@NotNull Complex c, boolean real) {
        return real? subtractReal(c.getReal()): subtractImaginary(c.getImaginary());
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

    public ComplexBuilder mult(double _real, double _img) {
        final double newReal = (real * _real) - (img * _img);
        final double newImg = (real * _img) + (img * _real);

        this.real = newReal;
        this.img = newImg;
        return this;
    }

    public ComplexBuilder mult(@NotNull Complex factor) {
        return mult(factor.getReal(), factor.getImaginary());
    }

    public ComplexBuilder mult(@NotNull ComplexBuilder factor) {
        return mult(factor.getReal(), factor.getImaginary());
    }


    @NotNull
    public Complex toComplex() {
        return new Complex(real, img);
    }
}
