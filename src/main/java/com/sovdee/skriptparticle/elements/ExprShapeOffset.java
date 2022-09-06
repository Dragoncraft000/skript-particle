package com.sovdee.skriptparticle.elements;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.skriptparticle.shapes.Shape;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class ExprShapeOffset extends SimplePropertyExpression<Shape, Vector> {

        static {
            register(com.sovdee.skriptparticle.elements.ExprShapeNormal.class, Vector.class, "offset [vector]","shapes");
        }

        @Override
        public Class<?>[] acceptChange(Changer.ChangeMode mode) {
            if (mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.RESET || mode == Changer.ChangeMode.DELETE || mode == Changer.ChangeMode.REMOVE_ALL)
                return new Class[]{Vector.class};
            return super.acceptChange(mode);
        }

        @Override
        public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
            for (Shape shape : getExpr().getArray(event)) {
                shape.setNeedsUpdate(true);
            }
            switch (mode) {
                case SET:
                    if (delta == null || delta.length == 0)
                        return;
                    for (Shape shape : getExpr().getArray(event)) {
                        shape.setOffset(((Vector) delta[0]));
                    }
                    break;
                case RESET:
                case DELETE:
                case REMOVE_ALL:
                    for (Shape shape : getExpr().getArray(event)) {
                        shape.setOffset(new Vector(0, 0, 0));
                    }
                    break;
            }
        }

        @Override
        public @NotNull Class<? extends Vector> getReturnType() {
            return Vector.class;
        }

        @Override
        protected @NotNull String getPropertyName() {
            return "offset vector of shapes";
        }

        @Override
        public @Nullable Vector convert(Shape shape) {
            return shape.getOffset();
        }
}