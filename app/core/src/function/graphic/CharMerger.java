package function.graphic;

import function.definition.ColorHandler;
import function.definition.ColorProviderI;
import util.main.ComplexUtil;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.ListIterator;

public class CharMerger extends CharFunction implements ColorHandler {

    public static final Color COLOR_LINK = new Color(0, 0, 0, 0);


    @NotNull
    private final List<CharFunction> functions;

    private final double w, h;
    private final double dEnd;
    private final long msDef, msMin, msMax;

    private final double wgap = 3;

    public CharMerger(@NotNull List<CharFunction> functions, float zoom, boolean center) {
        super(zoom, center);
        this.functions = functions;

        double _w = 0, _h = 0, _dEnd = 0;
        long _msDef = 0, _msMin = 0, _msMax = 0;
        for (CharFunction f: functions) {
            _w += f.getWidth();
            _h = Math.max(_h, f.getHeight());
            _dEnd += f.getDomainEnd();
            _msDef += f.getDomainAnimationDurationMsDefault();
            _msMin += f.getDomainAnimationDurationMsMin();
            _msMax += f.getDomainAnimationDurationMsMax();
        }

        final int n_1 = Math.max(0, functions.size() - 1);
        w = _w + (wgap * n_1); h = _h; dEnd = _dEnd + functions.size();
        msDef = _msDef; msMin = _msMin; msMax = _msMax;
    }

    @Override
    public double getWidth() {
        return w;
    }

    @Override
    public double getHeight() {
        return h;
    }

    @Override
    public double getDomainEnd() {
        return dEnd;
    }

    @Override
    public long getDomainAnimationDurationMsDefault() {
        return msDef;
    }

    @Override
    public long getDomainAnimationDurationMsMin() {
        return msMin;
    }

    @Override
    public long getDomainAnimationDurationMsMax() {
        return msMax;
    }




    @Override
    public @Nullable Color getColor(double input) {
        double dend;

        for (CharFunction f: functions) {
            dend = f.getDomainEnd();

            if (input <= dend)
                return f.getColor(input);

            input -= dend;
            if (input <= 1)
                return COLOR_LINK;

            input -= 1;
        }

        return null;
    }



    @Override
    public CharMerger hueCycle(float hueStart, float hueEnd) {
        final float hueRange = hueEnd - hueStart;
        final float huePart = hueRange / functions.size();

        float hueStartPart = hueStart;
        for (CharFunction f: functions) {
//            if (excludeContinuityLinks && f.isContinuityLink())
//                continue;

            f.hueCycle(hueStartPart, hueStartPart + huePart);
            hueStartPart += huePart;
        }

        return this;
    }

    @NotNull
    @Override
    public CharMerger setColorProvider(@Nullable ColorProviderI colorProvider) {
        for (CharFunction f: functions) {
            f.setColorProvider(colorProvider);
        }

        return this;
    }

    @Override
    @Nullable
    public ColorProviderI getColorProvider() {
        ColorProviderI colorProvider;
        for (CharFunction func: functions) {
            colorProvider = func.getColorProvider();
            if (colorProvider != null) {
                return colorProvider;
            }
        }

        return null;
    }


    @Override
    public @NotNull Complex compute(double input) {
        if (input > dEnd) {
            input %= dEnd;
        }

        double w = 0;

        final ListIterator<CharFunction> itr = functions.listIterator();
        CharFunction cur, next;

        double dend;
        while (itr.hasNext()) {
            cur = itr.next();
            dend = cur.getDomainEnd();

            if (input <= dend)
                return applyTransform(cur.compute(input).add(w));

            input -= dend;
            if (input <= 1) {
                final double nextFuncWOffset;
                if (itr.hasNext()) {
                    next = itr.next();
                    nextFuncWOffset = w + cur.getWidth() + wgap;
                } else {
                    next = functions.get(0);        // first
                    nextFuncWOffset = 0;
                }

                final Complex lerp = ComplexUtil.lerp(cur.compute(dend).add(w), next.compute(next.getDomainStart()).add(nextFuncWOffset), (float) input);
                return applyTransform(lerp);
            }

            w += (cur.getWidth() + wgap);
            input -= 1;
        }

//        for (CharFunction f: functions) {
//            if (input <= f.getDomainEnd())
//                return applyTransform(f.compute(input).add(w));
//            w += (f.getWidth() + wgap);
//            input -= f.getDomainEnd();
//
//            if (input <= 1) {
//                return applyTransform()
//            }
//
//            input -= 1;
//        }

        throw new IllegalStateException("Input (" + input + ") out of domain (" + dEnd + ")");
    }
}
