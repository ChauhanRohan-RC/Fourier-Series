package function.definition;

import misc.MathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;


public interface ColorProviderI {

    Color COLOR_TRANSPARENT = new Color(0, 0, 0, 0);

    Static TRANSPARENT = staticColor(COLOR_TRANSPARENT);

    @NotNull
    static Static staticColor(@Nullable Color color) {
        return new Static(color);
    }

    class Static implements ColorProviderI {

        @Nullable
        private Color color;

        public Static(@Nullable Color color) {
            this.color = color;
        }

        public void setColor(@Nullable Color color) {
            this.color = color;
        }

        public @Nullable Color getColor() {
            return color;
        }

        @Override
        public @Nullable Color getColor(double input) {
            return color;
        }

    }

    class HueCycle implements ColorProviderI {

        private final double domainStart, domainEnd;
        private final float hueStart, hueEnd;

        public HueCycle(double domainStart, double domainEnd, float hueStart, float hueEnd) {
            this.domainStart = domainStart;
            this.domainEnd = domainEnd;
            this.hueStart = hueStart;
            this.hueEnd = hueEnd;
        }

        public HueCycle(@NotNull DomainProviderI domainProvider, float hueStart, float hueEnd) {
            this(domainProvider.getDomainStart(), domainProvider.getDomainEnd(), hueStart, hueEnd);
        }

        public HueCycle(double domainStart, double domainEnd) {
            this(domainStart, domainEnd, 0, 1);
        }

        public HueCycle(@NotNull DomainProviderI domainProvider) {
            this(domainProvider, 0, 1);
        }

        @Override
        public @Nullable Color getColor(double input) {
            return Color.getHSBColor((float) MathUtil.map(input, domainStart, domainEnd, hueStart, hueEnd), 1, 1);
        }
    }



    @Nullable
    Color getColor(double input);

}
