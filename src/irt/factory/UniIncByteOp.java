package irt.factory;

import interpreter.Interpreter;
import irt.Expr;
import mir.*;
import parser.Symbols;

import java.util.List;

/**
 * Created by Robbin Ni on 2015/4/24.
 */
public class UniIncByteOp extends Op {

    int delta;

    public UniIncByteOp(Expr expr, int op, int delta) {
        this.expr = expr;
        this.delta = delta * (op == Symbols.INC_OP ? 1 : -1);
        expr.setValue(expr.exprs.get(0).retType, false, false, false, null);
    }

    @Override
    public int interpret(Interpreter v) {
        int addr = this.expr.exprs.get(0).accept(v);
        byte val = (byte)(v.fetchByte(addr) + delta);
        return v.writeByte(addr, val);
    }

    @Override
    public Value genIR(Label cur, List<MIRInst> list, Label next, MIRGen gen, VarName ret) {
        if (ret == null || ret.isAbsTmp()) {
            Label tcur = new Label(Label.DUMMY);
            VarName dest = (VarName)gen.gen(cur, expr.exprs.get(0), list, tcur, VarName.getAbsTmp());
            list.add((new AssignInst(delta < 0 ? ExprOp.sub : ExprOp.add, dest, dest, new IntConst(Math.abs(delta)))).setLabel(tcur));
            return dest;
        }
        Label tcur = new Label(Label.DUMMY);
        VarName dest = (VarName)gen.gen(cur, expr.exprs.get(0), list, tcur, ret);
        list.add((new AssignInst(delta < 0 ? ExprOp.sub : ExprOp.add, dest, dest, new IntConst(Math.abs(delta)))).setLabel(tcur));
        return dest;
    }
}
