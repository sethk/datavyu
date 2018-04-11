package org.datavyu.views.scriptcreator;

public class RubyArg {
    String name;
    String defaultValue;
    String value;
    boolean optional;
    boolean returnValue;
    String type;
    String description;

    public RubyArg(RubyArg a) {
        this.name = a.getName();
        this.defaultValue = a.getDefaultValue();
        this.value = a.getValue();
        this.optional = a.getOptional();
        this.returnValue = a.returnValue;
        this.type = a.type;
        this.description = a.description;
    }

    public RubyArg(String name, String defaultValue, boolean optional) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.optional = optional;
        this.returnValue = false;
        this.value = (defaultValue.length() > 0) ? defaultValue : "";
        this.type = "";
        this.description = "";
    }

    public RubyArg(String name, String defaultValue, boolean optional, boolean returnValue) {
        this(name, defaultValue, optional);
        this.returnValue = returnValue;
    }

    public RubyArg(String name, String defaultValue, boolean optional, String type, String description, boolean returnValue) {
        this(name, defaultValue, optional, returnValue);
        this.type = type;
        this.description = description;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean getOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public boolean isOptional() {
        return optional;
    }

    public boolean isReturnValue() {
        return returnValue;
    }

    public void setReturnValue(boolean returnValue) {
        this.returnValue = returnValue;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        System.out.println("TYPE");
        System.out.println(type);
        if(value.length() > 0) {
            if(type.equalsIgnoreCase("string")) {
                return name + " = " + "\"" + value + "\"";
            } else {
                return name + " = " + value;
            }
        } else {
            if(type.equalsIgnoreCase("string")) {
                return "\"" + name + "\"";
            } else {
                return name;
            }
        }
    }
}
