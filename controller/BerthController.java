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
import model.*;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;


import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class BerthController {
    @FXML
    ListView<String> berthListView;

    @FXML
    TextField berthSearchTextField;

    @FXML
    Button configureButton;

    @FXML
    Button buttonSpadDrop;
    @FXML
    Button testButton;

    @FXML
    Button buttonClose;
    @FXML
    Button uploadButton;

    @FXML
    Button buttonTDNInsert;
    @FXML
    CheckBox checkInsert;

    @FXML
    Button buttonTDNErase;
    @FXML
    CheckBox checkErase;


    @FXML
    Button buttonTDNEraseBerth;
    @FXML
    CheckBox checkEraseBerth;


    @FXML
    Button buttonTDNMove;
    @FXML
    CheckBox checkMove;

    @FXML
    Button buttonTDNExchange;
    @FXML
    CheckBox checkExchange;

    @FXML
    Button buttonPick;

    @FXML
    Label controlNotificationLabel;

    @FXML
    Label testNotificationLabel;

    @FXML
    CheckBox checkTwoBerth;

    @FXML
    Button pauseButton;

    @FXML
    Button buttonTwoBerth;

    @FXML
    Button buttonRemove;
@FXML
Button buttonTestAll;

    private Deps deps;
    private Stage stage;
    boolean one_by_one;

    boolean isEraseBySignal;
    File file = null;
    private  ObservableList<String> signalList;

    public enum BerthAction {

        INSERT,
        ERASE_SIGNAL,
        MOVE,

        EXCHANGE,
        ERASE_BERTH,
        TWO_TNE,

        TWO_DROP,

        TWO_REMOVE,
        ONE_BY_ONE,

        TWO_PICK,
        TEST
        // Add other signal-specific actions here
    }
    private boolean paused = false;
    private static final Logger logger = Logger.getLogger(SignalController.class.getName());


    public BerthController(Stage stage, Deps deps) throws ParserConfigurationException, IOException, SAXException {
        this.stage = stage;
        this.deps = deps;
    }

    private void handleTestAction(BerthAction action, String signal){
        controlNotificationLabel.setText("");
        testNotificationLabel.setText("");
        try {
            switch (action) {
                case INSERT:
                    deps.getBerthService().insertBerthBySignalID(signal);
                    break;
                case ERASE_SIGNAL:
                    isEraseBySignal = true;
                    deps.getBerthService().eraseTDNBySignalID(signal, isEraseBySignal);
                    break;
                case ERASE_BERTH:
                    isEraseBySignal = false;
                    deps.getBerthService().eraseTDNBySignalID(signal, isEraseBySignal);
                    break;
                case MOVE:
                    deps.getBerthService().moveTDNBySignalID(signal);

                    break;
                case EXCHANGE:
                    deps.getBerthService().exchangeTDNBySignalID(signal);

                    break;
            }
            // deps.getRouteService().cleanRouteByID(route);
        }catch (Exception e) {
            logger.info(e.getMessage());
        }
    }
    private void handleSignalAction(BerthAction action) {
        controlNotificationLabel.setText("");
        testNotificationLabel.setText("");
        ObservableList<String> selectedSignalsList = berthListView.getSelectionModel().getSelectedItems();
        new Thread(() -> {
            for (String o : selectedSignalsList) {
                System.out.println("---------------"+o+"-----------------");

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

                        case INSERT:
                            deps.getBerthService().insertBerthBySignalID(o);
                            break;
                        case ERASE_SIGNAL:
                            isEraseBySignal=true;
                            deps.getBerthService().eraseTDNBySignalID(o,isEraseBySignal);
                            break;
                        case ERASE_BERTH:
                            isEraseBySignal=false;
                            deps.getBerthService().eraseTDNBySignalID(o,isEraseBySignal);
                            break;
                        case MOVE:
                            deps.getBerthService().moveTDNBySignalID(o);

                            break ;
                        case EXCHANGE:
                            deps.getBerthService().exchangeTDNBySignalID(o);

                            break ;
                        case ONE_BY_ONE:
                            one_by_one= true;
                            System.out.println(one_by_one);
                            deps.getBerthService().dropAllTracks(o);
                            Thread.sleep(1000);
                            System.out.println("tracks dropped");
                            deps.getBerthService().dropSPADTracks(o,one_by_one);
                            Thread.sleep(25000);
                            System.out.println("spad track dropped");
                            deps.getBerthService().pickTrack(o,one_by_one);
                            Thread.sleep(4000);
                            System.out.println("track picked and berth removed");
                            deps.getBerthService().RemoveBerthSignal(o);
                            System.out.println("signal removed");
                            break;
                        case TWO_TNE:
                            deps.getBerthService().dropAllTracks(o);
                            break;
                        case TWO_DROP:
                            one_by_one=false;
                            deps.getBerthService().dropSPADTracks(o,one_by_one);
                            break;
                        case TWO_REMOVE:
                            deps.getBerthService().RemoveBerthSignal(o);
                            break;
                        case TWO_PICK:
                            one_by_one=false;
                            deps.getBerthService().pickTrack(o,one_by_one);
                            break;
                        case TEST:
                            if (noCheckboxSelected()) {
                                Platform.runLater(() ->
                                        testNotificationLabel.setText("Please select at least one checkbox option"));
                            }
                            else {
                                Set<BerthAction> operations = createOperationsSet();
                                {

                                    for (BerthController.BerthAction eachAction : operations ) {

                                        handleTestAction(eachAction,o);

                                        Thread.sleep(5000);
                                    }

                                }
                                break;
                            }
                            break;
                        default:
                            break;
                    }
                    Platform.runLater(() -> {
                        if (selectedSignalsList.size() > 1) {
                            controlNotificationLabel.setText("Pause Now");
                            BerthSignal.removeAllInstances();
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
        return !checkInsert.isSelected() &&
                !checkErase.isSelected() &&
                !checkEraseBerth.isSelected() &&
                !checkMove.isSelected() &&
                !checkExchange.isSelected()  ;

    }

    private Set<BerthAction> createOperationsSet() {
        Set<BerthAction> operations = EnumSet.noneOf(BerthAction.class);
       // if (checkTwoBerth.isSelected()) operations.add(BerthAction.TWO_TNE);
        if(checkInsert.isSelected()) operations.add(BerthAction.INSERT);
        if(checkErase.isSelected())operations.add(BerthAction.ERASE_SIGNAL);
        if(checkEraseBerth.isSelected())operations.add(BerthAction.ERASE_BERTH);
        if(checkMove.isSelected())operations.add(BerthAction.MOVE);
        if(checkExchange.isSelected())operations.add(BerthAction.EXCHANGE);

        return operations;
    }
    @FXML
    public void initialize() {
        berthListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

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
        berthListView.setItems(signalList);

        berthSearchTextField.setOnKeyReleased(event-> {
            berthListView.setItems(signalList.filtered(trackId -> trackId.toUpperCase().contains(berthSearchTextField.getText().toUpperCase())));
            berthListView.refresh();
        });

        berthListView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends String> change) -> {
            pauseButton.setDisable(!(berthListView.getSelectionModel().getSelectedItems().size() > 1));

        });


        // Bind actions to buttons
        buttonTwoBerth.setOnAction(event -> handleSignalAction(BerthAction.TWO_TNE));
        buttonSpadDrop.setOnAction(event -> handleSignalAction(BerthAction.TWO_DROP));
        buttonRemove.setOnAction(event -> handleSignalAction(BerthAction.TWO_REMOVE));
        buttonPick.setOnAction(event -> handleSignalAction(BerthAction.TWO_PICK));
        testButton.setOnAction(event -> handleSignalAction(BerthAction.TEST));
        buttonTDNInsert.setOnAction(event -> handleSignalAction(BerthAction.INSERT));
        buttonTDNErase.setOnAction(event -> handleSignalAction(BerthAction.ERASE_SIGNAL));
        buttonTDNMove.setOnAction(event -> handleSignalAction(BerthAction.MOVE));
        buttonTDNExchange.setOnAction(event -> handleSignalAction(BerthAction.EXCHANGE));
        uploadButton.setOnAction(event ->  openFileChooser());
        buttonTestAll.setOnAction(event -> handleSignalAction(BerthAction.ONE_BY_ONE));
//                SET_ROUTE,
//                UNSET_ROUTE,
//                TEST,
        //configureButton.setOnAction(event -> handleSignalAction(BerthAction.CONFIGURE));
//        testButton.setOnAction(event -> handleSignalAction(BerthAction.TEST));
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

            signalList= new ObservableListBase() {
                List<Signal> Signal = deps.getBerthService().getUpdatedSignal();

                @Override
                public Object get(int index) {
                    return Signal.get(index).getId();
                }

                @Override
                public int size() {
                    return Signal.size();
                }
            };

            berthListView.setItems(signalList);
        }

    }


    public void showStage(Pane root){
        Scene scene = new Scene(root,1000,800);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setTitle("Test Signals");
        stage.show();
    }

}