package util.main;

import misc.CollectionUtil;
import org.apache.batik.parser.AWTPathProducer;
import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PathParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;


public class PathUtil {

    private PathUtil() {
    }


    /**
     * Bezier Linear interpolation
     *
     * <b>
     *   p(i) = p0 + i * (p1 - p0)
     * </b>
     *
     * @param p0 start point
     * @param p1 end point
     * @param i interpolation factor, in range [0, 1]
     *
     * @return linear interpolation
     * */
    public static double interpolateLinear(double p0, double p1, float i) {
        return ((1 - i) * p0) + (i * p1);
    }

    /**
     * {@inheritDoc}
     *
     * @see #interpolateLinear(double, double, float)
     * */
    @NotNull
    public static Point2D interpolateLinear(@NotNull Point2D p0, @NotNull Point2D p1, float i) {
        final double i_ = 1 - i;
        return new Point2D.Double((i_ * p0.getX()) + (i * p1.getX()), (i_ * p0.getY()) + (i * p1.getY()));
    }


    /**
    * Bezier Quadratic interpolation
     *
     * if linear(p0, p1, i) means linear interpolation then
     *
     * <b>
     *     quad(i) = linear(linear(p0, p1, i), linear(p1, p2, i), i)
     * </b>
     *
     * where p0 and p2 are end points and p1 is control point
     *
     * @param p0 start point
     * @param p1 control point (may not lie on curve)
     * @param p2 end point
     * @param i interpolation factor, in range [0, 1]
     *
     * @return quadratic interpolation
     *
     * @see #interpolateLinear(double, double, float)
    * */
    public static double interpolateQuad(double p0, double p1, double p2, float i) {
        final double i_ = 1 - i;
        return (i_ * i_ * p0) + (2 * i_ * i * p1) + (i * i * p2);
    }

    /**
     * {@inheritDoc}
     *
     * @see #interpolateQuad(double, double, double, float)
     * */
    @NotNull
    public static Point2D interpolateQuad(@NotNull Point2D p0, @NotNull Point2D p1, @NotNull Point2D p2, float i) {
        final double i_ = 1 - i;
        final double i_2 = i_ * i_;
        final double i_i = 2 * i_ * i;
        final double i2 = i * i;

        return new Point2D.Double((i_2 * p0.getX()) + (i_i * p1.getX()) + (i2 * p2.getX()), (i_2 * p0.getY()) + (i_i * p1.getY()) + (i2 * p2.getY()));
    }

    /**
     * Bezier Cubic interpolation
     *
     * Like quadratic interpolation, if linear(p0, p1, i) means linear interpolation then
     *
     * <b>
     *     cubic(i) = linear(linear(linear(p0, p1, i), linear(p1, p2, i), i), linear(linear(p1, p2, i), linear(p2, p3, i), 1), i)
     * </b>
     *
     * where p0 and p3 are end points and p1 and p2 are control points
     *
     * @param p0 start point
     * @param p1 control point 1 (may not lie on curve)
     * @param p2 control point 2 (may not lie on curve)
     * @param p3 end point
     * @param i interpolation factor, in range [0, 1]
     *
     * @return cubic interpolation
     *
     * @see #interpolateLinear(double, double, float)
     * @see #interpolateQuad(double, double, double, float)
     * */
    public static double interpolateCubic(double p0, double p1, double p2, double p3, float i) {
        final double i_ = 1 - i;
        final double i_2 = i_ * i_;
        final double i2 = i * i;
        return (i_ * i_2 * p0) + (3 * i_2 * i * p1) + (3 * i_ * i2 * p2) + (i * i2 * p3);
    }

    /**
     * {@inheritDoc}
     *
     * @see #interpolateCubic(double, double, double, double, float)
     * */
    @NotNull
    public static Point2D interpolateCubic(@NotNull Point2D p0, @NotNull Point2D p1, @NotNull Point2D p2, @NotNull Point2D p3, float i) {
        final double i_ = 1 - i;
        final double i_2 = i_ * i_;
        final double i_3 = i_2 * i_;
        final double i2 = i * i;
        final double i3 = i2 * i;

        final double i_2_i = 3 * i_2 * i;
        final double i_i2 = 3 * i_ * i2;

        return new Point2D.Double((i_3 * p0.getX()) + (i_2_i * p1.getX()) + (i_i2 * p2.getX()) + (i3 * p3.getX()), (i_3 * p0.getY()) + (i_2_i * p1.getY()) + (i_i2 * p2.getY()) + (i3 * p3.getY()));
    }


    /* Parsing of SVG paths to Shapes */

    @NotNull
    public static Shape parsePathDataReader(@NotNull Reader reader) throws ParseException {
        final PathParser p = new PathParser();
        final AWTPathProducer ph = new AWTPathProducer();
        ph.setWindingRule(Path2D.WIND_NON_ZERO);

        p.setPathHandler(ph);
        p.parse(reader);

        final Shape shape = ph.getShape();
        if (shape == null) {
            throw new ParseException(new Exception("Path data is empty!"));
        }

        return shape;
    }

    @NotNull
    public static Shape parsePathDataFIle(@NotNull String file) throws ParseException, FileNotFoundException {
        return parsePathDataReader(new FileReader(file));
    }

    @NotNull
    public static Shape parsePathDataString(@NotNull String pathData) throws ParseException {
        return parsePathDataReader(new StringReader(pathData));
    }


    @NotNull
    public static Shape parsePathDataStrings(String[] paths) throws ParseException {
        if (paths == null || paths.length == 0)
            throw new ParseException(new Exception("Empty path data!"));

        final Shape first = parsePathDataString(paths[0]);
        final Shape shape;

        if (paths.length > 1) {
            final Area area = new Area(first);

            for (int i=1; i < paths.length; i++) {
                area.add(new Area(parsePathDataString(paths[i])));
            }

            shape = area;
        } else {
            shape = first;
        }

        return shape;
    }



    @Nullable
    public static GeneralPath createPath(List<Point2D> points, boolean close) {
        if (CollectionUtil.isEmpty(points))
            return null;

        final ListIterator<Point2D> itr = points.listIterator();
        final GeneralPath path = new GeneralPath();
        if (itr.hasNext()) {
            final Point2D start = itr.next();
            path.moveTo(start.getX(), start.getY());

            while (itr.hasNext()) {
                final Point2D p = itr.next();
                path.lineTo(p.getX(), p.getY());
            }

            if (close) {
                path.closePath();
            }
        }

        return path;
    }

    @NotNull
    public static GeneralPath mergePaths(Collection<Path2D> paths, boolean connect) {
        final GeneralPath result = new GeneralPath();
        if (CollectionUtil.notEmpty(paths)) {
            for (Path2D path: paths) {
                result.append(path, connect);
            }
        }

        return result;
    }
}
