package util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Log {

    public static final String TAG = "LOG";
    public static final boolean DEBUG = true;

    @NotNull
    private static String createMsg(String tag, Object msg) {
        if (tag == null) {
            tag = TAG;
        }

        return tag + ": " + msg;
    }

    public static void println(Object o) {
        System.out.println(o);
        System.out.flush();
    }

    public static void printErr(Object o) {
        System.err.println(o);
        System.err.flush();
    }

    public static void printTrace(@Nullable Throwable t) {
        if (t != null) {
            t.printStackTrace(System.err);
            System.err.flush();
        }
    }


    public static void v(String tag, Object msg) {
        println(createMsg(tag, msg));
    }

    public static void v(Object msg) {
        v(null, msg);
    }

    public static void d(String tag, Object msg, Throwable t) {
        if (DEBUG) {
            println(createMsg(tag, msg));
            printTrace(t);
        }
    }

    public static void d(String tag, Object msg) {
        d(tag, msg, null);
    }

    public static void d(Object msg) {
        d(null, msg);
    }


    public static void e(String tag, Object msg, Throwable t) {
        printErr(createMsg(tag, msg));
        printTrace(t);
    }

    public static void e(String tag, Object msg) {
        e(tag, msg, null);
    }

    public static void e(Object msg) {
        e(null, msg);
    }


}
