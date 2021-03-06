package gen.spim;

import gen.advanced.SPIMInfRegister;
import gen.spim.spimcfg.Block;
import gen.spim.spimcfg.Graph;

import java.util.HashSet;

/**
 * Created by Robbin Ni on 2015/5/27.
 */
public class SPIMLiveness {

    public static void addDef(SPIMValue val, Block b) {
        if (val instanceof SPIMInfRegister) {
            SPIMInfRegister reg = (SPIMInfRegister)val;
            if (!b.def.contains(reg)) {
                b.def.add(reg);
            }
        }
    }

    public static void addUse(SPIMValue val, Block b) {
        SPIMInfRegister reg = null;
        if (val instanceof SPIMInfRegister) {
            reg = (SPIMInfRegister) val;
        } else if (val instanceof SPIMAddress && ((SPIMAddress) val).regi != null) {
            if (((SPIMAddress) val).regi instanceof SPIMInfRegister) {
                reg = (SPIMInfRegister) (((SPIMAddress) val).regi);
            }
        }
        if (reg != null && !b.def.contains(reg) && !b.use.contains(reg)) {
            b.use.add(reg);
        }
    }

    public static void calUsage(Block b) {
        b.def = new HashSet<>();
        b.use = new HashSet<>();
        for (SPIMInst inst : b.insts) {
            if (inst.label != null) {
                continue;
            }
            if (inst.op.use > 0 && inst.op.def < 1) {
                addUse(inst.val0, b);
            }
            if (inst.op.use > 1 && inst.op.def < 2) {
                addUse(inst.val1, b);
            }
            if (inst.op.use > 2 && inst.op.def < 3) {
                addUse(inst.val2, b);
            }
            if (inst.op.def > 0) {
                addDef(inst.val0, b);
            }
            if (inst.op.def > 1) {
                addDef(inst.val1, b);
            }
            if (inst.op.def > 2) {
                addDef(inst.val2, b);
            }
        }
    }

    public static void calLive(Graph g) {

        class anonymous {
            public void propPrec(Block u, SPIMInfRegister reg) {
                if (u.liveIn.contains(reg)) {
                    return;
                }
                u.liveIn.add(reg);
                for (Block v : u.prec) {
                    if (v.liveOut.contains(reg)) {
                        continue;
                    }
                    v.liveOut.add(reg);
                    if (!v.def.contains(reg)) {
                        propPrec(v, reg);
                    }
                }
            }

        }
        for (Block block : g.blocks) {
            block.liveIn = new HashSet<>();
            block.liveOut = new HashSet<>();
        }
        anonymous tmp = new anonymous();
        for (Block block : g.blocks) {
            for (SPIMInfRegister reg : block.use)  {
                tmp.propPrec(block, reg);
            }
        }
    }

    static SPIMLabel mySelf;

    public static void calcalls(HashSet<Graph> calls, Block b, SPIMCode code) {
        for (SPIMInst inst : b.insts) {
            if (inst.label == null && inst.op.equals(SPIMOp.jal)) {
                SPIMAddress adr = (SPIMAddress)inst.val0;
                if (adr.label.label.equals("___printf") || adr.label.equals(mySelf)) {
                    continue;
                }
                calls.add(code.funcs.get(adr.label));
            }
        }
    }

    public static void LivenessAnalysis(SPIMCode code) {
        for (Graph g : code.graphs) {
            mySelf = g.name;
            for (Block b : g.blocks) {
                calUsage(b);
                g.used.addAll(b.use);
                g.used.addAll(b.def);
                calcalls(g.calls, b, code);
            }
            calLive(g);
        }

        HashSet<Graph> visited = new HashSet<>();
        class anonymous {
            public void dfs(Graph g) {
                if (visited.contains(g)) {
                    return;
                }
                visited.add(g);
                for (Graph g1 : g.calls) {
                    dfs(g1);
                }
            }

        }
        anonymous tmp = new anonymous();
        for (Graph g : code.graphs) {
            visited.clear();
            tmp.dfs(g);
            for (Graph g1 : visited) {
                if (!g.equals(g1)) {
                    g.used.addAll(g1.used);
                }
            }
        }
    }

    static public String print(SPIMCode code) {
        String ret = "";
        int gCnt = 0;
        for (Graph g : code.graphs) {
            ret += "Graph : " + gCnt++ + "\n";
            int bCnt = 0;
            for (Block b : g.blocks) {
                ret += "Block : " + bCnt++ + b + "\n";
                for (SPIMInst inst : b.insts) {
                    ret += inst.print() + "\n";
                }
                for (Block b1 : b.succ) {
                    ret += "Edge To" + b1 + "\n";
                }
                ret += "Def:\n";
                for (SPIMInfRegister reg : b.def) {
                    ret += reg.print() + "\n";
                }
                ret += "Use:\n";
                for (SPIMInfRegister reg : b.use) {
                    ret += reg.print() + "\n";
                }
                ret += "LiveIn:\n";
                for (SPIMInfRegister reg : b.liveIn) {
                    ret += reg.print() + "\n";
                }
                ret += "LiveOut:\n";
                for (SPIMInfRegister reg : b.liveOut) {
                    ret += reg.print() + "\n";
                }
            }
        }
        return ret;
    }
}
