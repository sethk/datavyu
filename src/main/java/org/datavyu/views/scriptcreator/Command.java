package org.datavyu.views.scriptcreator;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class Command  {
    String command;

    public Command() {
        command = "";
    }

    public Command(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return this.command;
    }
}
