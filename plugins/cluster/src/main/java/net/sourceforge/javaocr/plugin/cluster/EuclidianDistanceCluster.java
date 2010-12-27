package net.sourceforge.javaocr.plugin.cluster;

/**
 * cluster calculating euclidian distance
 * @author Konstantin Pribluda
 */
public class EuclidianDistanceCluster extends NormalDistributionCluster {
    /**
     * create cluster calculating euclidian distance between center and feature
     * vector
     *
     * @param c    assotiated character
     * @param size size of feature cluster
     */
    public EuclidianDistanceCluster(int dimensions) {
        super(dimensions);
    }

    public double distance(double[] features) {
        double cumulated = 0;
        for(int i = 0; i < getDimensions(); i++) {
            cumulated += Math.pow(getMx()[i] - features[i],2);
        }
        return Math.sqrt(cumulated);
    }
}
