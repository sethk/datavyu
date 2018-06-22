package org.datavyu.views.scriptcreator;

import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AddCommandV {
    private RubyClass command;
    private Stage primaryStage;
    private ObservableList<RubyArg> variablesList;
    private boolean returnValue = false;

    public AddCommandV(RubyClass cmd, Stage parentStage, ObservableList<RubyArg> variablesList) {
        this.command = cmd;
        this.primaryStage = parentStage;
        this.variablesList = variablesList;
    }

    private ComboBox populateBoxForArg(RubyArg arg, ObservableList<RubyArg> variablesList) {
        ComboBox<String> box = new ComboBox<>();
        box.setEditable(true);
        for(RubyArg var : variablesList) {
            System.out.println(var.getType() + "\t" + arg.getType());
            if (arg.getType().toLowerCase().contains(var.getType().toLowerCase())) {
                box.getItems().add(var.getName());
            }
            if (arg.getType().toLowerCase().contains("string")
                    && !arg.getType().toLowerCase().contains("<string")
                    && !arg.getType().toLowerCase().contains("string>")) {
                box.getItems().add("\"" + var.getName() + "\"");
            }
        }
        box.setOnAction(event -> {
            arg.setValue(box.getValue());
        });
        new AutoCompleteComboBoxListener<RubyArg>(box);
        return box;
    }

    public void show() {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        VBox dialogVbox = new VBox(20);
        dialogVbox.getChildren().add(new Text("Modify options below:"));
        for(RubyArg arg : this.command.getArgs()) {
            HBox argSelector = new HBox();
            argSelector.getChildren().add(new Text(arg.getName() + " Type: " + arg.getType()));
            argSelector.getChildren().add(populateBoxForArg(arg, variablesList));
            dialogVbox.getChildren().add(argSelector);
        }

        ButtonBar buttonBar = new ButtonBar();
        Button okButton = new Button("OK");
        okButton.setOnMouseClicked(event -> {
            // Create command
            returnValue = true;
            dialog.close();
        });
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnMouseClicked(event -> {
            returnValue = false;
            dialog.close();
        });
        buttonBar.getButtons().addAll(okButton, cancelButton);
        dialogVbox.getChildren().add(buttonBar);

        Scene dialogScene = new Scene(dialogVbox);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    public RubyClass getCommand() {
        return command;
    }

    public boolean isReturnValue() {
        return returnValue;
    }
}
