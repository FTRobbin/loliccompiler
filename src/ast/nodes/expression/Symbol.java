package ast.nodes.expression;

import ast.nodes.Visible;
import ast.visitors.Visitor;

import java.util.HashMap;

/**
 * Created by Robbin Ni on 2015/4/8.
 */
public class Symbol implements Visible {
    static int count = 0;

    static HashMap<String, Integer> dict = new HashMap<String, Integer>();
    static HashMap<Integer, String> rdict = new HashMap<>();

    static public void reset() {
        count = 0;
        dict.clear();
        rdict.clear();
    }

    static public int getnum(String name) {
        int ret = 0;
        if (dict.containsKey(name.intern())) {
            ret = dict.get(name.intern());
        } else {
            ret = count++;
            dict.put(name.intern(), ret);
            rdict.put(ret, name.intern());
        }
        return ret;
    }

    static public String getstr(int id) {
        String ret = "";
        if (rdict.containsKey(id)) {
            ret = rdict.get(id);
        }
        return ret;
    }

    private String name;
    private boolean anonymous;

    public int num;

    public Symbol(String name) {
        this.name = name;
        anonymous = name.length() == 0;
        if (anonymous) {
            num = count++;
        } else {
            num = getnum(name);
        }
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
