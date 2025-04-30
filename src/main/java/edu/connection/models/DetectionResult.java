package edu.connection.models;

import javafx.geometry.Rectangle2D;

public class DetectionResult {
    private final String className;
    private final float confidence;
    private final Rectangle2D boundingBox;

    public DetectionResult(String className, float confidence, Rectangle2D boundingBox) {
        this.className = className;
        this.confidence = confidence;
        this.boundingBox = boundingBox;
    }

    public String getClassName() {
        return className;
    }

    public float getConfidence() {
        return confidence;
    }

    public Rectangle2D getBoundingBox() {
        return boundingBox;
    }

    @Override
    public String toString() {
        return String.format("%s (%.2f%%)", className, confidence * 100);
    }
}