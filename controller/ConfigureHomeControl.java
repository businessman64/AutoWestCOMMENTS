package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConfigureHomeControl {
    @FXML
    private TableView<Device> tableView;
    @FXML
    private TableColumn<Device, Void> actionColumn;

    private Deps deps;
    private Stage stage;

    private ObservableList<Device> devices = FXCollections.observableArrayList();

    @FXML
    Button buttonControl;
    @FXML
    Button buttonSim;
    @FXML
    Button addButton;
    @FXML
    Button buttonClose;

    @FXML
    CheckBox simIP;
    @FXML
    CheckBox simWindow;
    @FXML
    CheckBox tCWindow;
    @FXML
    CheckBox controlIP;
    @FXML
    CheckBox controlWindow;

    @FXML
    CheckBox localControl;

    @FXML
    CheckBox localSim;

    String selectedMode="";
    public ConfigureHomeControl(Stage stage, Deps deps) throws ParserConfigurationException, IOException, SAXException {
        this.stage = stage;
        this.deps = deps;
    }
    @FXML
    private void initialize() {
        tableView.setItems(FXCollections.observableArrayList());
        ConfigureHomeControl configureHomeControl = this;
        localControl.setOnAction(event -> {
            localSim.setSelected(false);
            simIP.setVisible(true);
            simWindow.setVisible(true);
            tCWindow.setVisible(true);
            controlIP.setVisible(false);
            controlWindow.setVisible(false);
            simIP.setManaged(true);
            simWindow.setManaged(true);
            tCWindow.setManaged(true);
            controlIP.setManaged(false);
            controlWindow.setManaged(false);
                });

        buttonControl.setOnAction(event -> { selectedMode = "control";});
        buttonSim.setOnAction(event -> { selectedMode = "noncontrol";});

        localSim.setOnAction(event -> {
            localControl.setSelected(false);
            simIP.setVisible(false);
            simWindow.setVisible(false);
            tCWindow.setVisible(true);
            controlIP.setVisible(true);
            controlIP.setSelected(true);
            controlWindow.setVisible(true);
            simIP.setManaged(false);
            simWindow.setManaged(false);
            tCWindow.setManaged(true);
            controlIP.setManaged(true);
            controlWindow.setManaged(true);

        });

        addButton.setOnAction(event -> {
            StationMode stationMode = StationMode.getInstance();
            if((localControl.isSelected() || localSim.isSelected()) && !selectedMode.isEmpty()) {
                stationMode.setControlType(selectedMode);
                if (localControl.isSelected()) stationMode.setLocalServer("control");
                if (localSim.isSelected()) stationMode.setLocalServer("noncontrol");
                stationMode.setSimIPRequired(simIP.isSelected());
                stationMode.setSimWindowRequired(simWindow.isSelected());
                if (selectedMode.equals("noncontrol")) stationMode.setControlIPRequired(true);
                stationMode.setControlWindowRequired(controlWindow.isSelected());
                stationMode.setTCWindowRequired(tCWindow.isSelected());


            try {

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ConfigureScreenView.fxml"));
                ConfigureScreenController configureScreenController = new ConfigureScreenController(stage, deps); // Assuming stage and deps are defined elsewhere
                loader.setController(configureScreenController);
                Parent root = loader.load(); // This initializes the controller with the FXML components
                ConfigureScreenController popupController = loader.getController();
                popupController.setMainController(configureHomeControl);
                Stage popupStage = new Stage();
                popupStage.initModality(Modality.APPLICATION_MODAL); // Makes the popup modal
                popupStage.setScene(new Scene(root));
                popupStage.setTitle("Configure Screen");
                popupStage.showAndWait(); // Show and wait until the popup is closed


            } catch (Exception e) { // Simplified exception handling, adjust as necessary
                e.printStackTrace();
            }
            }else{
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please select server type and the Mode", ButtonType.OK);
                alert.showAndWait();

            }
        });

        buttonClose.setOnAction(event->{

            StationMode stationMode = StationMode.getInstance();
            stationMode.setAllDevice(tableView.getItems());
            for(Device device : stationMode.getAllDevice()) {

                if(device.getType().equals("control")){
                    if(!device.getIpAddress().isEmpty() && stationMode.getControlType().equals("control"))
                        stationMode.setSimIP(device.getIpAddress());
                    stationMode.setControlDevice(device);
                }
                if(device.getType().equals("noncontrol")){
                    if(!device.getIpAddress().isEmpty()) stationMode.setSimIP(device.getIpAddress());

                    stationMode.setSimDevice( device);}
                if(device.getType().equals("tc"))stationMode.setTcDevice( device);

            }
//            if(stationMode.getControlType().equals("control")){
                for(Device device : stationMode.getAllDevice())
                    if(device.getType().equals(stationMode.getControlType())) {
                        ViewManager.getInstance().setView(device.getScreenDeatils().getName(),
                                device.getScreenDeatils().getScreen(),
                                device.getScreenDeatils().getCoordinateX()
                                ,device.getScreenDeatils().getCoordinateY()

                        );
                    }
//            }



            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
                // Customize controller instance
                LoginController loginController =  new LoginController(stage, deps);

                loader.setController(loginController);
                Pane root = loader.load();

                loginController.showStage(root);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        actionColumn.setCellFactory(col -> {
            TableCell<Device, Void> cell = new TableCell<Device, Void>() {
                //private final Button addButton = new Button("Add new Device");
                private final Button subtractButton = new Button("Remove");
                private final HBox pane = new HBox( subtractButton);

                {


                    subtractButton.setOnAction(event -> {
                        // Handle Subtract action
                        //List<Device> selectedDevices = new ArrayList<>(tableView.getSelectionModel().getSelectedItems());

                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to remove the selected device?", ButtonType.YES, ButtonType.NO);
                        alert.showAndWait();

                        if (alert.getResult() == ButtonType.YES) {
                             removeSelectedDevice();
                           // tableView.getItems().removeAll(selectedDevices);
                            tableView.refresh();}
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : pane);
                }

            };
            return cell;
        });
    }




    private void removeSelectedDevice() {
        List<Device> selectedDevices = new ArrayList<>(tableView.getSelectionModel().getSelectedItems());
        for(Device device : selectedDevices) {
            devices.removeAll(device);
        }
    }



    public void addDeviceToDeviceTable(Device device) {
        tableView.getItems().add(device);
        System.out.println("Current table items: " + tableView.getItems());

    }

    public void showStage(Pane root){
        Scene scene = new Scene(root,600,800);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setTitle("Screen Configuration");
        stage.show();
    }
}