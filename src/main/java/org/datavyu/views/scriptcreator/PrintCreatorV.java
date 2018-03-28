package org.datavyu.views.scriptcreator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class PrintCreatorV {
    List<String> variablesList;
    ScriptArea scriptArea;

    enum CELL_OVERLAP {
        NEST,
        ANY,
        ONSET,
        OFFSET
    }

    public PrintCreatorV(List<String> variablesList, ScriptArea scriptArea) {
        this.variablesList = variablesList;
        this.scriptArea = scriptArea;
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
        Button createScriptButton = new Button("Generate Script");
        outputButtonBox.getChildren().add(createScriptButton);
        outputButtonBox.setAlignment(Pos.CENTER_RIGHT);
        nestingOptionsLayout.getChildren().add(outputButtonBox);

        HBox.setHgrow(overlapRadioBox, Priority.ALWAYS);
        HBox.setHgrow(outputButtonBox, Priority.ALWAYS);

        ListView<String> selectedVariables = new ListView<>();
        selectedVariables.setEditable(true);
        ListView<String> availableVariables = new ListView<>();
        availableVariables.setEditable(true);

        ObservableList<String> availableVarList = FXCollections.observableArrayList(variablesList);
        availableVariables.setItems(availableVarList.sorted());

        moveLeftButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(availableVariables.getSelectionModel().getSelectedItem() != null) {
                    String varName = availableVariables.getSelectionModel().getSelectedItem();
                    availableVarList.remove(varName);
                    selectedVariables.getItems().add(varName);
                }
            }
        });

        moveRightButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(selectedVariables.getSelectionModel().getSelectedItem() != null) {
                    String varName = selectedVariables.getSelectionModel().getSelectedItem();
                    availableVarList.add(varName);
                    selectedVariables.getItems().remove(varName);
                }
            }
        });

        moveUpButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(selectedVariables.getSelectionModel().getSelectedItem() != null) {
                    int selectedIdx = selectedVariables.getSelectionModel().getSelectedIndex();
                    if(selectedIdx > 0) {
                        String varName = selectedVariables.getSelectionModel().getSelectedItem();
                        selectedVariables.getItems().remove(varName);
                        selectedVariables.getItems().add(selectedIdx-1, varName);
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
                        String varName = selectedVariables.getSelectionModel().getSelectedItem();
                        selectedVariables.getItems().remove(varName);
                        selectedVariables.getItems().add(selectedIdx+1, varName);
                    }
                }
            }
        });

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
                generatePrintScript(selectedVariables, scriptArea, oc, trimCheckbox.isSelected());
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

        printWindow.show();
    }

    private void generatePrintScript(ListView<String> selectedVariables, ScriptArea scriptArea,
                                     CELL_OVERLAP overlap, boolean trim) {
        // We will be adding all commands to the commands already in the scriptArea
        List<RubyClass> commands = scriptArea.getCommands();

        for(String variable : selectedVariables.getItems()) {

        }
    }
}
