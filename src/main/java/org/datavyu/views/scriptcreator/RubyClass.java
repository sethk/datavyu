package org.datavyu.views.scriptcreator;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RubyClass extends Command implements Comparable<RubyClass> {
    private String header;
    private String name;
    private String description;

    private List<RubyArg> args;
    private List<String> docStrings;

    private List<RubyArg> returnValues;
    private boolean appendReturnValue;
    private boolean hidden;

    public RubyClass(RubyClass r) {
        this.header = r.header;
        this.name = r.name;
        this.description = r.description;
        this.args = new ArrayList<>();
        for(RubyArg a : r.args) {
            this.args.add(new RubyArg(a));
        }
        this.docStrings = r.docStrings;
        if(r.returnValues != null && r.returnValues.size() > 0) {
            this.returnValues = new ArrayList<>();
            for(RubyArg ret : r.returnValues) {
                this.returnValues.add(new RubyArg(ret));
            }
        } else {
            this.returnValues = null;
        }
        this.appendReturnValue = r.appendReturnValue;
        this.hidden = r.hidden;
    }

    public RubyClass(String classHeader, List<String> docStrings) {
        // We want to take in the function definition and docstrings for a function
        // and then parse them to add information to the GUI

        this.header = classHeader;
        this.docStrings = docStrings;
        this.args = new ArrayList<>();
        this.appendReturnValue = false;
        this.nestLevel = 1;
        this.hidden = false;

        this.returnValues = new ArrayList<>();

        // Now parse the header and the docStrings to get name, description, and args
        // def create_mutually_exclusive(name, var1name, var2name, var1_argprefix=nil, var2_argprefix=nil)
        this.name = classHeader.split(" ")[1].split("\\(")[0];
        System.out.println(classHeader);
        String[] args;
        if(!classHeader.contains("()") && classHeader.contains("(")) {
            args = classHeader.split("\\(")[1].split("\\)")[0].split(",");
        } else {
            args = new String[0];
        }

        // Now we have to clean up the arguments and mark them as optional or not optional, default or not default
        for(int i = 0; i < args.length; i++) {
            String arg = args[i];
            boolean optional = false;
            String defaultValue = "";

            arg = arg.trim();
            if(arg.contains("=")) {
                defaultValue = arg.split("=")[1].trim();
                arg = arg.split("=")[0].trim();
            }

            if(arg.contains("*")) {
                optional = true;
                arg = arg.replaceAll("\\*", "");
            }

            args[i] = arg;
            RubyArg a = new RubyArg(arg, defaultValue, optional);
            this.args.add(a);
        }

        // Now clean up the doc strings so we can have a little user guide for each command
        String doc = "";
        Pattern p = Pattern.compile("\\[(.*?)\\]");
        int paramCount = 0;
        for(int i = 0; i < this.docStrings.size(); i++) {
            String line = this.docStrings.get(i);
            line = line.replace("#", "").trim();
            doc += line + "\n";

            if(line.contains("@param")) {
                // Get the corresponding RubyArg that matches this param
                System.out.println(line);

                RubyArg arg = this.args.get(paramCount);
                if(line.contains("[")) {
                    Matcher m = p.matcher(line);
                    m.find();
                    String type = m.group(1);
                    arg.setType(type);
                }
                System.out.println(arg.getName() + "\t" + arg.getType());
                String argDescription = line.split(" ", 3)[1];
                arg.setDescription(argDescription);
                paramCount += 1;
            }

            // Parse the line for information
            if(line.contains("@return")) {
                RubyArg retVal = new RubyArg("returnValue", "return_value", true, true);
                if(line.contains("[")) {
                    Matcher m = p.matcher(line);
                    m.find();
                    String type = m.group(1);
                    retVal.setType(type);
                }
                this.returnValues.add(retVal);
            }

            if(line.contains("@!visibility private")) {
                hidden = true;
            }
        }
        this.getArgs().addAll(this.returnValues);
        this.description = doc;

        System.out.println(name);
        for(int i = 0; i < args.length; i++) {
            System.out.println(args[i] + ScriptArea.INDENT + this.args.get(i));
        }
    }

    public boolean isAppendReturnValue() {
        return appendReturnValue;
    }

    public void setAppendReturnValue(boolean appendReturnValue) {
        this.appendReturnValue = appendReturnValue;
    }

    public String toString() {
        String indent = StringUtils.repeat(ScriptArea.INDENT, nestLevel);
        String joinedParams = this.getParamsList().stream().collect(Collectors.joining(", "));
        RubyArg lastArg = null;
        if(getReturnValues().size() > 0) {
            lastArg = this.getReturnValues().get(getReturnValues().size() - 1);
        }
        String cmd = this.name + "(" + joinedParams + ")";
        if(lastArg != null) {
            if(appendReturnValue) {
                return indent + lastArg.getValue() + " += " + cmd;
            } else {
                List<String> retVals = new ArrayList<>();
                for(RubyArg r : getReturnValues()) {
                    retVals.add(r.getValue());
                }
                return indent + String.join(",", retVals) + " = " + cmd;
            }
        } else {
            return indent + cmd;
        }
    }

    public boolean isHidden() {
        return hidden;
    }

    public int compareTo(RubyClass oc) {
        return this.name.compareTo(oc.name);
    }

    public String getDescription() {
        return this.description;
    }

    public void updateValue(int index, String val) {
        this.args.get(index).setValue(val);
    }

    public List<String> getParamsList() {
        List<String> params = new ArrayList<>();
        for(int i = 0; i < this.args.size(); i++) {
            if(!this.getArgs().get(i).isReturnValue()) {
                params.add(this.args.get(i).toString());
            }
        }
        return params;
    }

    public void updateFromCursor(int column, String text) {
        String currentText = this.toString();
        // Now figure out where in this string the cursor is wanting to edit
//        if(column)
    }

    public String getValue(int index) {
        return this.args.get(index).getValue();
    }

    public void setValue(int index, String value) {
        this.args.get(index).setValue(value);
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<RubyArg> getArgs() {
        return args;
    }

    public void setArgs(List<RubyArg> args) {
        this.args = args;
    }

    public List<String> getDocStrings() {
        return docStrings;
    }

    public void setDocStrings(List<String> docStrings) {
        this.docStrings = docStrings;
    }

    public List<RubyArg> getReturnValues() {
        return returnValues;
    }

    public void setReturnValues(List<RubyArg> returnValues) {
        this.returnValues = returnValues;
    }

    public void addReturnValue(RubyArg retVal) {
        this.returnValues.add(retVal);
    }

    @Override
    public void modifyCommand(int position, String s) {
        position = position - getNestLevel(); // We dont want to count the tabs
        String cmdStr = toString();
        int cmdName = cmdStr.indexOf("(");
        System.out.println("MOD COMMAND");
        if(position < cmdName) {
            // Theyre modifying the name of the command... should we allow this?
            if(getReturnValues() != null) {
                // See if theyre trying to edit the return value name
            }
        } else if(position > cmdName) {
            // Modifying an argument, figure out which one
            position = position - (cmdName + 1);
            String[] args = cmdStr.substring(cmdName+1, cmdStr.length()-1).split(",");
            for(int i = 0; i < args.length; i++) {
                String a = args[i];
                if(position - a.length() <= 0) {
                    // This is the argument we want to modify
                    RubyArg arg = getArgs().get(i);
                    String val = arg.getValue();
                    val = val.substring(0, position) + s + val.substring(position, val.length());
                    arg.setValue(val);
                    System.out.println("Changing val " + val);
                    System.out.println(toString());
                } else {
                    position = position - a.length();
                }
            }
        }
    }
}
