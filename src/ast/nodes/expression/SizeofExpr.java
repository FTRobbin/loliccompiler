package ast.nodes.expression;

import ast.nodes.Visible;
import ast.nodes.expression.Expression;
import ast.nodes.type.Type;
import ast.visitors.Visitor;

/**
 * Created by Robbin Ni on 2015/4/9.
 */
public class SizeofExpr extends Expression implements Visible {

    public Type type;

    public SizeofExpr(Type type) {
        this.type = type;
    }

    @Override
    public int getPrecedence() {
        return 12;
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
