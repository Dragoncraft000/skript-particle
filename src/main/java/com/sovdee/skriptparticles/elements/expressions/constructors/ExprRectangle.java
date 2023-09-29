package com.sovdee.skriptparticles.elements.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.shapes.Rectangle;
import com.sovdee.skriptparticles.shapes.Shape;
import com.sovdee.skriptparticles.util.DynamicLocation;
import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Name("Particle Rectangle")
@Description({
        "Creates a rectangle from a length and a width, or from two corners. The length and width must be greater than 0.",
        "When defining a rectangle from two corners, the corners can either be vectors or locations/entities. " +
                "You cannot use both vectors and locations/entities, but you can mix and match locations and entities. " +
                "When using locations, this is a shape that can be drawn without a specific location. It will be drawn between the two given locations.",
        "Note that the rectangle defaults to the xz plane, or parallel to the ground, with x being width and z being length. " +
                "You can change this to the xy or yz plane by using the 'xy' or 'yz'. In all cases, the first axis is length and the second is width."
})
@Examples({
        "set {_shape} to rectangle with length 10 and width 5",
        "set {_shape} to a yz rectangle from vector(0, 0, 0) to vector(10, 10, 10)",
        "draw the shape of a rectangle with length 10 and width 5 at player",
        "",
        "# note that the following does not require a location to be drawn at",
        "draw the shape of a rectangle from player to player's target"
})
@Since("1.0.0")
public class ExprRectangle extends SimpleExpression<Rectangle> {

    static {
        Skript.registerExpression(ExprRectangle.class, Rectangle.class, ExpressionType.COMBINED,
                "[a] [solid:(solid|filled)] [:xz|:xy|:yz] rectangle (with|of) length %number% and width %number%",
                "[a] [solid:(solid|filled)] [:xz|:xy|:yz] rectangle (from|with corners [at]) %location/entity/vector% (to|and) %location/entity/vector%"
        );
    }

    private Expression<Number> lengthExpr;
    private Expression<Number> widthExpr;
    private Shape.Style style;
    private Expression<?> corner1Expr;
    private Expression<?> corner2Expr;
    private int matchedPattern;
    private Rectangle.Plane plane;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        style = parseResult.hasTag("solid") ? Shape.Style.SURFACE : Shape.Style.OUTLINE;
        this.matchedPattern = matchedPattern;
        if (matchedPattern == 0) {
            lengthExpr = (Expression<Number>) exprs[0];
            widthExpr = (Expression<Number>) exprs[1];
        } else {
            corner1Expr = exprs[0];
            corner2Expr = exprs[1];
        }
        plane = Rectangle.Plane.XZ;
        if (parseResult.hasTag("xy")) plane = Rectangle.Plane.XY;
        else if (parseResult.hasTag("yz")) plane = Rectangle.Plane.YZ;
        return true;
    }

    @Override
    protected @Nullable Rectangle[] get(Event event) {
        Rectangle rectangle;
        if (matchedPattern == 0) {
            if (lengthExpr == null || widthExpr == null) return null;
            Number length = lengthExpr.getSingle(event);
            Number width = widthExpr.getSingle(event);
            if (length == null || width == null) return null;
            length = Math.max(length.doubleValue(), MathUtil.EPSILON);
            width = Math.max(width.doubleValue(), MathUtil.EPSILON);
            rectangle = new Rectangle(length.doubleValue(), width.doubleValue(), plane);
        } else {
            if (corner1Expr == null || corner2Expr == null) return null;
            Object corner1 = corner1Expr.getSingle(event);
            Object corner2 = corner2Expr.getSingle(event);
            if (corner1 == null || corner2 == null) return null;

            // vector check
            if (corner1 instanceof Vector && corner2 instanceof Vector) {
                // if both are vectors, create a static rectangle
                rectangle = new Rectangle((Vector) corner1, (Vector) corner2, plane);
                return new Rectangle[]{rectangle};
            } else if (corner1 instanceof Vector || corner2 instanceof Vector) {
                // if only one is a vector, return empty array
                return null;
            } else {
                // if neither are vectors, create a dynamic rectangle
                corner1 = DynamicLocation.fromLocationEntity(corner1);
                corner2 = DynamicLocation.fromLocationEntity(corner2);
                if (corner1 == null || corner2 == null)
                    return null;
                rectangle = new Rectangle((DynamicLocation) corner1, (DynamicLocation) corner2, plane);
            }
        }
        rectangle.setStyle(style);
        return new Rectangle[]{rectangle};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Rectangle> getReturnType() {
        return Rectangle.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (style == Shape.Style.SURFACE ? "solid " : "") +
                switch (plane) {
                    case XZ -> " xz ";
                    case XY -> " xy ";
                    case YZ -> " yz ";
                } +
                switch (matchedPattern) {
                    case 0 ->
                            "rectangle with length " + lengthExpr.toString(event, debug) + " and width " + widthExpr.toString(event, debug);
                    case 1 ->
                            "rectangle from " + corner1Expr.toString(event, debug) + " to " + corner2Expr.toString(event, debug);
                    default -> "";
                };
    }
}
