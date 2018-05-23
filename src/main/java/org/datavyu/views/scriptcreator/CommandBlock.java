package org.datavyu.views.scriptcreator;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CommandBlock extends Command {
    List<Command> commands;

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

    public List<Command> getCommands() {
        return commands;
    }

    public void setCommands(List<Command> commands) {
        this.commands = commands;
    }

    @Override
    public void modifyCommand(int position, String c) {
        // Do nothing, we should only ever be modifying the things inside of a command block
    }
}
