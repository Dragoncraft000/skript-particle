package com.sovdee.skriptparticles.elements.expressions.properties;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.skriptparticles.shapes.PolyShape;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Shape Side Count - Polygonal/Polyhedral")
@Description({
        "Returns the number of sides of a polygonal or polyhedral shape. This determines how many sides the shape has.",
        "Changing this will change the number of sides of the shape accordingly, with a minimum of 3.",
        "Note that custom polygons will return their side count, but will not be affected by this expression. ",
        "Polyhedrons will return their face count, and can only be set to 4, 6, 8, 12, or 20."
})
@Examples({
        "set sides of {_shape} to 5",
        "set {_shape}'s side count to 6",
        "send sides of {_shape}"
})
@Since("1.0.0")
public class ExprShapeSides extends SimplePropertyExpression<PolyShape, Integer> {

    static {
        register(ExprShapeSides.class, Integer.class, "side[s| count]", "polyshapes");
    }

    @Override
    @Nullable
    public Integer convert(PolyShape polyShape) {
        return polyShape.getSides();
    }

    @Override
    @Nullable
    public Class<?>[] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, RESET, DELETE, ADD, REMOVE -> new Class[]{Number.class};
            case REMOVE_ALL -> null;
        };
    }

    @Override
    public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
        if ((mode == ChangeMode.ADD || mode == ChangeMode.SET || mode == ChangeMode.REMOVE) && (delta == null || delta.length == 0))
            return;

        int change = (delta[0] != null) ? ((Number) delta[0]).intValue() : 3;

        switch (mode) {
            case REMOVE:
                change = -change;
            case ADD:
                for (PolyShape polyShape : getExpr().getArray(event)) {
                    polyShape.setSides(Math.max(3, polyShape.getSides() + change));
                }
                break;
            case RESET:
            case DELETE:
            case SET:
                change = Math.max(3, change);
                for (PolyShape polyShape : getExpr().getArray(event)) {
                    polyShape.setSides(change);
                }
                break;
            case REMOVE_ALL:
            default:
                assert false;
        }
    }

    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    protected String getPropertyName() {
        return "side count";
    }

}
