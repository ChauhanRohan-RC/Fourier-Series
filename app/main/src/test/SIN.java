package test;

import org.jetbrains.annotations.NotNull;

public class SIN {

    public static final float PI = 3.141592653589793f;
    public static final float MINUS_PI = -PI;
    public static final float HALF_PI = PI / 2;
    public static final float THREE_HALF_PI = PI + HALF_PI;
    public static final float TWO_PI = PI * 2;

    // Example values used in the tests:
    //   15 deg = 0.2617993878 rad
    //   105 deg = 1.8325957146 rad
    //   285 deg= 4.9741883682 rad
    public float valueFloatA = 0.2617993878f;
    public float valueFloatB = 1.8325957146f;
    public float valueFloatC = 4.9741883682f;
    public double valueDoubleA = 0.2617993878;
    public double valueDoubleB = 1.8325957146;
    public double valueDoubleC = 4.9741883682;
    ///////////////////////////////////////
    // Default sin
    ///////////////////////////////////////
    
    public double math_default_sin() {
        return Math.sin(valueDoubleA) + Math.sin(valueDoubleB) + Math.sin(valueDoubleC);
    }

    
    public double math_default_cos() {
        return Math.cos(valueDoubleA) + Math.cos(valueDoubleB) + Math.cos(valueDoubleC);
    }

    ///////////////////////////////////////
    // FastCosSin.java posted by kappa  ( http://www.java-gaming.org/topics/extremely-fast-atan2/36467/msg/346117/view.html#msg346117 )
    ///////////////////////////////////////

    public static final class Fast {

        private static final float CONST_1 = 4f / PI;
        private static final float CONST_2 = 4f / (PI * PI);

        public static float sin(float x) {
            if (x < MINUS_PI) {
                x += TWO_PI;
            } else if (x > PI) {
                x -= TWO_PI;
            }

            return (x < 0f) ? (CONST_1 * x + CONST_2 * x * x)
                    : (CONST_1 * x - CONST_2 * x * x);
        }

        public static float cos(float x) {
            if (x < MINUS_PI) {
                x += TWO_PI;
            } else if (x > PI) {
                x -= TWO_PI;
            }

            x += HALF_PI;

            if (x > PI) {
                x -= TWO_PI;
            }

            return (x < 0f) ? (CONST_1 * x + CONST_2 * x * x)
                    : (CONST_1 * x - CONST_2 * x * x);
        }
    }

    
    public double math_fast_sin() {
        return Fast.sin(valueFloatA) + Fast.sin(valueFloatB) + Fast.sin(valueFloatC);
    }

    
    public double math_fast_cos() {
        return Fast.cos(valueFloatA) + Fast.cos(valueFloatB) + Fast.cos(valueFloatC);
    }

    ///////////////////////////////////////
    // Devmaster's sine/cosine ( http://forum.devmaster.net/t/fast-and-accurate-sine-cosine/9648 )
    ///////////////////////////////////////

    public static final class Devmaster {
        
        public static final float B = 4 / PI;
        public static final float C = -4 / (PI * PI);
        public static final float P = 0.225f;

        public static final float sin(float x) {
            float x1 = x % PI;
            float x2 = x % TWO_PI;

            if (x > 0) {
                float y = x1 * (B + C * x1);
                y = (y > 0) ? (y = P * (y * y - y) + y)
                        : (y = P * (-y * y - y) + y);
                float xp = x2 - TWO_PI;
                if (!(xp < 0 && xp < -PI)) {
                    y = -y;
                }
                return y;
            } else {
                float y = x1 * (B - C * x1);
                y = (y > 0) ? (y = P * (y * y - y) + y)
                        : (y = P * (-y * y - y) + y);
                float xp = x2 + TWO_PI;
                if (xp > 0 && xp < PI) {
                    y = -y;
                }
                return y;
            }
        }

        public static final float cos(float x) {
            float x0 = x + HALF_PI;
            float x1 = x0 % PI;
            float x2 = x0 % TWO_PI;

            if (x0 > 0) {
                float y = x1 * (B + C * x1);
                y = (y > 0) ? (y = P * (y * y - y) + y)
                        : (y = P * (-y * y - y) + y);
                float xp = x2 - TWO_PI;
                if (!(xp < 0 && xp < -PI)) {
                    y = -y;
                }
                return y;
            } else {
                float y = x1 * (B - C * x1);
                y = (y > 0) ? (y = P * (y * y - y) + y)
                        : (y = P * (-y * y - y) + y);
                float xp = x2 + TWO_PI;
                if (xp > 0 && xp < PI) {
                    y = -y;
                }
                return y;
            }
        }
    }

    
    public double math_devmaster_sin() {
        return Devmaster.sin(valueFloatA) + Devmaster.sin(valueFloatB) + Devmaster.sin(valueFloatC);
    }

    
    public double math_devmaster_cos() {
        return Devmaster.cos(valueFloatA) + Devmaster.cos(valueFloatB) + Devmaster.cos(valueFloatC);
    }

    ///////////////////////////////////////
    // Riven's sine/cosine ( http://www.java-gaming.org/topics/fast-math-sin-cos-lookup-tables/24191/view.html )
    ///////////////////////////////////////

    public static final class Riven {

        private static final int SIN_BITS;
        private static final int SIN_MASK;
        private static final int SIN_COUNT;
        private static final float radToIndex;

        private static final float[] sin;
        private static final float[] cos;

        static {
            SIN_BITS = 14;
            SIN_MASK = ~(-1 << SIN_BITS);
            SIN_COUNT = SIN_MASK + 1;

            radToIndex = SIN_COUNT / TWO_PI;
            sin = new float[SIN_COUNT];
            cos = new float[SIN_COUNT];

            for (int i = 0; i < SIN_COUNT; i++) {
                sin[i] = (float) Math.sin((i + 0.5f) / SIN_COUNT * TWO_PI);
                cos[i] = (float) Math.cos((i + 0.5f) / SIN_COUNT * TWO_PI);
            }

            // Four cardinal directions (credits: Nate)
            sin[0] = 0;
            sin[radIndexInternal(HALF_PI)] = 1;
            sin[radIndexInternal(PI)] = 0;
            sin[radIndexInternal(THREE_HALF_PI)] = -1;
            sin[radIndexInternal(TWO_PI)] = 0;

            cos[0] = 1;
            cos[radIndexInternal(HALF_PI)] = 0;
            cos[radIndexInternal(PI)] = -1;
            cos[radIndexInternal(THREE_HALF_PI)] = 0;
            cos[radIndexInternal(TWO_PI)] = 1;
        }

//        private static float normalizeRad(float rad) {
//            rad %= TWO_PI;
//            if (rad < 0) {
//                rad += TWO_PI;
//            }
//
//            return rad;
//        }

        private static int radIndexInternal(float rad) {
            return (int) (rad * radToIndex) & SIN_MASK;
        }

//        private static int radIndexNormalize(float rad) {
//            return radIndexInternal(normalizeRad(rad));
//        }

        public static float sin(float rad) {
            return sin[(int) (rad * radToIndex) & SIN_MASK];
        }

        public static float cos(float rad) {
            return cos[(int) (rad * radToIndex) & SIN_MASK];
//            return cos[(int) (rad * radToIndex) & SIN_MASK];
        }

        public static void sincos(float rad, float @NotNull[] dest) {
            final int i = (int) (rad * radToIndex) & SIN_MASK;
            dest[0] = sin[i];
            dest[1] = cos[i];
        }
    }

    
    public double math_riven_sin() {
        return Riven.sin(valueFloatA) + Riven.sin(valueFloatB) + Riven.sin(valueFloatC);
    }

    
    public double math_riven_cos() {
        return Riven.cos(valueFloatA) + Riven.cos(valueFloatB) + Riven.cos(valueFloatC);
    }

    ///////////////////////////////////////
    // Icecore's sine/cosine ( http://www.java-gaming.org/topics/extremely-fast-sine-cosine/36469/msg/346190/view.html#msg346190 )
    ///////////////////////////////////////

    public static final class Icecore {

        private static final int Size_SC_Ac = 5000;
        private static final int Size_SC_Ar = Size_SC_Ac + 1;
        private static final float Sin[] = new float[Size_SC_Ar];
        private static final float Cos[] = new float[Size_SC_Ar];
        private static final float Pi = (float) Math.PI;
        private static final float Pi_D = Pi * 2;
        private static final float Pi_SC_D = Pi_D / Size_SC_Ac;

        static {
            for (int i = 0; i < Size_SC_Ar; i++) {
                double d = i * Pi_SC_D;
                Sin[i] = (float) Math.sin(d);
                Cos[i] = (float) Math.cos(d);
            }
        }

        public static final float sin(float r) {
            float rp = r % Pi_D;
            if (rp < 0) {
                rp += Pi_D;
            }
            return Sin[(int) (rp / Pi_SC_D)];
        }

        public static final float cos(float r) {
            float rp = r % Pi_D;
            if (rp < 0) {
                rp += Pi_D;
            }
            return Cos[(int) (rp / Pi_SC_D)];
        }
    }

    
    public double math_icecore_sin() {
        return Icecore.sin(valueFloatA) + Icecore.sin(valueFloatB) + Icecore.sin(valueFloatC);
    }

    
    public double math_icecore_cos() {
        return Icecore.cos(valueFloatA) + Icecore.cos(valueFloatB) + Icecore.cos(valueFloatC);
    }
}