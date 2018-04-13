package org.datavyu.views.scriptcreator;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CommandBlock extends Command {
    List<Command> commands;
    int nestLevel;

    public CommandBlock() {
        nestLevel = 1;
        commands = new ArrayList<>();
    }

    public CommandBlock(int nestLevel) {
        this.nestLevel = nestLevel;
        commands = new ArrayList<>();
    }

    public void addCommand(Command c) {
        commands.add(c);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(Command c : commands) {
            c.setNestLevel(nestLevel);
            sb.append(c.toString() + "\n");
        }
        return sb.toString();
    }
}
