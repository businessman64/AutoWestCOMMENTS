package controller;

import exceptions.NetworkException;
import exceptions.ObjectStateException;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.*;
import org.apache.log4j.Logger;
import org.sikuli.script.FindFailed;
import org.xml.sax.SAXException;


import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class SignalController {
    @FXML
    ListView<String> signalListView;

    @FXML
    TextField signalSearchTextField;

    @FXML
    Button configureButton;

    @FXML
    CheckBox blockUnblockCheckbox;

    @FXML
    CheckBox fleetingCheckbox;

    @FXML
    CheckBox arsCheckbox;

    @FXML
    CheckBox disregardCheckbox;

    @FXML
    CheckBox lowCheckbox;

    @FXML
    Button testButton;

    @FXML
    Button buttonClose;

    @FXML
    Button blockButton;

    @FXML
    Button unblockButton;

    @FXML
    Button fleetingOnButton;

    @FXML
    Button fleetingOffButton;
    @FXML
    Label controlNotificationLabel;

    @FXML
    Label testNotificationLabel;

    @FXML
    Button arsOnButton;

    @FXML
    Button arsOffButton;

    @FXML
    Button pauseButton;
    @FXML
    Button setRouteButton;

    @FXML
    Button unsetRouteButton;
    @FXML
    Button disregardOnButton;
    @FXML
    Button disregardOffButton;

    @FXML
    Button LowSpeedButton;

    @FXML
    Button uploadButton;
    private Deps deps;
    private Stage stage;
    File file = null;
    private  ObservableList<String> signalList;

    public enum SignalAction {
        BLOCK,
        UNBLOCK,

        SET_DISREGARD_ON,
        SET_DISREGARD_OFF,
        SET_ARS_ON,
        SET_ARS_OFF,
        SET_FLEETING_ON,
        SET_FLEETING_OFF,

        LOWSPEED,
        SET_ROUTE,
        UNSET_ROUTE,
        TEST,
        CONFIGURE

        // Add other signal-specific actions here
    }
    private boolean paused = false;
    Set<SignalAction> operations;
    private static final Logger logger = Logger.getLogger(SignalController.class.getName());


    public SignalController(Stage stage, Deps deps) throws ParserConfigurationException, IOException, SAXException {
        this.stage = stage;
        this.deps = deps;
    }

    private void handleTestAction(SignalAction action, String signal){

        try {
            switch (action) {
                case BLOCK:
                    deps.getSignalService().blockSignalById(signal);
                    Thread.sleep(2000);
                    deps.getSignalService().unblockSignalById(signal);

                    break;

                case SET_DISREGARD_ON:
                    deps.getSignalService().setDisregardOnById(signal);
                    Thread.sleep(2000);
                    deps.getSignalService().setDisregardOffById(signal);

                    break;

                case SET_FLEETING_ON:
                    deps.getSignalService().setFleetingOnById(signal,deps.getRouteService());
                    Thread.sleep(1000);
//                    deps.getSignalService().setFleetingOffById(signal);
                    break;

                case SET_ARS_ON:
                    deps.getSignalService().setArsOnById(signal);
                    Thread.sleep(2000);
                    deps.getSignalService().setArsOffById(signal);
                    break;
                case LOWSPEED:
                    deps.getSignalService().setLowSpeedByID(signal,deps.getRouteService());
                    break;
                case CONFIGURE:

                    deps.getSignalService().configureSignalById();

                    break;
            }
            // deps.getRouteService().cleanRouteByID(route);
        }catch (Exception e) {
            logger.info(e.getMessage());
        }
    }
    private void handleSignalAction(SignalAction action) {
        controlNotificationLabel.setText("");
        testNotificationLabel.setText("");
        ObservableList<String> selectedSignalsList = signalListView.getSelectionModel().getSelectedItems();

        new Thread(() -> {
            for (String o : selectedSignalsList) {
                Platform.runLater(() -> {
                    controlNotificationLabel.setText("");
                });
                while (paused) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                try {
                    switch (action) {
                        case BLOCK:
                            deps.getSignalService().blockSignalById(o);
                            break;
                        case UNBLOCK:
                            deps.getSignalService().unblockSignalById(o);
                            break;
                        case SET_DISREGARD_ON:
                            deps.getSignalService().setDisregardOnById(o);
                            break;
                        case SET_DISREGARD_OFF:
                            deps.getSignalService().setDisregardOffById(o);
                            break;
                        case SET_FLEETING_ON:
                            deps.getSignalService().setFleetingOnById(o,deps.getRouteService());
                            break;
                        case SET_FLEETING_OFF:
                            deps.getSignalService().setFleetingOffById(o);
                            break;
                        case SET_ARS_ON:
                            deps.getSignalService().setArsOnById(o);
                            break;
                        case SET_ARS_OFF:
                            deps.getSignalService().setArsOffById(o);
                            break;
                        case CONFIGURE:

                                deps.getSignalService().configureSignalById();

                            break;
                        case LOWSPEED:
                            deps.getSignalService().setLowSpeedByID(o,deps.getRouteService());
                            break;
                        case TEST:
                            if (noCheckboxSelected()) {
                                    Platform.runLater(() ->
                                testNotificationLabel.setText("Please select at least one checkbox option"));
                            }
                            else {

                                operations = createOperationsSet();
                                for (SignalAction eachAction : operations ) {

                                    handleTestAction(eachAction,o);

                                    Thread.sleep(5000);
                                }
                            }
                            break;
                        default:
                            break;
                    }
                    Platform.runLater(() -> {
                        if (selectedSignalsList.size() > 1) {
                            controlNotificationLabel.setText("Pause Now, Or cry forever");
                        }
                    });
                    Thread.sleep(3000);

                } catch (Exception e) {
                    Platform.runLater(() -> controlNotificationLabel.setText("Error: " + e.getMessage()));
                }
            }
        }).start();
    }
    private boolean noCheckboxSelected() {
        return !blockUnblockCheckbox.isSelected() &&
                !fleetingCheckbox.isSelected() &&
                !arsCheckbox.isSelected() &&
                !disregardCheckbox.isSelected() &&
        !lowCheckbox.isSelected();
    }

    private Set<SignalAction> createOperationsSet() {
        Set<SignalAction> operations = EnumSet.noneOf(SignalAction.class);
        if (blockUnblockCheckbox.isSelected()) operations.add(SignalAction.BLOCK);
        if (fleetingCheckbox.isSelected()) operations.add(SignalAction.SET_FLEETING_ON);
        if (arsCheckbox.isSelected()) operations.add(SignalAction.SET_ARS_ON);
        if (disregardCheckbox.isSelected()) operations.add(SignalAction.SET_DISREGARD_ON);
        if (lowCheckbox.isSelected()) operations.add(SignalAction.LOWSPEED);
        return operations;
    }
    @FXML
    public void initialize() {
        signalListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        signalList = new ObservableListBase() {
            List<Signal> signals = deps.getSignalService().getSignals();

            @Override
            public Object get(int index) {
                return signals.get(index).getId();
            }

            @Override
            public int size() {
                return signals.size();
            }
        };
        signalListView.setItems(signalList);

        signalSearchTextField.setOnKeyReleased(event-> {
            signalListView.setItems(signalList.filtered(trackId -> trackId.toUpperCase().contains(signalSearchTextField.getText().toUpperCase())));
            signalListView.refresh();
        });

        signalListView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends String> change) -> {
            pauseButton.setDisable(!(signalListView.getSelectionModel().getSelectedItems().size() > 1));

        });


        // Bind actions to buttons
        blockButton.setOnAction(event -> handleSignalAction(SignalAction.BLOCK));
        unblockButton.setOnAction(event -> handleSignalAction(SignalAction.UNBLOCK));
        fleetingOnButton.setOnAction(event -> handleSignalAction(SignalAction.SET_FLEETING_ON));
        fleetingOffButton.setOnAction(event -> handleSignalAction(SignalAction.SET_FLEETING_OFF));
        arsOnButton.setOnAction(event -> handleSignalAction(SignalAction.SET_ARS_ON));
        arsOffButton.setOnAction(event -> handleSignalAction(SignalAction.SET_ARS_OFF));
        disregardOnButton.setOnAction(event -> handleSignalAction(SignalAction. SET_DISREGARD_ON));
        disregardOffButton.setOnAction(event -> handleSignalAction(SignalAction.SET_DISREGARD_OFF));
        uploadButton.setOnAction(event ->  openFileChooser());
        LowSpeedButton.setOnAction(event -> handleSignalAction(SignalAction.LOWSPEED));

//                SET_ROUTE,
//                UNSET_ROUTE,
//                TEST,
        configureButton.setOnAction(event -> handleSignalAction(SignalAction.CONFIGURE));
        testButton.setOnAction(event -> handleSignalAction(SignalAction.TEST));
        // Bind other buttons with corresponding actions

        buttonClose.setOnAction(event->{
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


        pauseButton.setOnAction(event -> {
            if (paused) {
                paused = false;
                pauseButton.setText("Pause");
            } else {
                paused = true; // Pause processing
                pauseButton.setText("Resume");
            }
        });
    }
    private void openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Document");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV", "*.csv")
        );

        file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            System.out.println("File selected: " + file.getAbsolutePath());

            StationMode stationMode = StationMode.getInstance();
            stationMode.setFile(file);

            signalList = new ObservableListBase() {
                List<Signal> Signals = deps.getSignalService().getUpdatedSignal();

                @Override
                public Object get(int index) {
                    return Signals.get(index).getId();
                }

                @Override
                public int size() {
                    return Signals.size();
                }
            };

            signalListView.setItems(signalList);
        }

    }




    public void showStage(Pane root){
        Scene scene = new Scene(root,800,600);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setTitle("Test Signals");
        stage.show();
    }

}
