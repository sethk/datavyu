package org.datavyu.views.scriptcreator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PrintCreatorV {
    List<RubyArg> variablesList;
    Map<String, Command> classMap;
    ScriptArea scriptArea;

    enum CELL_OVERLAP {
        NEST,
        ANY,
        ONSET,
        OFFSET
    }

    public PrintCreatorV(List<RubyArg> variablesList, ScriptArea scriptArea, Map<String, Command> classMap) {
        this.variablesList = variablesList;
        this.scriptArea = scriptArea;
        this.classMap = classMap;
    }

    public void show() {
        VBox rootLayout = new VBox();
        HBox columnSelectLayout = new HBox();
        VBox columnSelectButtons = new VBox();
        Button moveLeftButton = new Button("<--");
        Button moveRightButton = new Button("-->");
        Button moveUpButton = new Button("Up");
        Button moveDownButton = new Button("Down");
        columnSelectButtons.getChildren().add(moveLeftButton);
        columnSelectButtons.getChildren().add(moveRightButton);
        columnSelectButtons.getChildren().add(moveUpButton);
        columnSelectButtons.getChildren().add(moveDownButton);
        columnSelectButtons.setAlignment(Pos.CENTER);

        HBox nestingOptionsLayout = new HBox();
        ToggleGroup overlapGroup = new ToggleGroup();
        RadioButton nestedRb = new RadioButton();
        nestedRb.setText("Print only nested cells");
        nestedRb.setToggleGroup(overlapGroup);
        nestedRb.setSelected(true);
        RadioButton anyOverlapRb = new RadioButton();
        anyOverlapRb.setText("Print cells with any overlap");
        anyOverlapRb.setToggleGroup(overlapGroup);
        RadioButton postOverlapRb = new RadioButton();
        postOverlapRb.setText("Print cells that overlap on offset");
        postOverlapRb.setToggleGroup(overlapGroup);
        RadioButton preOverlapRb = new RadioButton();
        preOverlapRb.setText("Print cells that overlap on onset");
        preOverlapRb.setToggleGroup(overlapGroup);
        CheckBox trimCheckbox = new CheckBox();
        trimCheckbox.setText("Trim onsets and offsets of overlapping cells");
        VBox overlapRadioBox = new VBox();
        overlapRadioBox.getChildren().addAll(nestedRb, anyOverlapRb, preOverlapRb, postOverlapRb, trimCheckbox);
        overlapRadioBox.setAlignment(Pos.CENTER_LEFT);

        nestingOptionsLayout.getChildren().add(overlapRadioBox);
        nestingOptionsLayout.setAlignment(Pos.CENTER);
        nestingOptionsLayout.setSpacing(100);

        VBox outputButtonBox = new VBox();
        Button createScriptButton = new Button("Generate Single File Script");
        Button createDirScriptButton = new Button("Generate Directory Print Script");
        Button setOutputFileButton = new Button("Set Output File");

        TextField outputFileTextField = new TextField();
        HBox outputFileBox = new HBox();
        outputFileBox.getChildren().addAll(outputFileTextField, setOutputFileButton);

        outputButtonBox.getChildren().add(outputFileBox);

        outputButtonBox.getChildren().add(createScriptButton);
        outputButtonBox.getChildren().add(createDirScriptButton);
        outputButtonBox.setAlignment(Pos.CENTER_RIGHT);
        nestingOptionsLayout.getChildren().add(outputButtonBox);

        HBox.setHgrow(overlapRadioBox, Priority.ALWAYS);
        HBox.setHgrow(outputButtonBox, Priority.ALWAYS);

        ListView<RubyArg> selectedVariables = new ListView<>();
        selectedVariables.setEditable(true);
        ListView<RubyArg> availableVariables = new ListView<>();
        availableVariables.setEditable(true);

        ObservableList<RubyArg> availableVarList = FXCollections.observableArrayList(variablesList);
        availableVariables.setItems(availableVarList.sorted());

        moveLeftButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(availableVariables.getSelectionModel().getSelectedItem() != null) {
                    RubyArg var = availableVariables.getSelectionModel().getSelectedItem();
                    availableVarList.remove(var);
                    selectedVariables.getItems().add(var);
                }
            }
        });

        moveRightButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(selectedVariables.getSelectionModel().getSelectedItem() != null) {
                    RubyArg var = selectedVariables.getSelectionModel().getSelectedItem();
                    availableVarList.add(var);
                    selectedVariables.getItems().remove(var);
                }
            }
        });

        moveUpButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(selectedVariables.getSelectionModel().getSelectedItem() != null) {
                    int selectedIdx = selectedVariables.getSelectionModel().getSelectedIndex();
                    if(selectedIdx > 0) {
                        RubyArg var = selectedVariables.getSelectionModel().getSelectedItem();
                        selectedVariables.getItems().remove(var);
                        selectedVariables.getItems().add(selectedIdx-1, var);
                    }
                }
            }
        });

        moveDownButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(selectedVariables.getSelectionModel().getSelectedItem() != null) {
                    int selectedIdx = selectedVariables.getSelectionModel().getSelectedIndex();
                    if(selectedIdx < selectedVariables.getItems().size() - 1) {
                        RubyArg var = selectedVariables.getSelectionModel().getSelectedItem();
                        selectedVariables.getItems().remove(var);
                        selectedVariables.getItems().add(selectedIdx+1, var);
                    }
                }
            }
        });



        columnSelectLayout.getChildren().add(selectedVariables);
        columnSelectLayout.getChildren().add(columnSelectButtons);
        columnSelectLayout.getChildren().add(availableVariables);
        columnSelectLayout.setAlignment(Pos.CENTER);

        rootLayout.getChildren().add(columnSelectLayout);
        rootLayout.getChildren().add(nestingOptionsLayout);
        Scene printScene = new Scene(rootLayout, 600, 500);
        Stage printWindow = new Stage();
        printWindow.setTitle("Print Script Wizard");
        printWindow.setScene(printScene);

        createScriptButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                RadioButton overlapButton = (RadioButton) overlapGroup.getSelectedToggle();
                String text = overlapButton.getText();
                CELL_OVERLAP oc;
                if(text.contains("nested")) {
                    oc = CELL_OVERLAP.NEST;
                } else if (text.contains("onset")) {
                    oc = CELL_OVERLAP.ONSET;
                } else if (text.contains("offset")) {
                    oc = CELL_OVERLAP.OFFSET;
                } else {
                    oc = CELL_OVERLAP.ANY;
                }
                generatePrintScript(selectedVariables, scriptArea, oc, trimCheckbox.isSelected(), outputFileTextField.getText());
                printWindow.close();
            }
        });

        createDirScriptButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                RadioButton overlapButton = (RadioButton) overlapGroup.getSelectedToggle();
                String text = overlapButton.getText();
                CELL_OVERLAP oc;
                if(text.contains("nested")) {
                    oc = CELL_OVERLAP.NEST;
                } else if (text.contains("onset")) {
                    oc = CELL_OVERLAP.ONSET;
                } else if (text.contains("offset")) {
                    oc = CELL_OVERLAP.OFFSET;
                } else {
                    oc = CELL_OVERLAP.ANY;
                }
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle("Select directory containing study files.");
                directoryChooser.setInitialDirectory(
                        new File(System.getProperty("user.home"))
                );
                File f = directoryChooser.showDialog(printWindow);
                generateDirectoryPrintScript(selectedVariables, scriptArea, oc, trimCheckbox.isSelected(),
                        outputFileTextField.getText(), f.getAbsolutePath());
                printWindow.close();
            }
        });

        setOutputFileButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select print script output file.");
                fileChooser.setInitialDirectory(
                        new File(System.getProperty("user.home"))
                );
                File f = fileChooser.showSaveDialog(printWindow);
                outputFileTextField.setText(f.getAbsolutePath());
            }
        });

        printWindow.show();
    }

    private void generateDirectoryPrintScript(ListView<RubyArg> selectedVariables, ScriptArea scriptArea,
                                     CELL_OVERLAP overlap, boolean trim, String savePath, String dirPath) {
        // We will be adding all commands to the commands already in the scriptArea
        List<Command> commands = scriptArea.getCommands();
        CommandBlock block = new CommandBlock(1);

        // Create command for looping over files in directory
        Command dirVar = new Command("folder = '" + dirPath + File.separator + "'");
        block.addCommand(dirVar);
        Command dirFiles = new Command("files = Dir.entries(folder)");
        Loop dirLoop = new Loop("files", "file", 1);
        // Load the file into Datavyu
        Command loadData = new Command("$db, $pj = load_db(folder + file)");

        IfStatement fileCheck = new IfStatement("file", IF_TEMPLATE.FILE_CHECK, 2);
        fileCheck.addCommand(loadData);

        dirLoop.addCommand(fileCheck);

        Command saveCmd;
        if(savePath.length() > 0) {
            // Open file for saving
            Command openFile = new Command("output_file = File.new(\"" + savePath + "\", 'w')");
            block.addCommand(openFile);
            saveCmd = new Command("output_file.write(output_str)");
        } else {
            saveCmd = new Command("puts output_str");
        }

        List<RubyArg> parentCells = new ArrayList<>();

        block.addCommand(dirFiles);
        block.addCommand(dirLoop);
        fileCheck.addCommand(nestVariable(3, parentCells, selectedVariables.getItems(), overlap, trim, saveCmd));
        // Create command for writing to file, use puts if no file, print file name?

        commands.add(block);
        scriptArea.setCommands(commands);
    }

    private void generatePrintScript(ListView<RubyArg> selectedVariables, ScriptArea scriptArea,
                                     CELL_OVERLAP overlap, boolean trim, String savePath) {
        // We will be adding all commands to the commands already in the scriptArea
        List<Command> commands = scriptArea.getCommands();

        List<RubyArg> parentCells = new ArrayList<>();

        CommandBlock block = new CommandBlock(1);

        Command saveCmd;
        if(savePath.length() > 0) {
            // Open file for saving
            Command openFile = new Command("output_file = File.new(" + savePath + ", 'w')");
            block.addCommand(openFile);
            saveCmd = new Command("output_file.write(output_str)");
        } else {
            saveCmd = new Command("puts output_str");
        }

        block.addCommand(nestVariable(1, parentCells, selectedVariables.getItems(), overlap, trim, saveCmd));
        commands.add(block);
        scriptArea.setCommands(commands);
    }

    private Command nestVariable(int nestLevel, List<RubyArg> parentVars, List<RubyArg> variablesToNest,
                                 CELL_OVERLAP overlap, boolean trim, Command saveCmd) {

        if(variablesToNest.size() > 0) {
            RubyArg var = variablesToNest.get(0);

            if(parentVars.size() == 0) {
                CommandBlock block = new CommandBlock(nestLevel);
                RubyClass getCmd = (RubyClass)classMap.get("get_column");
                for(RubyArg v : variablesToNest) {
                    RubyClass g = new RubyClass(getCmd);
                    g.setValue(0, v.getName());
                    RubyArg ret = new RubyArg(v.getName(), v.getName(), false, true);
                    g.addReturnValue(ret);
                    block.addCommand(g);
                }
                variablesToNest.remove(0);
                parentVars.add(var);
                Loop loop = new Loop(var.getName(), nestLevel);
                loop.addCommand(nestVariable(nestLevel, parentVars, variablesToNest, overlap, trim, saveCmd));
                block.addCommand(loop);
                return block;
            }
            variablesToNest.remove(0);
            RubyArg parentVar = parentVars.get(parentVars.size()-1);
            parentVars.add(var);


            if(overlap == CELL_OVERLAP.NEST) {
                IfStatement ifStatement = new IfStatement(var.getName() + "_cell", parentVar.getName() + "_cell", IF_TEMPLATE.NEST, nestLevel+2);
                ifStatement.addCommand(nestVariable(nestLevel+2, parentVars, variablesToNest, overlap, trim, saveCmd));
                Loop loop = new Loop(var.getName(), nestLevel+1);
                loop.addCommand(ifStatement);
                return loop;
            } else {
                return null;
            }
        } else {
            RubyClass printCmd = (RubyClass)classMap.get("print_cell_codes");
            CommandBlock block = new CommandBlock(nestLevel+1);
            Command stringInit = new Command("arg_list = []");
            block.addCommand(stringInit);
            RubyArg returnValue = new RubyArg("arg_list", "arg_list", false, true);
            for(RubyArg v : parentVars) {
                RubyClass p = new RubyClass(printCmd);
                p.setValue(0, v.getName() + "_cell");
                p.setAppendReturnValue(true);
                p.addReturnValue(returnValue);
                block.addCommand(p);
            }
            Command joinString = new Command("output_str = arglist.join(\",\")");
            block.addCommand(joinString);
            block.addCommand(saveCmd);

            return block;
        }
    }
}
