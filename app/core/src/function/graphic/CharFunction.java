package function.graphic;

import function.definition.ColorHandler;
import function.definition.ColorProviderI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public abstract class CharFunction extends GraphicFunction implements ColorHandler {

    @Nullable
    private Rectangle2D.Double mBounds;

    @Nullable
    private ColorProviderI colorProvider;

    protected CharFunction(float zoom, boolean center) {
        super(zoom, center);
    }

    protected CharFunction() {
        this(1, false);
    }

    public abstract double getWidth();

    public abstract double getHeight();

    @Override
    @NotNull
    public final Rectangle2D getBounds() {
        if (mBounds == null) {
            mBounds = new Rectangle2D.Double(0, 0, getWidth(), getHeight());
        }

        return mBounds;
    }

    @Override
    public final double getDomainStart() {
        return 0;       // domain always positive
    }


    @Nullable
    @Override
    public ColorProviderI getColorProvider() {
        return colorProvider;
    }

    @Override
    public CharFunction setColorProvider(@Nullable ColorProviderI colorProvider) {
        this.colorProvider = colorProvider;
        return this;
    }

    @Override
    public ColorHandler hueCycle(float hueStart, float hueEnd) {
        return setColorProvider(new HueCycle(this, hueStart, hueEnd));
    }



    @Override
    public @Nullable Color getColor(double input) {
        return colorProvider != null? colorProvider.getColor(input): null;
    }

    @Override
    public long getDomainAnimationDurationMsDefault() {
        return 5000;
    }

    @Override
    public long getDomainAnimationDurationMsMin() {
        return 1000;
    }

    @Override
    public long getDomainAnimationDurationMsMax() {
        return 20000;
    }
}
