package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Deps;
import model.Point;
import model.StationMode;
import model.Track;
import org.sikuli.script.FindFailed;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javafx.collections.ListChangeListener;
import java.util.logging.Logger;
import javafx.application.Platform;



public class TrackController {
    @FXML
    ListView<String> trackListView;

    @FXML
    TextField trackSearchTextField;
    @FXML
    CheckBox blockuUblockCheckBox;
    @FXML
    Button uploadButton;
    @FXML
    Button configureButton;

    @FXML
    Button pauseButton;

    @FXML
    CheckBox blockUnblockCheckBox;

    @FXML
    CheckBox disregardOnOffCheckBox;

    @FXML
    CheckBox disregardOffCheckBox;

    @FXML
    Button testButton;

    @FXML
    Button buttonClose;

    @FXML
    Button blockButton;

    @FXML
    Button unblockButton;

    @FXML
    Button disregardOnButton;

    @FXML
    Button disregardOffButton;
    @FXML
    Label controlNotificationLabel;

    @FXML
    Label testNotificationLabel;

    @FXML
    Button dropSimButton;

    @FXML
    Button pickSimButton;

    @FXML
    Button failSimButton;

    @FXML
    Button unfailSimButton;
    @FXML
    CheckBox blockTrackCheck;

    @FXML
    CheckBox simDropCheck;
    @FXML
    CheckBox FailCheck;

    Set<TrackController.ButtonAction> operations;
    @FXML
    CheckBox disregardTrackCheck;

    private Deps deps;
    private Stage stage;

    private boolean paused = false;
    File file = null;

    public enum ButtonAction {
        BLOCK,
        UNBLOCK,
        SET_DISREGARD_ON,
        SET_DISREGARD_OFF,
        DROP_SIM,
        PICK_SIM,
        FAIL_SIM,
        UNFAIL_SIM,
        CONFIGURE,

        TEST
    }
    private  ObservableList<String> trackList;
    private static final Logger logger = Logger.getLogger(TrackController.class.getName());

    public TrackController(Stage stage, Deps deps) throws ParserConfigurationException, IOException, SAXException {
        this.stage = stage;
        this.deps = deps;
    }
    private void handleTestAction(TrackController.ButtonAction action, String track){
//        Platform.runLater(() -> {
//
//            controlNotificationLabel.setText("");
//            testNotificationLabel.setText("");
//        });
        try {
            switch (action) {
                case BLOCK:
                    deps.getTrackService().blockTrackById(track);
                    deps.getTrackService().unblockTrackById(track);
                    break;
                case UNBLOCK:
                    deps.getTrackService().unblockTrackById(track);
                    break;
                case SET_DISREGARD_ON:
                    deps.getTrackService().setDisregardOnTrackById(track);
                    deps.getTrackService().setDisregardOffTrackById(track);

                    Thread.sleep(1000);
                    break;
                case SET_DISREGARD_OFF:
                    deps.getTrackService().setDisregardOffTrackById(track);
                    break;
                case DROP_SIM:
                    deps.getTrackService().dropSimTrackById(track);
                    //deps.getTrackService().pickSimTrackById(track);
                    break;
//                case PICK_SIM:
//                    deps.getTrackService().pickSimTrackById(track);
//                    break;
//                case FAIL_SIM:
//                    deps.getTrackService().failTrackById(track);
//                    break;
//                case UNFAIL_SIM:
//                    deps.getTrackService().unfailTrackById(track);
//                    break;
            }
        }catch (Exception e) {
            logger.info(e.getMessage());
        }
    }
    private void handleButtonAction(ButtonAction action) {

        controlNotificationLabel.setText("");
        testNotificationLabel.setText("");
        ObservableList<String> selectedTracksList = trackListView.getSelectionModel().getSelectedItems();

        new Thread(() -> {
            for (String o : selectedTracksList) {
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
                            deps.getTrackService().blockTrackById(o);
                            deps.getTrackService().unblockTrackById(o);
                            break;
                        case UNBLOCK:
                            deps.getTrackService().unblockTrackById(o);
                            break;
                        case SET_DISREGARD_ON:
                            deps.getTrackService().setDisregardOnTrackById(o);
                            deps.getTrackService().setDisregardOffTrackById(o);
                            break;
                        case SET_DISREGARD_OFF:
                            deps.getTrackService().setDisregardOffTrackById(o);
                            break;
                        case DROP_SIM:
                            deps.getTrackService().dropSimTrackById(o);

                            break;
//                        case PICK_SIM:
//                            deps.getTrackService().pickSimTrackById(o);
//                            break;
                        case FAIL_SIM:
                            deps.getTrackService().failTrackById(o);
                            break;
//                        case UNFAIL_SIM:
//                            deps.getTrackService().unfailTrackById(o);
//                            break;
                        case CONFIGURE:
                            deps.getTrackService().configureTrackById(o);
                            break;
                        case TEST:
                            if (noCheckboxSelected()) {
                                Platform.runLater(() ->
                                        testNotificationLabel.setText("Please select at least one checkbox option"));
                            }
                            else {
                                operations = createOperationsSet();
                                for (TrackController.ButtonAction eachAction : operations ) {

                                    handleTestAction(eachAction,o);
//                                    if (stopRequested) {
//                                        break;
//                                    }
                                    Thread.sleep(4000);
                                }
                            }
                            break;
                        default:
                            break;
                    }
                    Platform.runLater(() -> {
                        if (selectedTracksList.size() > 1) {
                            controlNotificationLabel.setText("Pause Now");
                        }
                    });
                    Thread.sleep(3000);
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        controlNotificationLabel.setText("The request for track " + o + " failed. Please find the details in the terminal");
                        logger.info(e.getMessage());
                    });
                }
            }
            Platform.runLater(() -> {
                controlNotificationLabel.setText("");
            });
            pauseButton.setDisable(true);
        }).start();
    }

    private boolean noCheckboxSelected() {
        return !blockTrackCheck.isSelected() &&
                !disregardTrackCheck.isSelected() &&
                !FailCheck.isSelected() &&
                !simDropCheck.isSelected() ;

    }
    private Set<TrackController.ButtonAction> createOperationsSet() {
        Set<TrackController.ButtonAction> operations = EnumSet.noneOf(TrackController.ButtonAction.class);

        if (blockTrackCheck.isSelected()) operations.add(TrackController.ButtonAction.BLOCK);
        if (disregardTrackCheck.isSelected()) operations.add(TrackController.ButtonAction.SET_DISREGARD_ON);
        if (simDropCheck.isSelected()) operations.add(ButtonAction.DROP_SIM);
        if(FailCheck.isSelected()) operations.add(ButtonAction.FAIL_SIM);
        System.out.println(operations.size());
        return operations;
    }
    @FXML
    public void initialize() {
        this.trackListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);




        List<Track> tracks = deps.getTrackService().getTracks();
        Set<String> uniqueCircuitNames = new HashSet<>();
        for (Track track : tracks) {
            uniqueCircuitNames.add(track.getCircuitName());
        }

        // Create an ObservableList from the Set of unique names
        ObservableList<String> observableCircuitNames = FXCollections.observableArrayList(uniqueCircuitNames);

        // Set the list to ListView
        trackListView.setItems(observableCircuitNames);
        trackListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Filter functionality
        trackSearchTextField.setOnKeyReleased(event -> {
            trackListView.setItems(observableCircuitNames.filtered(circuitName ->
                    circuitName.toUpperCase().contains(trackSearchTextField.getText().toUpperCase())));
            trackListView.refresh();
        });

        // Listener for changes in selection
        trackListView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends String> change) -> {
            pauseButton.setDisable(!(trackListView.getSelectionModel().getSelectedItems().size() > 1));
        });
        ObservableList<String> selectedTracksList = trackListView.getSelectionModel().getSelectedItems();

        //pauseButton.setDisable(selectedTracksList.size() <= 1);
        uploadButton.setOnAction(event ->  openFileChooser());
        blockButton.setOnAction(event -> handleButtonAction(ButtonAction.BLOCK));
        //unblockButton.setOnAction(event -> handleButtonAction(ButtonAction.UNBLOCK));
        disregardOnButton.setOnAction(event -> handleButtonAction(ButtonAction.SET_DISREGARD_ON));
       // disregardOffButton.setOnAction(event -> handleButtonAction(ButtonAction.SET_DISREGARD_OFF));
        dropSimButton.setOnAction(event -> handleButtonAction(ButtonAction.DROP_SIM));
//        pickSimButton.setOnAction(event -> handleButtonAction(ButtonAction.PICK_SIM));
        failSimButton.setOnAction(event -> handleButtonAction(ButtonAction.FAIL_SIM));
//        unfailSimButton.setOnAction(event -> handleButtonAction(ButtonAction.UNFAIL_SIM));
        configureButton.setOnAction(event -> handleButtonAction(ButtonAction.CONFIGURE));
        testButton.setOnAction(event -> handleButtonAction(ButtonAction.TEST));
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
            List<Track> tracks = deps.getTrackService().getUpdatedTrack();
            Set<String> uniqueCircuitNames = new HashSet<>();
            for (Track track : tracks) {
                uniqueCircuitNames.add(track.getCircuitName());
            }

            ObservableList<String> observableCircuitNames = FXCollections.observableArrayList(uniqueCircuitNames);

            trackListView.setItems(observableCircuitNames);
//            trackList = new ObservableListBase() {
//                List<Track> Tracks = deps.getTrackService().getUpdatedTrack();
//
//                @Override
//                public Object get(int index) {
//                    return Tracks.get(index).getId();
//                }
//
//                @Override
//                public int size() {
//                    return Tracks.size();
//                }
//            };

//            trackListView.setItems(trackList);
        }

    }
    public void showStage(Pane root){
        Scene scene = new Scene(root,800,800);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setTitle("Test Tracks");
        stage.show();
    }
}

