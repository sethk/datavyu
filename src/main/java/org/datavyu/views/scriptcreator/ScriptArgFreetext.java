package org.datavyu.views.scriptcreator;

public class ScriptArgFreetext extends ScriptArg {

    public ScriptArgFreetext(String argName) {
        super(argName);
    }

    @Override
    public CHOOSER_TYPE getChooser() {
        return CHOOSER_TYPE.FREETEXT;
    }
}
