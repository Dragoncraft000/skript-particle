package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;

import java.util.Set;

public class Line extends Shape {

    private Vector start;
    private Vector end;

    public Line() {
        this(new Vector(0, 0, 0), new Vector(0, 0, 0));
    }

    public Line(Vector end) {
        this(new Vector(0, 0, 0), end);
    }

    public Line(Vector start, Vector end) {
        super();
        this.start = start;
        this.end = end;
    }

    @Override
    public Set<Vector> generateOutline() {
        return MathUtil.calculateLine(start, end, particleDensity);
    }

    public Vector getStart() {
        return start.clone();
    }

    public void setStart(Vector start) {
        this.start = start.clone();
    }

    public Vector getEnd() {
        return end.clone();
    }

    public void setEnd(Vector end) {
        this.end = end.clone();
    }

    @Override
    public void setParticleCount(int particleCount) {
        particleDensity = (end.clone().subtract(start).length() / particleCount);
        setNeedsUpdate(true);
    }

    @Override
    public Shape clone() {
        Line line = new Line(this.start, this.end);
        this.copyTo(line);
        return line;
    }

    public String toString(){
        return "Line from " + this.start + " to " + this.end;
    }

}