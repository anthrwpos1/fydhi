public class Complex implements Fractional {
    public double re, im;
    public static final Complex i = new Complex(0, 1);

    public Complex(double re, double im) {
        this.re = re;
        this.im = im;
    }

    @Override
    public Complex add(Fractional c) {
        Complex cc = (Complex) c;
        return new Complex(re + cc.re, im + cc.im);
    }

    public Complex add(double d) {
        return new Complex(re + d, im);
    }

    @Override
    public Complex sub(Fractional c) {
        Complex cc = (Complex) c;
        return new Complex(re - cc.re, im - cc.im);
    }

    public Complex sub(double d) {
        return new Complex(re - d, im);
    }

    public Complex mul(Complex c) {
        return new Complex(re * c.re - im * c.im, re * c.im + im * c.re);
    }

    public Complex mul(double d) {
        return new Complex(re * d, im * d);
    }

    public Complex conj() {
        return new Complex(re, -im);
    }


    public double abs() {
        return Math.sqrt(re * re + im * im);
    }

    public double abs2() {
        return re * re + im * im;
    }

    public double angle(){
        return Math.atan2(im,re);
    }

    public Complex clone() {
        return new Complex(re, im);
    }

    public Complex div(Complex c) {
        return mul(c.conj()).div(c.abs2());
    }

    public Complex div(double d) {
        return new Complex(re / d, im / d);
    }

    public String valueOf() {
        if (im < 0) return String.format("%f-i%f", re, -im);
        return String.format("%f+i%f", re, im);
    }

    public Complex exp() {
        return new Complex(Math.cos(im),Math.sin(im)).mul(Math.exp(re));
    }

    public Complex log(){
        return new Complex(Math.log(abs()),angle());
    }

    public Complex pow (Complex b){
        return log().mul(b).exp();
    }

    public Complex pow (double b){
        return log().mul(b).exp();
    }
}