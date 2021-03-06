package ast.visitors;

import ast.nodes.declaration.*;
import ast.nodes.expression.*;
import ast.nodes.initialization.InitList;
import ast.nodes.initialization.InitValue;
import ast.nodes.initialization.Initializer;
import ast.nodes.declaration.FunctionDefi;
import ast.nodes.Program;
import ast.nodes.statment.*;
import ast.nodes.type.*;
import parser.Symbols;
import parser.SymbolsRev;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by Robbin Ni on 2015/4/11.
 */
public class PrettyPrinter implements Visitor {

    int indent;
    Integer lastPre;
    ArrayList<String> stack;
    OutputStream out;

    public PrettyPrinter() {
        indent = 0;
        stack = new ArrayList<String>();
    }

    private void push(String str) {
        stack.add(str);
    }

    private String pop() {
        String ret = stack.get(stack.size() - 1);
        stack.remove(stack.size() - 1);
        return ret;
    }

    private String popTo(int target) {
        String ret = "";
        while (stack.size() > target) {
            ret = pop() + ret;
        }
        return ret;
    }

    private String cover(String str, Type shell) {
        if (shell == null) {
            return str;
        } else {
            if (shell instanceof ArrayType) {
                ArrayType tmp = (ArrayType)shell;
                int cur = stack.size();
                if (tmp.cap != null) {
                    tmp.cap.accept(this);
                }
                String cap = popTo(cur);
                str = str + "[" + cap + "]";
                return cover(str, tmp.baseType);
            } else if (shell instanceof PointerType){
                PointerType tmp = (PointerType) shell;
                str = "*" + str;
                if (tmp.baseType instanceof ArrayType || tmp.baseType instanceof FunctionType) {
                    str = "(" + str + ")";
                }
                return cover(str, tmp.baseType);
            } else {
                FunctionType tmp = (FunctionType)shell;
                int cur = stack.size();
                tmp.list.accept(this);
                String para = popTo(cur);
                str = str + "(" + para + ")";
                return cover(str, tmp.returnType);
            }
        }
    }

    private void pushSpace() {
        push(" ");
    }

    private void pushLine() {
        push("\n");
    }

    public void setOutput (OutputStream out) {
        this.out = out;
    }

    static String addIndent = "\0\0\0\0";
    static String delIndent = "\0\0\0\0\0";

    private void print() {
        boolean newLine = false;
        for (String str : stack) {
            if (str.equals(delIndent)) {
                --indent;
                continue;
            }
            if (str.equals(addIndent)) {
                ++indent;
                continue;
            }
            if (str.equals("}")) {
                --indent;
            } else if (newLine && indent > 0) {
                try {
                    out.write("    ".getBytes());
                } catch (IOException e) {
                    System.out.println("Unexpected IOException.");
                    e.printStackTrace();
                }
            }
            newLine = false;
            try {
                out.write(str.getBytes());
            } catch (IOException e) {
                System.out.println("Unexpected IOException.");
                e.printStackTrace();
            }
            if (str.equals("{")) {
                ++indent;
            }
            if (str.equals("\n")) {
                newLine = true;
                for (int i = 0; i < indent - 1; ++i) {
                    try {
                        out.write("    ".getBytes());
                    } catch (IOException e) {
                        System.out.println("Unexpected IOException.");
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void visit(Program p) {
        for (Declaration decl : p.list) {
            decl.accept(this);
            if (!(decl instanceof FunctionDefi)) {
                push(";");
            }
            pushLine();
            pushLine();
        }
        print();
    }

    public void visit(ParaList pl) {
        int i = 0;
        for (Declaration decl : pl.list) {
            ++i;
            decl.accept(this);
            if (i != pl.list.size()) {
                push(",");
                pushSpace();
            }
        }
    }

    public void visit(DeclList dl) {
        for (Declaration decl : dl.list) {
            decl.accept(this);
            push(";");
            pushLine();
        }
    }

    public void visit(FunctionDefi fd) {
        fd.returnType.accept(this);
        pushSpace();
        Type shell = fd.returnType.getShell();
        int cur = stack.size();
        fd.name.accept(this);
        push(cover(popTo(cur), shell));
        push("(");
        fd.paras.accept(this);
        push(")");
        pushSpace();
        //pushLine();
        fd.body.accept(this);
    }

    public void visit(VariableDecl vd) {
        if (vd.specifier) {
            vd.type.accept(this);
        } else {
            while (!stack.get(stack.size() - 1).equals(";")) {
                pop();
            }
            pop();
            push(",");
        }
        pushSpace();
        Type shell = vd.type.getShell();
        int cur = stack.size();
        if (vd.name != null) {
            vd.name.accept(this);
        }
        push(cover(popTo(cur), shell));
        if (vd.init != null) {
            cur = stack.size();
            vd.init.accept(this);
            String init = popTo(cur);
            if (!init.intern().isEmpty()) {
                pushSpace();
                push("=");
                pushSpace();
                push(init);
            }
        }
    }

    public void visit(FunctionDecl fd) {
        if (fd.specifier) {
            fd.type.accept(this);
        } else {
            while (!stack.get(stack.size() - 1).equals(";")) {
                pop();
            }
            pop();
            push(",");
        }
        pushSpace();
        Type shell = fd.type.getShell();
        int cur = stack.size();
        fd.name.accept(this);
        push(cover(popTo(cur), shell));
        /*
        push("(");
        fd.para.accept(this);
        push(")");
        */
    }

    public void visit(TypeDecl td) {
        td.type.accept(this);
        pushSpace();
    }

    public void visit(TypeDef td) {
        if (td.specifier) {
            push("typedef");
            pushSpace();
            td.type.accept(this);
        } else {
            while (!stack.get(stack.size() - 1).equals(";")) {
                pop();
            }
            pop();
            push(",");
        }
        pushSpace();
        Type shell = td.type.getShell();
        int cur = stack.size();
        td.name.accept(this);
        push(cover(popTo(cur), shell));
    }

    public void visit(InitValue iv) {
        iv.expr.accept(this);
    }

    public void visit(InitList il) {
        push("{");
        int count = 0;
        for (Initializer decl : il.list) {
            if (count > 0) {
                push(",");
                pushSpace();
            }
            decl.accept(this);
            ++count;
        }
        push("}");
    }

    public void visit(ArrayType at) {
        at.baseType.accept(this);
    }

    public void visit(PointerType pt) {
        pt.baseType.accept(this);
    }

    public void visit(StructType st) {
        push("struct");
        pushSpace();
        st.name.accept(this);
        pushSpace();
        if (st.list != null) {
            push("{");
            pushLine();
            st.list.accept(this);
            push("}");
        }
    }

    public void visit(UnionType ut) {
        push("union");
        pushSpace();
        ut.name.accept(this);
        pushSpace();
        if (ut.list != null) {
            push("{");
            pushLine();
            ut.list.accept(this);
            push("}");
        }
    }

    public void visit(IntType it) {
        push("int");
    }

    public void visit(CharType ct) {
        push("char");
    }

    public void visit(VoidType vt) {
        push("void");
    }

    public void visit(DefinedType dt) {
        dt.name.accept(this);
    }

    public void visit(FunctionType ft) {
        ft.returnType.accept(this);
    }

    public void visit(StatList sl) {
        for (Statement stat : sl.list) {
            stat.accept(this);
            pushLine();
        }
    }

    public void visit(ExpressionStat es) {
        es.expr.accept(this);
        push(";");
    }

    public void visit(CompoundStat cs) {
        push("{");
        pushLine();
        cs.dlst.accept(this);
        cs.slst.accept(this);
        push("}");
    }

    public void visit(SelectionStat ss) {
        push("if");
        pushSpace();
        push("(");
        ss.expr.accept(this);
        push(")");
        if (ss.iftr instanceof CompoundStat) {
            pushSpace();
            ss.iftr.accept(this);
        } else {
            push(addIndent);
            pushLine();
            ss.iftr.accept(this);
            push(delIndent);
        }
        if (!ss.iffl.isEmpty()) {
            pushSpace();
            push("else");
            if (ss.iffl instanceof CompoundStat) {
                pushSpace();
                ss.iffl.accept(this);
            } else {
                push(addIndent);
                pushLine();
                ss.iffl.accept(this);
                push(delIndent);
            }
        }
    }

    public void visit(IterationStat is) {
        if (is.init.isEmpty() && is.inct.isEmpty() && !is.expr.isEmpty()) {
            push("while");
            pushSpace();
            push("(");
            is.expr.accept(this);
            push(")");
            if (is.stat instanceof CompoundStat) {
                pushSpace();
                is.stat.accept(this);
            } else {
                push(addIndent);
                pushLine();
                is.stat.accept(this);
                push(delIndent);
            }
        } else {
            push("for");
            pushSpace();
            push("(");
            is.init.accept(this);
            push(";");
            pushSpace();
            is.expr.accept(this);
            push(";");
            pushSpace();
            is.inct.accept(this);
            push(")");
            if (is.stat instanceof CompoundStat) {
                pushSpace();
                is.stat.accept(this);
            } else {
                push(addIndent);
                pushLine();
                is.stat.accept(this);
                push(delIndent);
            }
        }
    }

    public void visit(ContinueStat cs) {
        push("continue;");
    }

    public void visit(BreakStat bs) {
        push("break;");
    }

    public void visit(ReturnStat rs) {
        if (rs.expr.isEmpty()) {
            push("return;");
        } else {
            push("return");
            pushSpace();
            rs.expr.accept(this);
            push(";");
        }
    }

    public void visit(EmptyExpr ee) {

    }

    public void visit(ArgumentList al) {
        int count = 0;
        for (Expression decl : al.list) {
            if (count > 0) {
                push(",");
                pushSpace();
            }
            decl.accept(this);
            ++count;
        }
    }

    public void visit(BinaryExpr be) {
        int preL = be.expr1.getPrecedence(),
            preOp = be.getPrecedence(),
            preR = be.expr2.getPrecedence();
        boolean paraL, paraR;
        if (be.getPrecedence() != 1) {
            paraL = preL < preOp;
            paraR = preR <= preOp;
        } else {
            paraL = preL <= preOp;
            paraR = preR < preOp;
        }
        if (paraL) {
            push("(");
        }
        be.expr1.accept(this);
        if (paraL) {
            push(")");
        }
        if (be.op != Symbols.COMMA) {
            pushSpace();
        }
        push(SymbolsRev.getSymbol(be.op)) ;
        pushSpace();
        if (paraR) {
            push("(");
        }
        be.expr2.accept(this);
        if (paraR) {
            push(")");
        }
    }

    public void visit(CastExpr ce) {
        int preOp = ce.getPrecedence(),
            inOp = ce.expr.getPrecedence();
        boolean para = inOp < preOp;
        push("(");
        int cur = stack.size();
        ce.type.accept(this);
        Type shell = ce.type.getShell();
        push(cover(popTo(cur), shell));
        push(")");
        if (para) {
            push("(");
        }
        ce.expr.accept(this);
        if (para) {
            push(")");
        }
    }

    public void visit(UnaryExpr ue) {
        push(SymbolsRev.getSymbol(ue.op));
        boolean para = ue.getPrecedence() > ue.expr.getPrecedence();
        if (para || ue.op == Symbols.SIZEOF) {
            push("(");
        }
        ue.expr.accept(this);
        if (para || ue.op == Symbols.SIZEOF) {
            push(")");
        }
    }

    public void visit(SizeofExpr se) {
        push("sizeof");
        push("(");
        int cur = stack.size();
        se.type.accept(this);
        Type shell = se.type.getShell();
        push(cover(popTo(cur), shell));
        push(")");
    }

    public void visit(FunctionCall fc) {
        fc.func.accept(this);
        push("(");
        fc.argu.accept(this);
        push(")");
    }

    public void visit(ArrayExpr ar) {
        boolean para = ar.getPrecedence() > ar.expr.getPrecedence();
        if (para) {
            push("(");
        }
        ar.expr.accept(this);
        if (para) {
            push(")");
        }
        push("[");
        ar.addr.accept(this);
        push("]");
    }

    public void visit(PointerAccess pa) {
        boolean para = pa.getPrecedence() > pa.expr.getPrecedence();
        if (para) {
            push("(");
        }
        pa.expr.accept(this);
        if (para) {
            push(")");
        }
        push("->");
        pa.id.accept(this);
    }

    public void visit(RecordAccess ra) {
        boolean para = ra.getPrecedence() > ra.expr.getPrecedence();
        if (para) {
            push("(");
        }
        ra.expr.accept(this);
        if (para) {
            push(")");
        }
        push(".");
        ra.id.accept(this);
    }

    public void visit(PostExpr pe) {
        boolean para = pe.getPrecedence() > pe.expr.getPrecedence();
        if (para) {
            push("(");
        }
        pe.expr.accept(this);
        if (para) {
            push(")");
        }
        push(SymbolsRev.getSymbol(pe.op));
    }

    public void visit(Variable v) {
        v.id.accept(this);
    }

    public void visit(IntConst ic) {
        push("" + ic.val);
    }

    public void visit(CharConst cc) {
        push("\'");
        push(SymbolsRev.getChar(cc.ch));
        push("\'");
    }

    public void visit(StringConst sc) {
        push("\"");
        for (int i = 0; i < sc.str.length(); ++i) {
            push(SymbolsRev.getChar(sc.str.charAt(i)));
        }
        push("\"");
    }

    public void visit(Symbol s) {
        push(s.toString());
    }

}
