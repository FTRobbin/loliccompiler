package mir;

import java.util.List;

/**
 * Created by Robbin Ni on 2015/5/7.
 */
public class GotoInst extends BranchInst {

    public GotoInst(Label target) {
        this.target = target;
        if (target != null && target.isDummy()) {
            target.reInit();
        }
    }

    @Override
    public List<String> print() {
        return super.print("goto " + target.name);
    }
}
