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
import javafx.stage.Stage;
import model.Deps;
import model.Signal;
import model.Track;
import org.apache.log4j.Logger;
import org.sikuli.script.FindFailed;
import org.xml.sax.SAXException;


import javax.xml.parsers.ParserConfigurationException;
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


    private Deps deps;
    private Stage stage;

    private  ObservableList<String> signalList;

    public enum SignalAction {
        BLOCK,
        UNBLOCK,
        SET_FLEETING_ON,
        SET_FLEETING_OFF,
        SET_ARS_ON,
        SET_ARS_OFF,
        SET_DISREGARD_ON,
        SET_DISREGARD_OFF,
        SET_ROUTE,
        UNSET_ROUTE,
        TEST,
        CONFIGURE
        // Add other signal-specific actions here
    }
    private boolean paused = false;
    private static final Logger logger = Logger.getLogger(SignalController.class.getName());


    public SignalController(Stage stage, Deps deps) throws ParserConfigurationException, IOException, SAXException {
        this.stage = stage;
        this.deps = deps;
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
                            deps.getSignalService().setFleetingOnById(o);
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
                        case TEST:
                            if (noCheckboxSelected()) {
                                    Platform.runLater(() ->
                                testNotificationLabel.setText("Please select at least one checkbox option"));
                            }
                            else {
                                Set<SignalOperations> operations = createOperationsSet();
                                deps.getSignalService().testSignal(operations, o);
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
                !disregardCheckbox.isSelected();
    }

    private Set<SignalOperations> createOperationsSet() {
        Set<SignalOperations> operations = EnumSet.noneOf(SignalOperations.class);
        if (blockUnblockCheckbox.isSelected()) operations.add(SignalOperations.BLOCKANDUNBLOCK);
        if (fleetingCheckbox.isSelected()) operations.add(SignalOperations.FLEETINGONOFF);
        if (arsCheckbox.isSelected()) operations.add(SignalOperations.ARSONOFF);
        if (disregardCheckbox.isSelected()) operations.add(SignalOperations.DISREGARDONOFF);
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



    public void showStage(Pane root){
        Scene scene = new Scene(root,800,600);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setTitle("Test Signals");
        stage.show();
    }

}
//
//    @FXML
//    public void initialize(){
//        this.signalListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//
//        signalList = new ObservableListBase() {
//            List<Signal> signals = deps.getSignalService().getSignals();
//
//            @Override
//            public Object get(int index) {
//                return signals.get(index).getId();
//            }
//            @Override
//            public int size() {
//                return signals.size();
//            }
//        };
//
//        signalListView.setItems(signalList);
//
//        signalSearchTextField.setOnKeyReleased(event->{
//            signalListView.setItems(signalList.filtered(trackId-> trackId.contains(signalSearchTextField.getText())));
//            signalListView.refresh();
//        });
//
//        blockButton.setOnAction(event ->{
//            controlNotificationLabel.setText("");
//            testNotificationLabel.setText("");
//            ObservableList selectedTracksList= signalListView.getSelectionModel().getSelectedItems();
//            for(Object o :selectedTracksList){
//                try {
//                    this.deps.getSignalService().blockSignalById((String)o);
//                    //The function above calls multiple functions inside and it lets them throw all kinds of exception
//                    //In the end all the exceptions will be handled below
//                    //this.trackService.blockTrackByIndex((Integer) o);
//                } catch (Exception e) {
//                    controlNotificationLabel.setText("The request for signal "+o+" failed. Please find the details in the terminal");
//                    logger.info(e.getMessage());
//                }
//            }
//        });
//        unblockButton.setOnAction(event ->{
//            controlNotificationLabel.setText("");
//            testNotificationLabel.setText("");
//            ObservableList selectedTracksList= signalListView.getSelectionModel().getSelectedItems();
//            for(Object o :selectedTracksList){
//               // try {
//                try {
//                    this.deps.getSignalService().unblockSignalById((String)o);
//                } catch (FindFailed e) {
//                    throw new RuntimeException(e);
//                } catch (ParserConfigurationException e) {
//                    throw new RuntimeException(e);
//                } catch (SAXException e) {
//                    throw new RuntimeException(e);
//                } catch (ObjectStateException e) {
//                    throw new RuntimeException(e);
//                } catch (NetworkException e) {
//                    throw new RuntimeException(e);
//                } catch (FileNotFoundException e) {
//                    throw new RuntimeException(e);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//                //The function above calls multiple functions inside and it lets them throw all kinds of exception
//                    //In the end all the exceptions will be handled below
//                    //this.trackService.blockTrackByIndex((Integer) o);
//                 /*catch (Exception e) {
//                    controlNotificationLabel.setText("The request for track "+o+" failed. Please find the details in the terminal");
//                    logger.info(e.getMessage());
//                }*/
//            }
//        });
//
//        fleetingOnButton.setOnAction(event ->{
//            controlNotificationLabel.setText("");
//            testNotificationLabel.setText("");
//            ObservableList selectedTracksList= signalListView.getSelectionModel().getSelectedItems();
//            for(Object o :selectedTracksList){
//                try {
//                    this.deps.getSignalService().setFleetingOnById((String)o);
//                    //The function above calls multiple functions inside and it lets them throw all kinds of exception
//                    //In the end all the exceptions will be handled below
//                    //this.trackService.blockTrackByIndex((Integer) o);
//                } catch (Exception e) {
//                    controlNotificationLabel.setText("The request for signal "+o+" failed. Please find the details in the terminal");
//                    logger.info(e.getMessage());
//                }
//            }
//
//
//        });
//
//        fleetingOffButton.setOnAction(event ->{
//            controlNotificationLabel.setText("");
//            testNotificationLabel.setText("");
//            ObservableList selectedTracksList= signalListView.getSelectionModel().getSelectedItems();
//            for(Object o :selectedTracksList){
//                try {
//                    this.deps.getSignalService().setFleetingOffById((String)o);
//                    //The function above calls multiple functions inside and it lets them throw all kinds of exception
//                    //In the end all the exceptions will be handled below
//                    //this.trackService.blockTrackByIndex((Integer) o);
//                } catch (Exception e) {
//                    controlNotificationLabel.setText("The request for signal "+o+" failed. Please find the details in the terminal");
//                    logger.info(e.getMessage());
//                }
//            }
//
//        });
//
//        arsOnButton.setOnAction((event) -> {
//            controlNotificationLabel.setText("");
//            testNotificationLabel.setText("");
//            ObservableList selectedTracksList= signalListView.getSelectionModel().getSelectedItems();
//            for(Object o :selectedTracksList){
//                try {
//                    this.deps.getSignalService().setArsOnById((String)o);
//                    //The function above calls multiple functions inside and it lets them throw all kinds of exception
//                    //In the end all the exceptions will be handled below
//                    //this.trackService.blockTrackByIndex((Integer) o);
//                } catch (Exception e) {
//                    controlNotificationLabel.setText("The request for signal "+o+" failed. Please find the details in the terminal");
//                    logger.info(e.getMessage());
//                }
//            }
//
//        });
//        arsOffButton.setOnAction((event) -> {
//            controlNotificationLabel.setText("");
//            testNotificationLabel.setText("");
//            ObservableList selectedTracksList= signalListView.getSelectionModel().getSelectedItems();
//            for(Object o :selectedTracksList){
//                try {
//                    this.deps.getSignalService().setArsOffById((String)o);
//                    //The function above calls multiple functions inside and it lets them throw all kinds of exception
//                    //In the end all the exceptions will be handled below
//                    //this.trackService.blockTrackByIndex((Integer) o);
//                } catch (Exception e) {
//                    controlNotificationLabel.setText("The request for signal "+o+" failed. Please find the details in the terminal");
//                    logger.info(e.getMessage());
//                }
//            }
//
//        });
//        disregardOnButton.setOnAction((event) -> {
//            controlNotificationLabel.setText("");
//            testNotificationLabel.setText("");
//            ObservableList selectedTracksList= signalListView.getSelectionModel().getSelectedItems();
//            for(Object o :selectedTracksList){
//                try {
//                    this.deps.getSignalService().setDisregardOnById((String)o);
//                    //The function above calls multiple functions inside and it lets them throw all kinds of exception
//                    //In the end all the exceptions will be handled below
//                    //this.trackService.blockTrackByIndex((Integer) o);
//                } catch (Exception e) {
//                    controlNotificationLabel.setText("The request for signal "+o+" failed. Please find the details in the terminal");
//                    logger.info(e.getMessage());
//                }
//            }
//
//        });
//        disregardOffButton.setOnAction((event) -> {
//            controlNotificationLabel.setText("");
//            testNotificationLabel.setText("");
//            ObservableList selectedTracksList= signalListView.getSelectionModel().getSelectedItems();
//            for(Object o :selectedTracksList){
//                try {
//                    this.deps.getSignalService().setDisregardOffById((String)o);
//                    //The function above calls multiple functions inside and it lets them throw all kinds of exception
//                    //In the end all the exceptions will be handled below
//                    //this.trackService.blockTrackByIndex((Integer) o);
//                } catch (Exception e) {
//                    controlNotificationLabel.setText("The request for signal "+o+" failed. Please find the details in the terminal");
//                    logger.info(e.getMessage());
//                }
//            }
//
//        });
//
//        setRouteButton.setOnAction((event) -> {
//            controlNotificationLabel.setText("");
//            testNotificationLabel.setText("");
//            ObservableList selectedTracksList= signalListView.getSelectionModel().getSelectedItems();
//            for(Object o :selectedTracksList){
//                try {
//                    this.deps.getSignalService().setRouteById((String)o);
//                } catch (ObjectStateException e) {
//                    throw new RuntimeException(e);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                } catch (ParserConfigurationException e) {
//                    throw new RuntimeException(e);
//                } catch (NetworkException e) {
//                    throw new RuntimeException(e);
//                } catch (SAXException e) {
//                    throw new RuntimeException(e);
//                } catch (FindFailed e) {
//                    throw new RuntimeException(e);
//                }
//                //The function above calls multiple functions inside and it lets them throw all kinds of exception
//                    //In the end all the exceptions will be handled below
//                    //this.trackService.blockTrackByIndex((Integer) o);
//
//            }
//
//        });
//
//        unsetRouteButton.setOnAction((event) -> {
//            controlNotificationLabel.setText("");
//            testNotificationLabel.setText("");
//            ObservableList selectedTracksList= signalListView.getSelectionModel().getSelectedItems();
//            for(Object o :selectedTracksList){
//
//                try {
//                    this.deps.getSignalService().unsetRouteById((String)o);
//                } catch (ObjectStateException e) {
//                    throw new RuntimeException(e);
//                } catch (FileNotFoundException e) {
//                    throw new RuntimeException(e);
//                } catch (ParserConfigurationException e) {
//                    throw new RuntimeException(e);
//                } catch (NetworkException e) {
//                    throw new RuntimeException(e);
//                } catch (SAXException e) {
//                    throw new RuntimeException(e);
//                } catch (FindFailed e) {
//                    throw new RuntimeException(e);
//                }
//                //The function above calls multiple functions inside and it lets them throw all kinds of exception
//                    //In the end all the exceptions will be handled below
//                    //this.trackService.blockTrackByIndex((Integer) o);
//
//            }
//
//        });
//
//        testButton.setOnAction(event->{
//            testNotificationLabel.setText("");
//            controlNotificationLabel.setText("");
//            ObservableList selectedTracksList= signalListView.getSelectionModel().getSelectedItems();
//            if (selectedTracksList!=null){
//                Set operations = new LinkedHashSet<TrackOperations>();
//                int count=0;
//                if(!blockUnblockCheckbox.isSelected() && !fleetingCheckbox.isSelected() && !arsCheckbox.isSelected() && !disregardCheckbox.isSelected()){
//                    testNotificationLabel.setText("Please select at least one checkbox option");
//                    return;
//                }else {
//                    logger.info("\n==================================Test Suite Started " + count++ + "=========================================");
//                    if (blockUnblockCheckbox.isSelected()) {
//                        operations.add(SignalOperations.BLOCKANDUNBLOCK);
//                        //this.trackService.testBlockedTrack((String)o);
//                    }
//                    if (disregardCheckbox.isSelected()) {
//                        operations.add(SignalOperations.DISREGARDONOFF);
//                        //this.trackService.testDisregardedTrack((String) o);
//                    }
//                    if (arsCheckbox.isSelected()) {
//                        operations.add(SignalOperations.ARSONOFF);
//                        //this.trackService.testDisregardedTrack((String) o);
//                    }
//                    if (fleetingCheckbox.isSelected()) {
//                        operations.add(SignalOperations.FLEETINGONOFF);
//                        //this.trackService.testDisregardedTrack((String) o);
//                    }
//
//                    for (Object o : selectedTracksList) {
//                        logger.info("\n=====================================Test case " + count++ + "=========================================");
//                        try {
//                            this.deps.getSignalService().testSignal(operations, (String) o);
//                            logger.info("=======================================Test Case " + count + " PASSED====================================");
//                        } catch (Exception e) {
//                            logger.info("=======================================Test Case " + count + " FAILED====================================");
//                            logger.info("REASON:" + e.getMessage());
//                        }
//                        logger.info("========================================Test Suite completed=======================================");
//                        //this.trackService.blockTrackByIndex((Integer) o);
//                    }
//                }
//            }else{
//                testNotificationLabel.setText("Please select atleast one track");
//            }
//        });
//
//        configureButton.setOnAction(event->{
//            ObservableList selectedsignalsList= signalListView.getSelectionModel().getSelectedItems();
//            for(Object o :selectedsignalsList){
//                try {
//                    this.deps.getSignalService().configureSignalById((String) o);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                } catch (FindFailed e) {
//                    throw new RuntimeException(e);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        });
//
//
//        buttonClose.setOnAction(event->{
//            try {
//                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
//                // Customize controller instance
//                LoginController loginController =  new LoginController(stage, deps);
//
//                loader.setController(loginController);
//                Pane root = loader.load();
//
//                loginController.showStage(root);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
//
//    }
//    public void showStage(Pane root){
//        Scene scene = new Scene(root,800,600);
//        stage.setScene(scene);
//        stage.setResizable(true);
//        stage.setTitle("Test Signals");
//        stage.show();
//    }
//
//}
