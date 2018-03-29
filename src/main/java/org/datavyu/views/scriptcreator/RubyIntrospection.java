package org.datavyu.views.scriptcreator;

import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ParseFailedException;
import org.jruby.embed.ScriptingContainer;

import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class RubyIntrospection {
    private ScriptArea scriptArea;
    private ScriptingContainer container;
    private StringWriter outputStream;

    public RubyIntrospection(ScriptArea scriptArea) {
        // Holds the current script
        this.scriptArea = scriptArea;
        container = new ScriptingContainer(LocalVariableBehavior.PERSISTENT);
        outputStream = new StringWriter();
        container.setWriter(outputStream);
    }

    public String runScript() {
        String script = scriptArea.getText();
        container.clear();
        outputStream.flush();
        try {
            container.runScriptlet(script);
            return outputStream.toString();
        } catch (Exception e) {
            // Now we have to handle this exception and show the user where
            // in the script the exception has occurred
            String msg = e.getMessage();
            e.printStackTrace();
            return msg;
        }
    }

    public List<String> getLocalVariables() {
        String script = "return local_variables";
        Object local_variables = container.runScriptlet(script);
        return (List<String>) local_variables;
    }


}
