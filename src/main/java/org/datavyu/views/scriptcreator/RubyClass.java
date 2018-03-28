package org.datavyu.views.scriptcreator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RubyClass extends Command implements Comparable<RubyClass> {
    String header;
    String name;
    String description;

    List<RubyArg> args;
    List<String> docStrings;

    RubyArg returnValue;

    public RubyClass(RubyClass r) {
        this.header = r.header;
        this.name = r.name;
        this.description = r.description;
        this.args = new ArrayList<>();
        for(RubyArg a : r.args) {
            this.args.add(new RubyArg(a));
        }
        this.docStrings = r.docStrings;
        if(r.returnValue != null) {
            this.returnValue = new RubyArg(r.returnValue);
        } else {
            this.returnValue = null;
        }
    }

    public RubyClass(String classHeader, List<String> docStrings) {
        // We want to take in the function definition and docstrings for a function
        // and then parse them to add information to the GUI

        this.header = classHeader;
        this.docStrings = docStrings;
        this.args = new ArrayList<>();

        this.returnValue = null;

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
                    String lineNoType = line.split("\\[")[0].trim() + " " + line.split("\\]")[1].trim();
//                    if(lineNoType.)
//                    String argDescription = lineNoType.split(" ", 3)[2];
//                    arg.setDescription(argDescription);

                    arg.setType(type);
                } else {
                    String argDescription = line.split(" ", 3)[1];
                    arg.setDescription(argDescription);
                }
            }

            // Parse the line for information
            if(line.contains("@return")) {
                this.returnValue = new RubyArg("returnValue", "return_value", true, true);
                this.getArgs().add(this.returnValue);
            }
        }
        this.description = doc;



        System.out.println(name);
        for(int i = 0; i < args.length; i++) {
            System.out.println(args[i] + "\t" + this.args.get(i));
        }


    }

    public String toString() {
        return this.name;
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
            if(!this.getArgs().get(i).returnValue) {
                params.add(this.args.get(i).getValue());
            }
        }
        return params;
    }

    public String toCommand() {
        String joinedParams = this.getParamsList().stream().collect(Collectors.joining(", "));
        RubyArg lastArg = this.getArgs().get(this.getArgs().size()-1);
        String cmd = this.name + "(" + joinedParams + ")";
        if(lastArg.returnValue) {
            return lastArg.getValue() + " = " + cmd;
        } else {
            return cmd;
        }
    }

    public String getValue(int index) {
        return this.args.get(index).getValue();
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

    public RubyArg getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(RubyArg returnValue) {
        this.returnValue = returnValue;
    }
}
