package ast.nodes.expression;

import ast.nodes.Visible;
import ast.nodes.type.Type;

/**
 * Created by Robbin Ni on 2015/4/8.
 */
public abstract class Expression implements Visible {

    public Type retType = null;

    public boolean isEmpty() {
        return false;
    }

    public abstract int getPrecedence();
}
