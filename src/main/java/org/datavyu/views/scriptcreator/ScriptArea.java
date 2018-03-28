package org.datavyu.views.scriptcreator;

import javafx.scene.control.TextArea;

import java.util.ArrayList;
import java.util.List;

public class ScriptArea extends TextArea {
    List<RubyClass> commands;
    String baseTextTop = "require 'DatavyuAPI.rb'\n\nbegin\n";
    String baseTextBottom = "end";

    public ScriptArea() {
        super();
        commands = new ArrayList<>();
    }

    public void addCommand(RubyClass method) {
        commands.add(new RubyClass(method));
    }

    public void deleteCommand(int index) {
        commands.remove(index);
    }

    public void refreshDisplay() {
        String displayStr = "" + baseTextTop;
        for(RubyClass rc : commands) {
            displayStr += "\t" + rc.toCommand() + "\n";
        }
        displayStr += baseTextBottom;
        this.setText(displayStr);
        System.out.println(displayStr);
    }

    public List<RubyClass> getCommands() {
        return commands;
    }

    public void setCommands(List<RubyClass> commands) {
        this.commands = commands;
    }
}
