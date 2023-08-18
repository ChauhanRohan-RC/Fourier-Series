package function.path;

import function.definition.ColorHandler;
import function.definition.ColorProviderI;
import function.definition.DomainAnimationDurationScalerI;
import function.graphic.GraphicFunction;
import org.apache.batik.parser.ParseException;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class PathFunctionMerger extends GraphicFunction implements ColorHandler, DomainAnimationDurationScalerI {

    public static final String TAG = "PathFunctionIMerger";

    public static final ColorProviderI COLOR_PROVIDER_CONTINUITY_LINK = ColorProviderI.TRANSPARENT;

    @NotNull
    private final Rectangle2D bounds;
    @NotNull
    private final PathFunctionI[] segments;

    private final long animDurationDefault, animDurationMin, animDurationMax;
    private float animDurationScale = 1;

    public PathFunctionMerger(@NotNull PathFunctionI[] segments, @NotNull Rectangle2D bounds, float zoom, boolean center) throws IllegalArgumentException {
        super(zoom, center);
        if (segments == null || segments.length == 0)
            throw new IllegalArgumentException("No PathFunctionIs provided to PathFunctionIMerger!!");

        this.bounds = bounds;
        this.segments = segments;

        long _msDef = 0, _msMin = 0, _msMax = 0;
        for (PathFunctionI f: this.segments) {
            _msDef += f.getDomainAnimationDurationMsDefault();
            _msMin += f.getDomainAnimationDurationMsMin();
            _msMax += f.getDomainAnimationDurationMsMax();
        }

        animDurationDefault = _msDef; animDurationMin = _msMin; animDurationMax = _msMax;
    }


    public final int getSegmentsCount() {
        return segments.length;
    }

    @NotNull
    public final PathFunctionI getSegment(int index) {
        return segments[index];
    }


    public final int getContinuityLinksCount() {
        int count = 0;
        for (PathFunctionI f: segments) {
            if (f.isContinuityLink())
                count++;
        }

        return count;
    }

    public final int getCountExceptContinuityLinks() {
        return getSegmentsCount() - getContinuityLinksCount();
    }

    public PathFunctionMerger hueCycle(boolean excludeContinuityLinks, float hueStart, float hueEnd) {
        final float hueRange = hueEnd - hueStart;
        final float huePart = hueRange / (excludeContinuityLinks? getCountExceptContinuityLinks(): getSegmentsCount());

        float hueStartPart = hueStart;
        for (PathFunctionI f: segments) {
            if (excludeContinuityLinks && f.isContinuityLink())
                continue;

            f.setColorProvider(new HueCycle(f, hueStartPart, hueStartPart + huePart));
            hueStartPart += huePart;
        }

        return this;
    }

    @Override
    public PathFunctionMerger hueCycle(float hueStart, float hueEnd) {
        return hueCycle(true, hueStart, hueEnd);
    }

    public PathFunctionMerger hueCycle(boolean excludeContinuityLinks) {
        return hueCycle(excludeContinuityLinks, 0, 1);
    }

    @NotNull
    public PathFunctionMerger setColorProvider(@Nullable ColorProviderI colorProvider, boolean excludeContinuityLinks) {
        for (PathFunctionI f: segments) {
            if (excludeContinuityLinks && f.isContinuityLink())
                continue;

            f.setColorProvider(colorProvider);
        }

        return this;
    }

    @NotNull
    public PathFunctionMerger setColorProvider(@Nullable ColorProviderI colorProvider) {
        return setColorProvider(colorProvider, true);
    }

    @Override
    public @Nullable ColorProviderI getColorProvider() {
        ColorProviderI colorProvider;
        for (PathFunctionI segment: segments) {
            if (segment.isContinuityLink())
                continue;

            colorProvider = segment.getColorProvider();
            if (colorProvider != null) {
                return colorProvider;
            }
        }

        return null;
    }


    @Override
    @NotNull
    public final Rectangle2D getBounds() {
        return bounds;
    }

    @Override
    public final double getDomainStart() {
        return 0;
    }

    @Override
    public final double getDomainEnd() {
        return segments.length;
    }

    @Override
    public final float getDomainAnimationDurationScale() {
        return animDurationScale;
    }

    @Override
    public final PathFunctionMerger setDomainAnimationDurationScale(float animationDurationScale) {
        if (animationDurationScale > 0) {
            this.animDurationScale = animationDurationScale;
        }

        return this;
    }


    @Override
    public final long getDomainAnimationDurationMsDefault() {
        return (long) (animDurationDefault * animDurationScale);
    }

    @Override
    public final long getDomainAnimationDurationMsMin() {
        return (long) (animDurationMin * animDurationScale);
    }

    @Override
    public final long getDomainAnimationDurationMsMax() {
        return (long) (animDurationMax * animDurationScale);
    }

//    @Override
//    protected @NotNull Complex applyTransform(@NotNull Complex o) {
//        final Complex c = super.applyTransform(o);
//        return new Complex(c.getReal(), -c.getImaginary());     // Invert imaginary
//    }

    @Override
    @NotNull
    public final Complex compute(double d) {
        if (d > segments.length) {
            d %= segments.length;
        }

        int i = (int) d;
        if (i >= segments.length) {
            i = segments.length - 1;           // last
        }

        return applyTransform(segments[i].compute(d - i));
    }

    @Override
    public @Nullable Color getColor(double d) {
        if (d > segments.length) {
            d %= segments.length;
        }

        int i = (int) d;
        if (i >= segments.length) {
            i = segments.length - 1;           // last
        }

        return segments[i].getColor(d - i);
    }


    @NotNull
    public List<? extends List<Point2D>> interpolatePoints(int segmentPointCount, boolean interpolateLines, boolean excludeContinuityLinks) {
        final List<List<Point2D>> result = new ArrayList<>(segments.length + 2);

        for (PathFunctionI path: segments) {
            if (excludeContinuityLinks && path.isContinuityLink())
                continue;

            result.add(path.interpolatePoints(segmentPointCount, interpolateLines));
        }

        return result;
    }

    @NotNull
    public List<? extends List<Point2D>> interpolatePoints() {
        return interpolatePoints(PathFunctionI.DEFAULT_INTERPOLATE_POINT_COUNT, PathFunctionI.DEFAULT_INTERPOLATE_LINE, true);
    }



    @NotNull
    public static PathFunctionI createContinuityLink(@NotNull Point2D start, @NotNull Point2D end) {
        return new LinePath(start, end)
                .setIsContinuityLink(true)
                .setColorProvider(COLOR_PROVIDER_CONTINUITY_LINK);
    }

    @NotNull
    public static List<PathFunctionI> parse(@NotNull PathIterator itr) {
        final List<PathFunctionI> segments = new ArrayList<>();
        PathFunctionI last = null;

        boolean firstMoveTo = true;
        Point2D firstMovePoint = null;

        Point2D startPoint = new Point2D.Double(0, 0);
        final double[] coords = new double[6];

        for (; !itr.isDone(); itr.next()) {
            switch (itr.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO -> {
                    startPoint = new Point2D.Double(coords[0], coords[1]);
                    if (firstMoveTo) {
                        firstMovePoint = startPoint;
                        firstMoveTo = false;
                    }

                    if (!(last == null || last.endPoint().equals(startPoint))) {
                        last = createContinuityLink(last.endPoint(), startPoint);     // for continuity
                        segments.add(last);
                    }

                    last = null;
                }
                case PathIterator.SEG_LINETO -> {
                    last = new LinePath(last != null? last.endPoint(): startPoint, new Point2D.Double(coords[0], coords[1]));
                    segments.add(last);
                }
                case PathIterator.SEG_QUADTO -> {
                    last = new QuadCurvePath(last != null ? last.endPoint() : startPoint, new Point2D.Double(coords[0], coords[1]), new Point2D.Double(coords[2], coords[3]));
                    segments.add(last);
                }
                case PathIterator.SEG_CUBICTO -> {
                    last = new CubicCurvePath(last != null ? last.endPoint() : startPoint, new Point2D.Double(coords[0], coords[1]), new Point2D.Double(coords[2], coords[3]), new Point2D.Double(coords[4], coords[5]));
                    segments.add(last);
                }
                case PathIterator.SEG_CLOSE -> {
                    if (last != null) {
                        last = new LinePath(last.endPoint(), startPoint);
                        segments.add(last);
                    }

//                    last = null;
                }
            }
        }


        // Close last end and first start
        if (!(firstMovePoint == null || last == null || last.endPoint().equals(firstMovePoint))) {
            last = createContinuityLink(last.endPoint(), firstMovePoint);     // for continuity
            segments.add(last);
        }

        return segments;
    }

    @NotNull
    public static PathFunctionMerger create(@NotNull PathIterator itr, @NotNull Rectangle2D bounds, float zoom, boolean center) throws ParseException {
        List<PathFunctionI> segments = parse(itr);
        if (segments.isEmpty())
            throw new ParseException(new Exception("Path is empty!!"));

        return new PathFunctionMerger(segments.toArray(new PathFunctionI[0]), bounds, zoom, center);
    }

    @NotNull
    public static PathFunctionMerger create(@NotNull Shape shape, float zoom, boolean center) throws ParseException {
        return create(shape.getPathIterator(null), shape.getBounds(), zoom, center);
    }

    @NotNull
    public static PathFunctionMerger create(@NotNull Shape shape) throws ParseException {
        return create(shape, 1, true);
    }

}
