package org.datavyu.views.scriptcreator;

import java.util.ArrayList;
import java.util.List;

public class Loop extends Command {
    List<RubyClass> commands;
    String variable;
    String cellName;

    public Loop(String variable) {
        this.variable = variable;
        this.cellName = variable.toLowerCase() + "Cell";
        commands = new ArrayList<>();
    }

    public Loop(String variable, String cellName) {
        this.cellName = cellName;
        this.variable = variable;
        commands = new ArrayList<>();
    }

    @Override
    public String toString() {
        String s = String.format("for %s in %s\n", this.cellName, this.variable);
        for(RubyClass c : commands) {
            s += "\t" + c.toCommand() + "\n";
        }
        s += "end\n";
        return s;
    }
}
