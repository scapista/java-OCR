package net.sourceforge.javaocr.filter;

/**
 * normalise grayscale pixels linear basing on min/max values
 *
 * @author Konstantin Pribluda
 */
public class NormaliseGrayscaleFilter extends AbstractSinglePixelFilter {
    int min;
    int max;
    int range;

    /**
     * @param max max pixel value in image
     * @param min min pixel value in image
     */
    public NormaliseGrayscaleFilter(int max, int min) {
        this.max = max;
        this.min = min;
        range = max - min;
    }

    /**
     * @param pixel pixel value to be normalized
     * @return
     */
    protected int convert(int pixel) {
        return Math.min(255,Math.max(0,((pixel- min) * 255) / range));
    }
}