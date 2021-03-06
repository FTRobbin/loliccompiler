package irt.factory;

import ast.nodes.type.ArrayType;
import ast.nodes.type.Type;
import interpreter.Interpreter;
import irt.Expr;
import mir.*;

import java.util.List;

/**
 * Created by Robbin Ni on 2015/4/24.
 */
public class VariOp extends Op{

    public VariOp(Expr expr) {
        this.expr = expr;
        if (expr.consts.get(1) instanceof ArrayType) {
            expr.setValue((Type)expr.consts.get(1),
                    true, !(((Type)expr.consts.get(1)) instanceof ArrayType), false, null);
        } else {
            expr.setValue((Type)expr.consts.get(1),
                    false, !(((Type)expr.consts.get(1)) instanceof ArrayType), true, null);
        }
    }

    @Override
    public int interpret(Interpreter v) {
        if (expr.retType instanceof ArrayType) {
            return v.writeInt(v.newInt(), v.getId((Integer)expr.consts.get(0)));
        } else {
            return v.getId((Integer) (expr.consts.get(0)));
        }
    }

    @Override
    public Value genIR(Label cur, List<MIRInst> list, Label next, MIRGen gen, VarName ret) {
        int id = (Integer)this.expr.consts.get(0);
        VarName var = gen.getEntry(id);
        if (ret == null || ret.isAbsTmp()) {
            list.add((new EmptyInst()).setLabel(cur));
        }
        if (ret == null) {
            return null;
        } else if (ret.isAbsTmp()) {
            return var;
        } else {
            list.add(new AssignInst(ExprOp.asg, ret, var).setLabel(cur));
            return ret;
        }
    }
}
