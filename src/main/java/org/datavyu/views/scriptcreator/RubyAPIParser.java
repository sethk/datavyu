package org.datavyu.views.scriptcreator;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class RubyAPIParser {
    static List<RubyClass> parseRubyAPI() {
        String path = System.getProperty("user.dir") + File.separator;
        String apiPath = "Datavyu_API.rb";
        List<RubyClass> rubyClasses = new ArrayList<>();

        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(apiPath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            List<String> docStrings = new ArrayList<>();
            String functionDef = "";
            String line;
            while((line = br.readLine()) != null) {
//                line = line.trim();
                if(line.startsWith("#")) {
                    docStrings.add(line);
                } else if (line.startsWith("def")) {
                    // We have a function definition, add it and its docstring
                    functionDef = line;
                    RubyClass rc = new RubyClass(functionDef, docStrings);
                    if(!rc.isHidden()) {
                        rubyClasses.add(rc);
                    }
                } else {
                    // We have something else, clear the docstring and function def
                    docStrings.clear();
                    functionDef = "";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rubyClasses;
    }
}
