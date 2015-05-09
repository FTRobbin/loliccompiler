package irt.factory;

import interpreter.Interpreter;
import irt.Expr;
import mir.*;

import java.util.List;

/**
 * Created by Robbin Ni on 2015/4/25.
 */
public class AssignOp extends Op {

    int size;

    public AssignOp(Expr expr, int size) {
        this.expr = expr;
        this.size = size;
        expr.setValue(expr.exprs.get(0).retType, false, true, false, null);
    }

    @Override
    public int interpret(Interpreter v) {
        int addr = expr.exprs.get(0).accept(v);
        int val = expr.exprs.get(1).accept(v);
        for (int i = 0; i < size; ++i) {
            v.writeByte(addr + i, v.fetchByte(val + i));
        }
        return addr;
    }

    @Override
    public Value genIR(Label cur, List<MIRInst> list, Label next, MIRGen gen) {
        Label mid = new Label(Label.DUMMY), tcur = new Label(Label.DUMMY);
        VarName dest = (VarName)gen.gen(cur, expr.exprs.get(0), list, mid);
        Value src1 = gen.gen(mid, expr.exprs.get(1), list, tcur);
        list.add((new AssignInst(ExprOp.asg, dest, src1)).setLabel(tcur));
        return dest;
    }
}
