package org.datavyu.views.scriptcreator;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Loop extends CommandBlock {
    String variable;
    String cellName;

    public Loop(String variable) {
        this.variable = variable;
        this.cellName = variable.toLowerCase() + "_cell";
        commands = new ArrayList<>();
        nestLevel = 1;
    }

    public Loop(String variable, int nestLevel) {
        this(variable);
        this.nestLevel = nestLevel;
    }

    public Loop(String variable, String cellName, int nestLevel) {
        this(variable, nestLevel);
        this.cellName = cellName;
    }

    public void addCommand(Command c) {
        commands.add(c);
    }

    public void removeCommand(int index) {
        commands.remove(index);
    }

    public void removeLastCommand() {
        commands.remove(commands.size()-1);
    }

    public void addCommands(List<Command> cs) {
        commands.addAll(cs);
    }

    @Override
    public String toString() {
        String indent = StringUtils.repeat(ScriptArea.INDENT, nestLevel);
        String s = indent + String.format("for %s in %s\n", this.cellName, this.variable);
        for(Command c : commands) {
            c.setNestLevel(nestLevel+1);
            s += c.toString() + "\n";
        }
        s += indent + "end";
        return s;
    }

    @Override
    public void modifyCommand(int position, String s) {

    }
}
