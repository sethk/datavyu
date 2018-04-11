package org.datavyu.views.scriptcreator;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

enum IF_TEMPLATE {
    NEST,
    OVERLAP,
    FILE_CHECK
}

public class IfStatement extends Command {
    List<Command> commands;
    String cell1;
    String cell2;
    int nestLevel;
    IF_TEMPLATE template;

    Map<IF_TEMPLATE, String> ifTemplates;

    public IfStatement() {
        ifTemplates = new HashMap<>();
        ifTemplates.put(IF_TEMPLATE.NEST, "if %1$s.onset <= %2$s.onset and %1$s.offset > %2$s.offset");
        ifTemplates.put(IF_TEMPLATE.FILE_CHECK, "if %1$s.endswith(\".opf\")");
        this.nestLevel = 1;
        this.commands = new ArrayList<>();
    }

    public IfStatement(String val1, IF_TEMPLATE template) {
        this();
        this.cell1 = val1;
        this.template = template;
    }

    public IfStatement(String val1, IF_TEMPLATE template, int nestLevel) {
        this(val1, template);
        this.nestLevel = nestLevel;
    }

    public IfStatement(String cell1, String cell2, IF_TEMPLATE template) {
        this();
        this.cell1 = cell1;
        this.cell2 = cell2;
        this.template = template;
    }

    public IfStatement(String cell1, String cell2, IF_TEMPLATE template, int nestLevel) {
        this(cell1, cell2, template);
        this.nestLevel = nestLevel;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String indent = StringUtils.repeat(ScriptArea.INDENT, nestLevel);
        sb.append(indent + String.format(ifTemplates.get(template), cell1, cell2) + "\n");
        for(Command c : this.commands) {
            c.setNestLevel(nestLevel+1);
            sb.append(c.toString() + "\n");
        }
        sb.append(indent + "end");
        return sb.toString();
    }
}
