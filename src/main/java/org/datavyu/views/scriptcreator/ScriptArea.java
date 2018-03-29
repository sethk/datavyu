package org.datavyu.views.scriptcreator;

import javafx.scene.control.TextArea;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptArea extends CodeArea {
    List<RubyClass> commands;
    String baseTextTop = "require 'Datavyu_API.rb'\n\nbegin\n";
    String baseTextBottom = "end";

    private static final String[] KEYWORDS =
            ("alias   and   begin   break   case   class   def   " +
                    "defined?   do   else   elsif   end   ensure   false   " +
                    "for   if   in   module   next   nil   not   or   redo   " +
                    "rescue   retry   return   self   super   then   true   " +
                    "undef   unless   until   when   while   yield").split("\\W+");


    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
            + "|(?<PAREN>" + PAREN_PATTERN + ")"
            + "|(?<BRACE>" + BRACE_PATTERN + ")"
            + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
            + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
            + "|(?<STRING>" + STRING_PATTERN + ")"
            + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

    public ScriptArea() {
        super();
        commands = new ArrayList<>();
        this.setParagraphGraphicFactory(LineNumberFactory.get(this));

        this.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved())) // XXX
                .subscribe(change -> {
                    this.setStyleSpans(0, computeHighlighting(this.getText()));
                });
        this.replaceText(0, 0, baseTextTop + "\n" + baseTextBottom);
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
        this.replaceText(displayStr);
        this.setPrefSize(500,500);
        System.out.println(displayStr);
    }

    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                    matcher.group("PAREN") != null ? "paren" :
                    matcher.group("BRACE") != null ? "brace" :
                    matcher.group("BRACKET") != null ? "bracket" :
                    matcher.group("SEMICOLON") != null ? "semicolon" :
                    matcher.group("STRING") != null ? "string" :
                    matcher.group("COMMENT") != null ? "comment" :
                    null; /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    public List<RubyClass> getCommands() {
        return commands;
    }

    public void setCommands(List<RubyClass> commands) {
        this.commands = commands;
    }
}
