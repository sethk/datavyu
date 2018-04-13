package org.datavyu.views.scriptcreator;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class Command  {
    String command;
    int nestLevel;

    public Command() {
        command = "";
        nestLevel = 1;
    }

    public Command(String command) {
        this.command = command;
    }

    public int getNestLevel() {
        return nestLevel;
    }

    public void setNestLevel(int nestLevel) {
        this.nestLevel = nestLevel;
    }

    @Override
    public String toString() {
        String indent = StringUtils.repeat(ScriptArea.INDENT, nestLevel);
        return indent + this.command;
    }

    public int countLines() {
        return toString().split("\r\n|\r|\n", -1).length;
    }
}
