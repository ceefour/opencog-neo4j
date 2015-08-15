package org.opencog.atomspace;

import java.io.Serializable;

/**
 * Created by ceefour on 8/15/15.
 */
public class TruthValue implements Serializable {

    double fuzzyStrength;
    double confidence;
    double count;

    public TruthValue() {
    }

    public TruthValue(double fuzzyStrength, double confidence, double count) {
        this.fuzzyStrength = fuzzyStrength;
        this.confidence = confidence;
        this.count = count;
    }

    public double getFuzzyStrength() {
        return fuzzyStrength;
    }

    public void setFuzzyStrength(double fuzzyStrength) {
        this.fuzzyStrength = fuzzyStrength;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }

    public double[] toArray() {
        return new double[] { fuzzyStrength, confidence, count };
    }

    @Override
    public String toString() {
        return "(stv " + fuzzyStrength + " " + confidence + " " + count + ")";
    }
}
