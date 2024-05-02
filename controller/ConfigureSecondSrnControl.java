
package controller;

import com.sun.javafx.sg.prism.NGNode;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.FileChooser;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import model.*;
import org.sikuli.script.Location;
import org.sikuli.script.Screen;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import util.CmdLine;
import config.AppConfig;
import util.SSHManager;

public class ConfigureSecondSrnControl {

    public Device device;
    ;
    @FXML
    private GridPane grid;

    @FXML
    Button buttonApply;

    @FXML
    Button buttonCalibrate;
    //Button buttonSim;
    @FXML
    Button uploadButton;
    @FXML
    TextField textIp;
    //Button buttonControl;

    @FXML
    Button buttonLocalScreen;
    @FXML
    Button buttonClose;
    @FXML
    Button buttonTest;
    @FXML
    TextField textScreenNumber;

    @FXML
    CheckBox screenShot;
    // TextField textSim;

    //@FXML

    // TextField textControl;
    @FXML
    Button buttonShow;

    @FXML
    Button buttonConnect;
    @FXML
    Button buttonConfirm;

    @FXML
    Label prompt;
    @FXML
    Label labelServer;
    @FXML
    Label promptConnect;
    @FXML
    private ComboBox<String> comboServerType;
    @FXML
    Label labelIpAdd;

    @FXML
    TextField textYCali;

    @FXML
    TextField textXCali;

    @FXML
    GridPane gridIP;


//    @FXML
//    Button buttonConfigureDefault;

    String viewID;

    int finalX;
    int finalY;
    private ConfigureHomeControl configureHomeControl;

    private Deps deps;
    private Stage stage;
    boolean screenConfirmed = false;
    public Button lastButton = null;
    int X =0 ;
    int Y =0;
    StationMode stationMode = StationMode.getInstance();
    View secondScreen;

    File file = null;

    public ConfigureSecondSrnControl(Stage stage, Deps deps) throws ParserConfigurationException, IOException, SAXException {
        this.stage = stage;
        this.deps = deps;
        System.out.println("ConfigureScreenController instantiated");
    }
    public void initModel(Device device) {
        this.device = device;
    }
    public void initialize() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("([1-9]|1[0-6])?")) { // Matches empty, or numbers 1 to 16
                return change;
            }
            return null;
        };
        textXCali.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 0, filter));
        textYCali.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 0, filter));


        textScreenNumber.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 1, filter));

        buttonShow.setOnAction(event -> setSecondScreenCount(Integer.parseInt(textScreenNumber.getText())));

        buttonTest.setOnAction(event -> {
            String signal = deps.getSignalService().getRandomSignal(true);
            //CmdLine.sendSocketCmdTest("rnv" + viewID, signal, "Signal");
            CmdLine.getResponseSocketDifferent("rnv" + viewID, signal, "Signal", "localhost","SC") ;


        });
        buttonApply.setOnAction(event -> {

                Screen screen = new Screen(Integer.parseInt(viewID));

            secondScreen = new View("rnv" + viewID,screen,finalX+X,finalY+Y);


        });
        buttonConfirm.setOnAction(event -> {
            prompt.setText("**Click on the green button to select a screen for automation testing**");
            prompt.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: red;");
            screenConfirmed = true;
            for (Node child : grid.getChildren()) {
                ((Rectangle) ((StackPane) child).getChildren().get(0)).setFill(Color.GREEN);
            }

        });
        buttonClose.setOnAction(event -> {
            if (secondScreen != null ) {
                Node source = (Node) event.getSource();
                Stage stage = (Stage) source.getScene().getWindow();
                stage.close();
            }else{
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please Click on apply to select the Screen", ButtonType.OK);
                alert.showAndWait();
            }

        });

        buttonCalibrate.setOnAction(event -> {
            X =Integer.parseInt(textXCali.getText());
            Y =Integer.parseInt(textYCali.getText());
            String signal = deps.getSignalService().getRandomSignal(true);
            System.out.println(signal);
            CmdLine.getResponseSocketDifferent("rnv" + viewID, signal, "Signal",device.getIpAddress(),device.getType());
            Location centerLocation = new Location(finalX+X, finalY+Y);
            centerLocation.click();
            });

        buttonTest.setOnAction(event -> {
            String signal = deps.getSignalService().getRandomSignal(true);
            //CmdLine.sendSocketCmdTest("rnv" + viewID, signal, "Signal");
            CmdLine.sendSocketCmdTest("rnv" + viewID, signal, "Signal");

        });
    }

    public void showStage(Pane root){
        Scene scene = new Scene(root,600,800);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setTitle("Screen Configuration");
        stage.show();
    }
    public void setSecondScreenCount(int numberOfScreens) {
        final Button[] lastButtonHolder = new Button[1];
        //buttonConfirm.setDisable(false);
        grid.getChildren().clear(); // Clear existing buttons if any
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();
        buttonApply.setDisable(true);
        buttonTest.setDisable(true);
        boolean evenNumberOfScreens = (numberOfScreens == 1) || (
                (numberOfScreens % 2 == 0) && (numberOfScreens / 2 != 1));

        if (!evenNumberOfScreens) {
            numberOfScreens++; // To make it even
            screenConfirmed = false;
            buttonConfirm.setVisible(true);
            prompt.setText("**Click on any enabled button to swap it with disabled button. Such that it matches your screen Configuration **");
            prompt.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: red;");
        } else {
            buttonConfirm.setVisible(false);
        }

        int numColumns = numberOfScreens / 2 == 1 ? 2 : numberOfScreens / 2;

        for (int i = 0; i < numColumns; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setMinWidth(20.0);
            colConst.setPrefWidth(20.0);


            colConst.setPercentWidth(100.0 / numColumns);
            grid.getColumnConstraints().add(colConst);
        }

        for (int i = 0; i < 2; i++) { // 2 rows
            RowConstraints rowConst = new RowConstraints();
            rowConst.setPercentHeight(50);
            grid.getRowConstraints().add(rowConst);
        }

        for (int i = 0; i < numberOfScreens; i++) {
            StackPane stack = new StackPane();
            // Create a rectangle to represent the screen
            Rectangle screen = new Rectangle(50, 45); // Set width and height
            screen.setFill(Color.RED); // Set color

            // Create a button and place it on top of the rectangle
            Button button = new Button("" + (i));
            button.setPrefWidth(20);
            button.setPrefHeight(20);
            button.setId("button" + i); // General ID for all buttons
            if (i == numberOfScreens - 1 && !evenNumberOfScreens) {
                button.setId("disable"); // Specific ID for the disabled button
                button.setDisable(true);

                lastButtonHolder[0] = button;

            }
            if (!buttonConfirm.isVisible()) {

                screen.setFill(Color.GREEN);
                prompt.setText("**Click on the green button to select a screen for automation testing**");
                prompt.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: red;");
            }
            Button finalLastButton = lastButton;

            button.setOnAction(event -> {
                if (lastButtonHolder[0] != null && !screenConfirmed) {
                    String lastButtonText = lastButtonHolder[0].getText();
                    Button newLastButton = lastButtonHolder[0];
                    // Handler for button click
                    Button clickedButton = (Button) event.getSource();
                    lastButtonHolder[0] = clickedButton;
                    newLastButton.setDisable(false);
                    newLastButton.setId(clickedButton.getId());
                    newLastButton.setText(clickedButton.getText());
                    clickedButton.setDisable(true);


                    clickedButton.setId("disable");
                    clickedButton.setText(lastButtonText);
                } else {
                    int x = 2560;
                    int y = 1435 ;
                    buttonApply.setDisable(false);
                    buttonTest.setDisable(false);
                    Button clickedButton = (Button) event.getSource();
                    for (Node child : grid.getChildren()) {
                        if (clickedButton.equals(((StackPane) child).getChildren().get(1))) {
                            Integer rowIndex = GridPane.getRowIndex(child);
                            Integer columnIndex = GridPane.getColumnIndex(child);

                            int row = (rowIndex != null) ? rowIndex : 0;
                            int col = (columnIndex != null) ? columnIndex : 0;

                            finalY = row == 0 ? y / 2 : (row * y) + 717 ;
                            finalX = col == 0 ? x / 2 : (col * x) + 1286;
                            viewID = button.getText();
                            prompt.setText("Selected Screen is " + viewID + ", Click on Test to test the screen else Apply to confirm.");
                            prompt.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: green;");
                            //System.out.println("Selected Screen is at row " + finalX + ", column " + finalY);
                            break;
                        }
                    }
                }
            });
            int gridY = 0;
            int gridX = 0;
            stack.getChildren().addAll(screen, button);
            try {
                gridX = i % numColumns;
                gridY = i / numColumns == 0 ? 1 : 0;
            } catch (Exception ignored) {
            }
            grid.add(stack, gridX, gridY);

        }
    }
}