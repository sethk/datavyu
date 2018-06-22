package org.datavyu.views.scriptcreator;

import javafx.collections.ObservableList;
import javafx.scene.Scene;
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
    public AddCommandV(RubyClass cmd, Stage parentStage, ObservableList<RubyArg> variablesList) {
        this.command = cmd;
        this.primaryStage = parentStage;
        this.variablesList = variablesList;
    }

    private ComboBox populateBoxForArg(RubyArg arg, ObservableList<RubyArg> variablesList) {
        ComboBox<RubyArg> box = new ComboBox<>();
        for(RubyArg var : variablesList) {
            if (arg.getType().equalsIgnoreCase(var.getType())) {
                box.getItems().add(var);
            }
        }
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
        }
        Scene dialogScene = new Scene(dialogVbox, 300, 200);
        dialog.setScene(dialogScene);
        dialog.show();
    }
}
