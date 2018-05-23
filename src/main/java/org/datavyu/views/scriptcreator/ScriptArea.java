package org.datavyu.views.scriptcreator;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
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

import static javafx.scene.input.KeyCode.F;
import static javafx.scene.input.KeyCombination.CONTROL_DOWN;
import static javafx.scene.input.KeyCombination.SHIFT_DOWN;
import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import org.fxmisc.wellbehaved.event.InputHandler;


public class ScriptArea extends CodeArea {
    List<Command> commands;
    String baseTextTop = "require 'Datavyu_API.rb'\n\nbegin\n";
    String baseTextBottom = "end";

    private static final String[] KEYWORDS =
            ("alias   and   begin   break   case   class   def   " +
                    "defined?   do   else   elsif   end   ensure   false   " +
                    "for   if   in   module   next   nil   not   or   redo   " +
                    "require   rescue   retry   return   self   super   then   true   " +
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

    public static final String INDENT = "    ";

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

        EventHandler<KeyEvent> modifyCommand = e -> {
            int pos = this.getCaretPosition();
            // Now get the line that this position is on
            String[] lines = this.getText().split("\n");
            int curPos = 0;
            String foundLine = "";
            for(String l : lines) {
                curPos += l.length();
                if(curPos >= pos) {
                    foundLine = l.trim();
                    break;
                }
            }
            for(Command c : getAllCommands()) {
                System.out.println(c);
            }
            System.out.println(foundLine);
            Command c = findBlock(foundLine);
            System.out.println(c);

            // Now we've found the command the user is trying to edit
            // we should now figure out what about that command theyre modifying
            c.modifyCommand(this.getCaretColumn(), e.getCharacter());
            System.out.println("MODIFIED " + c.toString());
            e.consume();
            refreshDisplay();
            this.moveTo(pos+1);
        };
        this.addEventFilter(KeyEvent.KEY_TYPED, modifyCommand);
    }

    public void addCommand(RubyClass method) {
        commands.add(new RubyClass(method));
    }

    public void deleteCommand(int index) {
        commands.remove(index);
    }

    public void refreshDisplay() {
        String displayStr = "" + baseTextTop;
        for(Command rc : commands) {
            displayStr += rc.toString() + "\n";
        }
        displayStr += baseTextBottom;
        this.replaceText(displayStr);
        this.setPrefSize(500,500);
        System.out.println(displayStr);
    }

    private List<Command> getAllCommands() {
        return getAllCommandsHelper(new ArrayList<>(), commands);
    }

    private List<Command> getAllCommandsHelper(List<Command> foundCommands, List<Command> parentBlock) {
        for(Command c : parentBlock) {
            foundCommands.add(c);
            if(c instanceof CommandBlock) {
                CommandBlock cb = (CommandBlock) c;
                foundCommands.addAll(getAllCommandsHelper(new ArrayList<>(), cb.getCommands()));
            }
        }
        return foundCommands;
    }

    /*
    Find the smallest block that contains this line
     */
    private Command findBlock(String line) {
        int minCount = Integer.MAX_VALUE;
        Command command = null;

        List<Command> allCommands = getAllCommands();
        for(Command c : allCommands) {
            if(c.toString().contains(line)) {
                int numLines = c.toString().split("\n").length;
                if(minCount > numLines) {
                    minCount = numLines;
                    command = c;
                }
            }
        }
        return command;
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

    public List<Command> getCommands() {
        return commands;
    }

    public void setCommands(List<Command> commands) {
        this.commands = commands;
        refreshDisplay();
    }
}
