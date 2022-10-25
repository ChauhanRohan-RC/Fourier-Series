package models;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public  class Pair<F, S> implements Serializable {

    public static final long serialVersionUID = 9958088698489767L;

    public F first;
    public S second;

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public Pair(@NotNull Pair<F, S> source) {
        first = source.first;
        second = source.second;
    }

    public static int toInt(@Nullable Boolean bool) {
        return (bool == null)? -1: bool? 1: 0;
    }

    @Nullable
    public static Boolean toBoolean(int booleanInt) {
        return switch (booleanInt) {
            case 0 -> false;
            case 1 -> true;
            default -> null;
        };
    }


    public static class Uno<T> extends Pair<T, T> implements Serializable {

        public static final long serialVersionUID = 995808869232392371L;

        public Uno(@NotNull Pair<T, T> source) {
            super(source);
        }

        public Uno(T first, T second) {
            super(first, second);
        }
    }


    public static class Bool extends Uno<Boolean> implements Serializable {
        public static final long serialVersionUID = 807766293612132L;

        public Bool(@NotNull Pair<Boolean, Boolean> source) {
            super(source);
        }

        public Bool(Boolean first, Boolean second) {
            super(first, second);
        }
    }


    public static class Byte extends Uno<java.lang.Byte> implements Serializable {
        public static final long serialVersionUID = 807766293612132L;

        public Byte(@NotNull Pair<java.lang.Byte, java.lang.Byte> source) {
            super(source);
        }

        public Byte(java.lang.Byte first, java.lang.Byte second) {
            super(first, second);
        }
    }

    public static class Int extends Uno<Integer> implements Serializable {
        public static final long serialVersionUID = 8077662936121321L;

        public Int(@NotNull Pair<Integer, Integer> source) {
            super(source);
        }

        public Int(Integer first, Integer second) {
            super(first, second);
        }
    }

    public static class Long extends Uno<java.lang.Long> implements Serializable {
        public static final long serialVersionUID = 8077662936121322L;

        public Long(@NotNull Pair<java.lang.Long, java.lang.Long> source) {
            super(source);
        }

        public Long(java.lang.Long first, java.lang.Long second) {
            super(first, second);
        }
    }

    public static class Float extends Uno<java.lang.Float> implements Serializable {
        public static final long serialVersionUID = 8077662936121323L;

        public Float(@NotNull Pair<java.lang.Float, java.lang.Float> source) {
            super(source);
        }

        public Float(java.lang.Float first, java.lang.Float second) {
            super(first, second);
        }
    }

    public static class Double extends Uno<java.lang.Double> implements Serializable {
        public static final long serialVersionUID = 8077662936121324L;

        public Double(@NotNull Pair<java.lang.Double, java.lang.Double> source) {
            super(source);
        }

        public Double(java.lang.Double first, java.lang.Double second) {
            super(first, second);
        }
    }

    public static class String extends Uno<java.lang.String> implements Serializable {
        public static final long serialVersionUID = 8077662936121325L;

        public String(@NotNull Pair<java.lang.String, java.lang.String> source) {
            super(source);
        }

        public String(java.lang.String first, java.lang.String second) {
            super(first, second);
        }
    }
}
