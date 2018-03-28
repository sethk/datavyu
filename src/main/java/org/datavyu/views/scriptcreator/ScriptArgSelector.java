package org.datavyu.views.scriptcreator;

public class ScriptArgSelector extends ScriptArg {

    public ScriptArgSelector(String argName) {
        super(argName);
    }

    @Override
    public CHOOSER_TYPE getChooser() {
        return CHOOSER_TYPE.FREETEXT;
    }
}
