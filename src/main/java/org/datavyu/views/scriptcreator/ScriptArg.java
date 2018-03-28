package org.datavyu.views.scriptcreator;

public abstract class ScriptArg {
    protected String argName;
    protected String argValue;

    public ScriptArg(String argName) {
        this.argName = argName;
    }

    public enum CHOOSER_TYPE {
        VARIABLE,
        FREETEXT,
        LIST
    }

    public String getArgName() {
        return this.argName;
    }

    public String getArgValue() {
        return this.argValue;
    }

    public void setArgValue(String arg) {
        this.argValue = arg;
    }

    public abstract CHOOSER_TYPE getChooser();
}
