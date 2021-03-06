package stl;

import ast.nodes.declaration.DeclList;
import ast.nodes.declaration.FunctionDefi;
import ast.nodes.declaration.TypeDecl;
import ast.nodes.declaration.VariableDecl;
import ast.nodes.expression.Symbol;
import ast.nodes.statment.CompoundStat;
import ast.nodes.statment.StatList;
import ast.nodes.type.*;
import ast.visitors.Visitor;

/**
 * Created by Robbin Ni on 2015/4/16.
 */
public class printfDefi extends FunctionDefi {

    public printfDefi() {
        super(new FunctionType(new IntType(), new DeclList()), new Symbol("printf"), (new DeclList()).add(new VariableDecl(new PointerType(new CharType()), new Symbol("argu"), null)), new CompoundStat(new DeclList(), new StatList()));
        this.paras.add(new TypeDecl(new ELLIPSIS()));
        this.type = new FunctionType(this.returnType, this.paras);
        //TODO
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
