package ast.nodes;

import ast.visitors.Visitor;

/**
 * Created by Robbin Ni on 2015/4/9.
 */
public class IntType extends Type implements Visible {

    @Override
    public Type getShell() {
        return null;
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
