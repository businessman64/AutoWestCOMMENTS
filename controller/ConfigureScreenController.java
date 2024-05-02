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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.converter.IntegerStringConverter;
import model.*;
import org.sikuli.script.Location;
import org.sikuli.script.Screen;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import util.CmdLine;
import config.AppConfig;
import util.SSHManager;

public class ConfigureScreenController {

    public ArrayList<Device> allDevice = new ArrayList<>();;
    @FXML
    private GridPane grid;

    @FXML
    Button buttonApply;

    //Button buttonSim;
    @FXML
    Button uploadButton;
    @FXML
    TextField textIp;
    //Button buttonControl;
    @FXML
    VBox screenDisplay;

    @FXML
    Spinner<Integer> textYCali;

    @FXML
    HBox hboxVnc;
    @FXML
    Spinner<Integer> textXCali;
//    @FXML
//    Button buttonLocalScreen;
    @FXML
    Button buttonClose;
    @FXML
    Button buttonTest;
    @FXML
    TextField textScreenNumber;

    @FXML
    CheckBox screenShot;

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
    GridPane gridIP;


    @FXML
    TextField textScreenNumberVNC;

    @FXML
    ComboBox<String> comboScreenNum;

    @FXML
    Button buttonTestVNC;

//    @FXML
//    Button buttonConfigureDefault;

    String viewID;

    int finalX;
    int finalY;
    private ConfigureHomeControl configureHomeControl;
    int X =0 ;
    int Y =0;
    private Deps deps;
    private Stage stage;
    boolean screenConfirmed = false;
    public Button lastButton = null;


    StationMode stationMode = StationMode.getInstance();
    View screenDetails;
    View secondScreen;
    String viewValue;
    Device device =new Device("","","","",screenDetails);

    File file = null;
    public ConfigureScreenController(Stage stage, Deps deps) throws ParserConfigurationException, IOException, SAXException {
        this.stage = stage;
        this.deps = deps;
        System.out.println("ConfigureScreenController instantiated");
    }
    public void setMainController(ConfigureHomeControl configureHomeControl) {
        this.configureHomeControl = configureHomeControl;
    }
    @FXML
    public void initialize() {

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("([1-9]|1[0-6])?")) { // Matches empty, or numbers 1 to 16
                return change;
            }
            return null;
        };

        textScreenNumberVNC.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 0, filter));

        textScreenNumber.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 1, filter));

        textScreenNumberVNC.textProperty().addListener((observable, oldValue, newValue) -> {
            List<String> comboBoxValuesVNC = new ArrayList<>();
            String value = Objects.equals(newValue, "") ? "0": newValue;
            for (int c = 0; c <= Integer.parseInt(value); c++) {
                comboBoxValuesVNC.add(String.valueOf("rnv"+c));
            }
            comboScreenNum.getItems().clear();
            comboScreenNum.getItems().addAll(comboBoxValuesVNC);

        });

        comboScreenNum.setOnAction(event -> {
            buttonTestVNC.setDisable(false);

                });

        List<String> comboBoxValues = new ArrayList<>();
        comboBoxValues.add("non");
        if(stationMode.isControlIPRequired()){
            comboBoxValues.add("control");
        }
        if (stationMode.isControlWindowRequired() || stationMode.getLocalServer().equals("control"))
            {
                comboBoxValues.add("control");
            }
        if (stationMode.isSimWindowRequired() || stationMode.getLocalServer().equals("noncontrol")) {
            comboBoxValues.add("noncontrol");
        } if (stationMode.isTCWindowRequired()) {
            comboBoxValues.add("tc");
        }
        Set<String> set = new LinkedHashSet<>(comboBoxValues);

        List<String> deduplicatedList = new ArrayList<>(set);
        comboServerType.getItems().clear();
        comboServerType.getItems().addAll(deduplicatedList);
        comboServerType.getSelectionModel().selectFirst();

        comboServerType.setOnAction(event -> {

            gridIP.setManaged(false);
            gridIP.setVisible(false);
            String selectedItem = comboServerType.getSelectionModel().getSelectedItem();
            device.setType(selectedItem);
//            if(!selectedItem.equals(stationMode.getLocalServer())){
////                buttonLocalScreen.setVisible(true);
////                buttonLocalScreen.setManaged(true);
//
//            }

            if(!selectedItem.equals(stationMode.getLocalServer())){
                gridIP.setManaged(true);
                gridIP.setVisible(true);
                hboxVnc.setManaged(true);
                hboxVnc.setVisible(true);
                screenDisplay.setVisible(true);
                screenDisplay.setManaged(true);
                labelIpAdd.setText("SIM IP Address");
            }
            if(stationMode.isControlIPRequired() && !stationMode.isControlWindowRequired()){
                gridIP.setManaged(true);
                gridIP.setVisible(true);
                hboxVnc.setManaged(false);
                hboxVnc.setVisible(false);
                screenDisplay.setVisible(false);
                screenDisplay.setManaged(false);
                labelIpAdd.setText("Control IP Address");
            }
            if(selectedItem.equals("control") && stationMode.getLocalServer().equals("control") && stationMode.isSimIPRequired()){
                gridIP.setManaged(true);
                gridIP.setVisible(true);
                screenDisplay.setVisible(true);
                screenDisplay.setManaged(true);
                labelIpAdd.setText("SIM IP Address");
            }
            if(selectedItem.equals("control") && stationMode.getLocalServer().equals("control") && !stationMode.isSimIPRequired()){
                gridIP.setManaged(false);
                gridIP.setVisible(false);
                screenDisplay.setVisible(true);
                screenDisplay.setManaged(true);
            }
            if(selectedItem.equals("control") && stationMode.getLocalServer().equals("noncontrol") && stationMode.isControlWindowRequired() &&  !stationMode.isControlWindowRequired() ){
                gridIP.setManaged(true);
                gridIP.setVisible(true);
                screenDisplay.setVisible(true);
                screenDisplay.setManaged(true);
                labelIpAdd.setText("Control IP Address");
            }
            if(selectedItem.equals("noncontrol") && stationMode.getLocalServer().equals("noncontrol") && stationMode.isControlIPRequired()){
                gridIP.setManaged(true);
                gridIP.setVisible(true);
                screenDisplay.setVisible(true);
                screenDisplay.setManaged(true);
                labelIpAdd.setText("Sim IP Address");
            }
            if(selectedItem.equals("tc") ){
                gridIP.setManaged(true);
                gridIP.setVisible(true);
                screenDisplay.setVisible(true);
                screenDisplay.setManaged(true);
                labelIpAdd.setText("TC IP Address");
                hboxVnc.setManaged(true);
                hboxVnc.setVisible(true);
            }
        });

        buttonShow.setOnAction(event -> setScreenCount(Integer.parseInt(textScreenNumber.getText())));
        textIp.textProperty().addListener((observable, oldValue, newValue) -> {
                buttonConnect.setDisable(false);
});
        buttonConfirm.setOnAction(event -> {
            prompt.setText("**Click on the green button to select a screen for automation testing**");
            prompt.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: red;");
            screenConfirmed = true;
            for (Node child : grid.getChildren()) {
                ((Rectangle) ((StackPane) child).getChildren().get(0)).setFill(Color.GREEN);
            }

        });
        buttonConnect.setOnAction(event -> {
            SSHManager nonControlIp = null, controlIp = null, tcIP= null;
            try {
                if (comboServerType.getSelectionModel().getSelectedItem().equals("noncontrol")) {
                    nonControlIp = SSHManager.getInstance("sysadmin", "tcms2009", textIp.getText(), 22);
                    if (!nonControlIp.isConnected()) throw new Exception("Failed to connect to non-control IP.");
                }
                if (comboServerType.getSelectionModel().getSelectedItem().equals("Control")) {
                    controlIp = SSHManager.getInstance("sysadmin", "tcms2009", textIp.getText(), 22);
                    if (!controlIp.isConnected()) throw new Exception("Failed to connect to control IP.");
                }
                if (comboServerType.getSelectionModel().getSelectedItem().equals("TC")) {
                    tcIP = SSHManager.getInstance("sysadmin", "tcms2009", textIp.getText(), 22);
                    if (!tcIP.isConnected()) throw new Exception("Failed to connect to control IP.");
                }
                device.setIpAddress(textIp.getText());
                 promptConnect.setText("Connection successful");

            } catch (Exception e) {
                promptConnect.setText("Error in connection. Check the IP address");
                promptConnect.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: red;");
            }
        });

        buttonTestVNC.setOnAction(event -> {
            String signal = deps.getSignalService().getRandomSignal(true);
            CmdLine.getResponseSocketDifferent(comboScreenNum.getSelectionModel().getSelectedItem(), signal, "Signal", textIp.getText(),"SC") ;


        });

        buttonClose.setOnAction(event -> {
            boolean ipRequired= gridIP.isManaged() ? !textIp.getText().isEmpty() : true;
            if ((screenDetails != null && ipRequired) ||(stationMode.isControlIPRequired() && stationMode.getLocalServer().equals("noncontrol")&& !stationMode.isControlWindowRequired() && comboServerType.getSelectionModel().getSelectedItem().equals("control") ) ) {
                device = new Device(comboServerType.getSelectionModel().getSelectedItem(), textIp.getText(), viewValue, String.valueOf(screenShot.isSelected()),screenDetails);


                configureHomeControl.addDeviceToDeviceTable(device);

                Stage stage = (Stage) buttonClose.getScene().getWindow();
                stage.close();
            }else{
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please Click on apply to select the Screen", ButtonType.OK);
                alert.showAndWait();
            }

        });

        buttonTest.setOnAction(event -> {

            X =textXCali.getValue();
            Y =textYCali.getValue();
            String signal = deps.getSignalService().getRandomSignal(true);
            System.out.println(signal);
            if(comboServerType.getSelectionModel().getSelectedItem().equals(stationMode.getLocalServer())) {
                CmdLine.getResponseSocketDifferent("rnv" + viewID, signal, "Signal","localhost","SC");
                Location centerLocation = new Location(finalX+X, finalY+Y);
                centerLocation.click();
            }else {
                CmdLine.getResponseSocketDifferent(comboScreenNum.getSelectionModel().getSelectedItem(), signal, "Signal", textIp.getText(),"SC");
                Location centerLocation = new Location(finalX+X, finalY+Y);
                centerLocation.click();
            }


        });
        buttonApply.setOnAction(event -> {
            X =textXCali.getValue();
            Y =textYCali.getValue();
           // buttonLocalScreen.setDisable(false);
            Screen screen = new Screen(Integer.parseInt(viewID));
            viewValue=hboxVnc.isVisible()? comboScreenNum.getSelectionModel().getSelectedItem():"rnv" + viewID;
            screenDetails = new View(viewValue,screen,finalX+X,finalY+Y);

            device.setScreenDeatils(screenDetails);
            device.setScreen(viewValue);
        });


    }


    public void setScreenCount(int numberOfScreens) {
        final Button[] lastButtonHolder = new Button[1];
        //buttonConfirm.setDisable(false);
        grid.getChildren().clear(); // Clear existing buttons if any
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();
        buttonApply.setDisable(true);
        buttonTest.setDisable(true);
        boolean evenNumberOfScreens = (numberOfScreens == 1) || (
                (numberOfScreens % 2 == 0) && (numberOfScreens / 2 != 1));

        //boolean evenNumberOfScreens = numberOfScreens % 2 == 0 && numberOfScreens / 2 != 1 ;
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
    private void applyIPFilter(TextField textField) {
        Pattern pattern = Pattern.compile(
                "(([0-1]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([0-1]?\\d\\d?|2[0-4]\\d|25[0-5])");

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty() || pattern.matcher(newText).matches()) {
                return change;
            }
            return null;
        };

        TextFormatter<String> formatter = new TextFormatter<>(filter);
        textField.setTextFormatter(formatter);
    }
    private void openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Document");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV", "*.csv")
        );

        file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            // Handle the file (e.g., upload it or read its contents)
            System.out.println("File selected: " + file.getAbsolutePath());
        }
    }

    private void handleClose(WindowEvent event) {
        boolean ipRequired = gridIP.isManaged() && !textIp.getText().isEmpty();
        if (screenDetails != null && ipRequired) {
            device = new Device(comboServerType.getSelectionModel().getSelectedItem(), textIp.getText(), viewValue, String.valueOf(screenShot.isSelected()), screenDetails);
            configureHomeControl.addDeviceToDeviceTable(device);
        } else {
            event.consume();

            Alert alert = new Alert(Alert.AlertType.ERROR, "Please ensure all required fields are filled and click 'Apply' to select the screen before closing.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    // Modifying how you set up the handler
    public void showStage(Pane root) {
        Scene scene = new Scene(root, 600, 900);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setTitle("Screen Configuration");


        stage.show();
    }


}
