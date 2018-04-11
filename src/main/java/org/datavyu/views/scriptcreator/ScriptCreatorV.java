package org.datavyu.views.scriptcreator;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.datavyu.Datavyu;
import org.datavyu.views.discrete.SpreadsheetColumn;
import org.jruby.Ruby;

import javax.xml.crypto.Data;
import java.util.*;

import static javafx.application.Application.launch;

public class ScriptCreatorV extends Application {
    Map<String, Command> classMap = new HashMap<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        VBox root = new VBox();

        BorderPane topRoot = new BorderPane();
        Scene scene = new Scene(root, 800, 500);

        ListView<RubyClass> list = new ListView<>();
        list.setCellFactory(lv -> new ListCell<RubyClass>() {
            @Override
            protected void updateItem(RubyClass item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? null : item.getName());
            }
        });
        ListView<RubyArg> argsList = new ListView<>();
        ListView<String> variablesList = new ListView<>();
        List<RubyClass> rubyClasses = RubyAPIParser.parseRubyAPI();
        Collections.sort(rubyClasses);
        System.out.println(rubyClasses);

        for(RubyClass c : rubyClasses) {
            classMap.put(c.getName(), c);
        }

        TextArea doc = new TextArea();
        doc.setWrapText(true);
        doc.setEditable(false);
        doc.setText("Placeholder");

        HBox rightPane = new HBox();
        ObservableList<RubyArg> args = FXCollections.observableArrayList(new ArrayList<>());
        argsList.setItems(args);
        rightPane.getChildren().add(argsList);

        ObservableList<String> variables = FXCollections.observableArrayList(getColumnNames());
        variablesList.setItems(variables);
        rightPane.getChildren().add(variablesList);


        ObservableList<RubyClass> items = FXCollections.observableArrayList(rubyClasses);
        list.setItems(items);
        list.setPrefWidth(250);
        list.setPrefHeight(70);
        list.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                doc.setText(list.getSelectionModel().getSelectedItem().getDescription());
                argsList.setItems(FXCollections.observableArrayList(list.getSelectionModel().getSelectedItem().getArgs()));
            }
        });

        argsList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getClickCount() == 2) {
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("Changing argument value");
                    dialog.setHeaderText("Changing argument value");
                    dialog.setContentText("New value:");
                    Optional<String> result = dialog.showAndWait();
                    if(result.isPresent()) {
                        String value = result.get();
                        RubyClass method = list.getSelectionModel().getSelectedItem();
                        int argIndex = argsList.getSelectionModel().getSelectedIndex();
                        method.updateValue(argIndex, value);
                        System.out.println("UPDATING VALUE " + value + " " + method.getArgs().get(argIndex));
                        argsList.setItems(FXCollections.observableArrayList(method.getArgs()));
                        argsList.refresh();
                    }
                }
            }
        });

        variablesList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                RubyClass method = list.getSelectionModel().getSelectedItem();
                int argIndex = argsList.getSelectionModel().getSelectedIndex();
                String var = variablesList.getSelectionModel().getSelectedItem();

                method.updateValue(argIndex, var);
                System.out.println("UPDATING VALUE " + var + " " + method.getArgs().get(argIndex));
                argsList.setItems(FXCollections.observableArrayList(method.getArgs()));
                argsList.refresh();
            }
        });

        topRoot.setLeft(list);
        topRoot.setCenter(doc);
        topRoot.setRight(rightPane);

        BorderPane bottomPane = new BorderPane();

        ScriptArea scriptArea = new ScriptArea();
        scriptArea.setWrapText(true);
        scriptArea.setEditable(true);
//        scriptArea.setText("The script goes here and is built using the above system!");
//        scriptArea.setPrefRowCount(30);

        bottomPane.setCenter(scriptArea);

        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(true);

        bottomPane.setRight(outputArea);

        ButtonBar buttonBar = new ButtonBar();
        Button addButton = new Button("Add Command");
        ButtonBar.setButtonData(addButton, ButtonBar.ButtonData.APPLY);
        buttonBar.getButtons().add(addButton);

        RubyIntrospection ri = new RubyIntrospection(scriptArea);
        List<String> lv = ri.getLocalVariables();
        System.out.println(Arrays.toString(lv.toArray()));

        addButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                RubyClass method = list.getSelectionModel().getSelectedItem();
                System.out.println("Adding " + method);
                scriptArea.addCommand(method);
                scriptArea.refreshDisplay();
                String output = ri.runScript();
                outputArea.setText(output);
            }
        });

        Button printButton = new Button("Print Script Wizard");
        ButtonBar.setButtonData(printButton, ButtonBar.ButtonData.APPLY);
        buttonBar.getButtons().add(printButton);
        PrintCreatorV printCreator = new PrintCreatorV(getColumnNames(), scriptArea, classMap);

        printButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                printCreator.show();
            }
        });

        root.getChildren().add(topRoot);
        root.getChildren().add(buttonBar);
        root.getChildren().add(bottomPane);

        scene.getStylesheets().add(ScriptArea.class.getResource("ruby-keywords.css").toExternalForm());

        primaryStage.setTitle("Script Creator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public List<String> getColumnNames() {
        try {
            List<SpreadsheetColumn> cols = Datavyu.getView().getSpreadsheetPanel().getColumns();
            List<String> colNames = new ArrayList<>();
            for (SpreadsheetColumn c : cols) {
                colNames.add(c.getColumnName());
            }
            return colNames;
        } catch (NullPointerException e) {
            e.printStackTrace();
            ArrayList<String> s = new ArrayList<>();
            s.add("test1");
            s.add("test2");
            return s;
        }
    }
}
