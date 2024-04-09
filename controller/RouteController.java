package controller;

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
import model.Deps;
import model.Route;

import model.StationMode;
import org.xml.sax.SAXException;


import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Logger;


public class RouteController {
    @FXML
    ListView<String> RouteListView;

    @FXML
    TextField RouteSearchTextField;

    @FXML
    Button spadButton;
    @FXML
    Button approachButton;


    @FXML
    CheckBox blockCheck;

    @FXML
    CheckBox spadCheck;

    @FXML
    TextArea infoText;

    @FXML
    TextArea conflictInfoText;

    @FXML
    CheckBox navigationCheck;
    @FXML
    CheckBox conflictCheck;

    @FXML
    CheckBox lowSpeedCheck;

    @FXML
    CheckBox approachCheck;


    @FXML
    CheckBox disregardCheck;

    @FXML
    Button testButton;
    @FXML
    Button uploadButton;
    @FXML
    Button buttonClose;

    @FXML
    Button blockButton;

    @FXML
    Button navigationButton;

    @FXML
    Label controlNotificationLabel;

    @FXML
    Label testNotificationLabel;

    @FXML
    Button lowSpeedButton;

    @FXML
    Button disregardButton;

    @FXML
    Button pauseButton;
    @FXML
    Button buttonStop;
    @FXML
    Button setRouteButton;

    @FXML
    Button unsetRouteButton;
    @FXML
    Button conflictButton;

    @FXML
    CheckBox RouteCheck;


    private Deps deps;
    private Stage stage;
    private volatile boolean stopRequested = false;

    ObservableList<String> selectedRoutesList;
    private  ObservableList<String> RouteList;
    Set<RouteAction> operations;
    public enum RouteAction {
        SET_ROUTE,
        NAVIGATE,
        SET_LOW_SPEED,
        SET_APPROACH_LOCK,

        SPAD,

        BLOCK,

        SET_DISREGARD_ON,

        UNSET_ROUTE,
        SET_ROUTE_DROP,
        UNSET_ROUTE_DROP,
        CONFLICT,
        TEST,
        // Add other Route-specific actions here
    }
    File file = null;
    private boolean paused = false;
    private static final Logger logger = Logger.getLogger(RouteController.class.getName());


    public RouteController(Stage stage, Deps deps) throws ParserConfigurationException, IOException, SAXException {
        this.stage = stage;
        this.deps = deps;
    }

    private void handleTestAction(RouteAction action, String route){
        controlNotificationLabel.setText("");
        testNotificationLabel.setText("");
        try {
            switch (action) {
                case SET_ROUTE:

                    deps.getRouteService().setRouteBySignalDropDownId(route);
                    Thread.sleep(1000);
                    deps.getRouteService().unSetRouteBySignalDropDownId(route);
                    break;
                case NAVIGATE:
                    deps.getRouteService().navigationById(route);
                    break;
                case SET_LOW_SPEED:
                    deps.getRouteService().setLowRouteById(route);
                    break;
                case SET_APPROACH_LOCK:
                    deps.getRouteService().setApproachLockById(route);
                    break;
                case SPAD:
                    deps.getRouteService().setSpadByID(route);
                    break;
                case SET_DISREGARD_ON:
                    deps.getRouteService().setDisregardByID(route);
                    break;
                case BLOCK:
                    deps.getRouteService().setBlockRouteByID(route);
                    break;
                case CONFLICT:

                    deps.getRouteService().setConflictRouteByID(route);
            }
           // deps.getRouteService().cleanRouteByID(route);
        }catch (Exception e) {
            logger.info(e.getMessage());
        }
    }
    private void handleRouteAction(RouteAction action) {
        controlNotificationLabel.setText("");
        testNotificationLabel.setText("");
        selectedRoutesList = RouteListView.getSelectionModel().getSelectedItems();

        new Thread(() -> {
            for (String o : selectedRoutesList) {
                logger.info("----------------------"+o+"----------------------------");
                if (stopRequested) {
                    break;
                }
                Platform.runLater(() -> {
                    controlNotificationLabel.setText("");
                });
                deps.getRouteService().fillInformation(o);

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
                        case SET_ROUTE_DROP:
                            deps.getRouteService().setRouteBySignalDropDownId(o);
                            break;
                        case UNSET_ROUTE_DROP:
                            deps.getRouteService().unSetRouteBySignalDropDownId(o);
                            break;
                        case NAVIGATE:
                            deps.getRouteService().navigationById(o);
                            break;
                        case SET_LOW_SPEED:
                            deps.getRouteService().setLowRouteById(o);
                            break;
                        case SET_APPROACH_LOCK:
                            deps.getRouteService().setApproachLockById(o);
                            break;
                        case SPAD:
                            deps.getRouteService().setSpadByID(o);
                            break;
                        case SET_DISREGARD_ON:
                            deps.getRouteService().setDisregardByID(o);
                            break;
                        case BLOCK:
                            deps.getRouteService().setBlockRouteByID(o);
                            break;
                        case CONFLICT:
                            deps.getRouteService().setConflictRouteByID(o);
                            break;
                        case TEST:
                            if (noCheckboxSelected()) {
                                Platform.runLater(() ->
                                        testNotificationLabel.setText("Please select at least one checkbox option"));
                            }
                            else {
                                operations = createOperationsSet();
                                for (RouteAction eachAction : operations ) {

                                    handleTestAction(eachAction,o);
                                    if (stopRequested) {
                                        break;
                                    }
                                    Thread.sleep(5000);
                                }
                                try {
                                    deps.getRouteService().cleanRouteByID(o);
                                } catch (IOException | InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            break;
                        default:
                            break;
                    }
                    Platform.runLater(() -> {
                        if (selectedRoutesList.size() > 1) {
                            controlNotificationLabel.setText("Pause Now");
                        }
                    });
                    Thread.sleep(5000);

                } catch (Exception e) {
                    Platform.runLater(() -> controlNotificationLabel.setText("Error: " + e.getMessage()));
                    logger.info(e.getMessage());
                }


            }
        }).start();
        stopRequested = false;
    }
    private boolean noCheckboxSelected() {
        return !blockCheck.isSelected() &&
                !navigationCheck.isSelected() &&
                !disregardCheck.isSelected() &&
                !lowSpeedCheck.isSelected() &&
                !approachCheck.isSelected() &&
                !spadCheck.isSelected() &&
                !conflictCheck.isSelected() ;

    }

    private Set<RouteAction> createOperationsSet() {
        Set<RouteAction> operations = EnumSet.noneOf(RouteAction.class);
        if (navigationCheck.isSelected()) operations.add(RouteAction.NAVIGATE);
        if (lowSpeedCheck.isSelected()) operations.add(RouteAction.SET_LOW_SPEED);
        if (approachCheck.isSelected()) operations.add(RouteAction.SET_APPROACH_LOCK);
        if (spadCheck.isSelected()) operations.add(RouteAction.SPAD);
        if (blockCheck.isSelected()) operations.add(RouteAction.BLOCK);
        if (disregardCheck.isSelected()) operations.add(RouteAction.SET_DISREGARD_ON);
        if (conflictCheck.isSelected()) operations.add(RouteAction.CONFLICT);
        if (RouteCheck.isSelected()) operations.add(RouteAction.SET_ROUTE);
        return operations;
    }
    @FXML
    public void initialize()  {
        infoText.textProperty().bind(deps.getRouteService().textToUpdateProperty());
        conflictInfoText.textProperty().bind(deps.getRouteService().textConflictToUpdateProperty());

        RouteListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        RouteList = new ObservableListBase() {
            List<Route> Routes = deps.getRouteService().getRoutes();

            @Override
            public Object get(int index) {
                return Routes.get(index).getId();
            }

            @Override
            public int size() {
                return Routes.size();
            }
        };

        RouteListView.setItems(RouteList);

        RouteSearchTextField.setOnKeyReleased(event-> {
            RouteListView.setItems(RouteList.filtered(trackId -> trackId.toUpperCase().contains(RouteSearchTextField.getText().toUpperCase())));
            RouteListView.refresh();
        });


        RouteListView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends String> change) -> {
            pauseButton.setDisable(!(RouteListView.getSelectionModel().getSelectedItems().size() > 1));

        });


        // Bind actions to buttons

        navigationButton.setOnAction(event -> handleRouteAction(RouteAction.NAVIGATE));

        setRouteButton.setOnAction(event -> handleRouteAction(RouteAction.SET_ROUTE_DROP));
        unsetRouteButton.setOnAction(event -> handleRouteAction(RouteAction.UNSET_ROUTE_DROP));
        lowSpeedButton.setOnAction(event -> handleRouteAction(RouteAction.SET_LOW_SPEED));
        spadButton.setOnAction(event -> handleRouteAction(RouteAction.SPAD));
        approachButton.setOnAction(event -> handleRouteAction(RouteAction.SET_APPROACH_LOCK));
        disregardButton.setOnAction(event -> handleRouteAction(RouteAction.SET_DISREGARD_ON));
        blockButton.setOnAction(event -> handleRouteAction(RouteAction.BLOCK));
        conflictButton.setOnAction(event -> handleRouteAction(RouteAction.CONFLICT));
        testButton.setOnAction(event -> handleRouteAction(RouteAction.TEST));
        uploadButton.setOnAction(event ->  openFileChooser());

        buttonClose.setOnAction(event->{
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
                // Customize controller instance
                LoginController loginController =  new LoginController(stage, deps);

                loader.setController(loginController);
                Pane root = loader.load();

                loginController.showStage(root);

            } catch (IOException e) {
                logger.info(e.getMessage());
            }
        });

        buttonStop.setOnAction(event -> {

            stopRequested = true;
            System.out.println("Stop requested");
            System.out.println("Loop will stop after this function");
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

            RouteList = new ObservableListBase() {
                List<Route> Routes = deps.getRouteService().getUpdatedRoutes();

                @Override
                public Object get(int index) {
                    return Routes.get(index).getId();
                }

                @Override
                public int size() {
                    return Routes.size();
                }
            };

            RouteListView.setItems(RouteList);
        }

    }


    public void showStage(Pane root){
        Scene scene = new Scene(root,1000,800);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setTitle("Test Routes");
        stage.show();
    }

}
