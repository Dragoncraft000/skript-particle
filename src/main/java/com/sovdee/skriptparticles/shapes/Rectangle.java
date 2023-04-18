package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.DynamicLocation;
import com.sovdee.skriptparticles.util.Quaternion;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;


/*
 * Please don't judge me based off this class
 * I know it's a mess, but i just wanted to get it working
 */
public class Rectangle extends AbstractShape implements LWHShape {

    // axes
    public static final int XZ = 0;
    public static final int XY = 1;
    public static final int YZ = 2;
    private int axis = XZ;

    private double halfLength;
    private double halfWidth;
    private double lengthStep = 1.0;
    private double widthStep = 1.0;
    private Vector centerOffset = new Vector(0, 0, 0);
    private DynamicLocation negativeCorner;
    private DynamicLocation positiveCorner;
    private boolean isDynamic = false;

    public Rectangle(double length, double width, int axis){
        super();
        this.axis = axis;
        this.halfWidth = width / 2;
        this.halfLength = length / 2;
        calculateSteps();
    }

    public Rectangle(Vector negativeCorner, Vector positiveCorner, int axis){
        super();
        this.axis = axis;
        setLengthWidth(negativeCorner, positiveCorner);
        centerOffset = positiveCorner.clone().add(negativeCorner).multiply(0.5);
        switch (axis) {
            case XZ -> centerOffset.setY(0);
            case XY -> centerOffset.setZ(0);
            case YZ -> centerOffset.setX(0);
        }
        calculateSteps();
    }

    public Rectangle(DynamicLocation negativeCorner, DynamicLocation positiveCorner, int axis){
        super();
        this.axis = axis;
        Location negative = negativeCorner.getLocation();
        Location positive = positiveCorner.getLocation();
        if (negativeCorner.isDynamic() || positiveCorner.isDynamic()) {
            this.negativeCorner = negativeCorner.clone();
            this.positiveCorner = positiveCorner.clone();
            isDynamic = true;
        } else {
            setLengthWidth(negative, positive);
        }
        // get center of rectangle
        Vector offset = positive.toVector().subtract(negative.toVector()).multiply(0.5);
        switch (axis) {
            case XZ -> offset.setY(0);
            case XY -> offset.setZ(0);
            case YZ -> offset.setX(0);
        };
        location = new DynamicLocation(negative.clone().add(offset));
        calculateSteps();
    }

    private void setLengthWidth(Location a, Location b) {
        setLengthWidth(a.toVector(), b.toVector());
    }
    private void setLengthWidth(Vector a, Vector b) {
        double length = switch (axis) {
            case XZ, XY -> Math.abs(a.getX() - b.getX());
            case YZ -> Math.abs(a.getY() - b.getY());
            default -> 0;
        };
        double width = switch (axis) {
            case XZ, YZ -> Math.abs(a.getZ() - b.getZ());
            case XY -> Math.abs(a.getY() - b.getY());
            default -> 0;
        };
        this.halfWidth = Math.abs(width) / 2;
        this.halfLength = Math.abs(length) / 2;
    }

    private Vector vectorFromLengthWidth(double length, double width) {
        return switch (axis) {
            case XZ -> new Vector(length, 0, width);
            case XY -> new Vector(length, width, 0);
            case YZ -> new Vector(0, length, width);
            default -> new Vector(0, 0, 0);
        };
    }

    /**
     * Calculates the nearest factor to particleDensity as step size for the x and z axis
     * Used to ensure the shape has a uniform density of particles
     */
    private void calculateSteps() {
        lengthStep = 2 * halfWidth / Math.round(2 * halfWidth / particleDensity);
        widthStep = 2 * halfLength / Math.round(2 * halfLength / particleDensity);
    }

    @Override
    public Set<Vector> generateOutline() {
        Set<Vector> points = new HashSet<>();
        for (double l = -halfLength + widthStep; l < halfLength; l += widthStep) {
            points.add(vectorFromLengthWidth(l, -halfWidth));
            points.add(vectorFromLengthWidth(l, halfWidth));
        }
        for (double w = -halfWidth; w <= halfWidth; w += lengthStep) {
            points.add(vectorFromLengthWidth(-halfLength, w));
            points.add(vectorFromLengthWidth(halfLength, w));
        }
        return points;
    }

    @Override
    public Set<Vector> generateSurface() {
        Set<Vector> points = new HashSet<>();
        for (double w = -halfWidth; w <= halfWidth; w += lengthStep) {
            for (double l = -halfLength; l <= halfLength; l += widthStep) {
                points.add(vectorFromLengthWidth(l, w));
            }
        }
        return points;
    }

    @Override
    public Set<Vector> generatePoints() {
        if (isDynamic) {
            Location pos = positiveCorner.getLocation();
            Location neg = negativeCorner.getLocation();
            setLengthWidth(neg, pos);
            // get center of rectangle
            Vector offset = pos.toVector().subtract(neg.toVector()).multiply(0.5);
            switch (axis) {
                case XZ -> offset.setY(0);
                case XY -> offset.setZ(0);
                case YZ -> offset.setX(0);
            };
            location = new DynamicLocation(neg.clone().add(offset));
        }
        calculateSteps();
        Set<Vector> points = super.generatePoints();
        points.forEach(vector -> vector.add(centerOffset));
        return points;
    }

    // Ensure that the points are always needing to be updated if the start or end location is dynamic
    @Override
    public Set<Vector> getPoints(Quaternion orientation) {
        Set<Vector> points = super.getPoints(orientation);
        if (isDynamic)
            this.needsUpdate = true;
        return points;
    }

    @Override
    public void setParticleCount(int count) {
        switch (style){
            case FILL, SURFACE -> particleDensity = Math.sqrt(4 * halfWidth * halfLength / count);
            case OUTLINE -> particleDensity = 4 * (halfWidth + halfLength) / count;
        };
        this.needsUpdate = true;
    }


    @Override
    public double getLength() {
        return halfLength * 2;
    }

    @Override
    public double getWidth() {
        return halfWidth * 2;
    }

    @Override
    public double getHeight() {
        return 0;
    }

    @Override
    public void setLength(double length) {
        this.halfLength = Math.abs(length) / 2;
        this.needsUpdate = true;
    }

    @Override
    public void setWidth(double width) {
        this.halfWidth = Math.abs(width) / 2;
        this.needsUpdate = true;
    }

    @Override
    public void setHeight(double height) {
    }

    public int getAxis() {
        return axis;
    }

    public void setAxis(int axis) {
        this.axis = axis;
    }

    @Override
    public Shape clone() {
        Rectangle rectangle = (isDynamic ? new Rectangle(negativeCorner, positiveCorner, axis): new Rectangle(this.getLength(), this.getWidth(), axis));
        rectangle.isDynamic = this.isDynamic;
        return this.copyTo(rectangle);
    }

    @Override
    public String toString() {
        if (isDynamic)
            return "rectangle from " + negativeCorner.toString() + " to " + positiveCorner.toString();
        return "rectangle with length " + this.getLength() + " and width " + this.getWidth();
    }
}