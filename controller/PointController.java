package controller;

import Service.PointService;
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
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;


public class PointController {
    @FXML
    ListView<String> pointListView;

    @FXML
    TextField pointSearchTextField;

    @FXML
    Button configureButton;

    @FXML
    CheckBox normalCheckbox;

    @FXML
    CheckBox reverseCheckbox;

    @FXML
    CheckBox checkCheckbox;
    @FXML
    Button testButton;

    @FXML
    Button buttonClose;

    @FXML
    Button normaliseButton;

    @FXML
    Button reverseButton;
    @FXML
    Button centreButton;

    @FXML
    Button blockButton;

    @FXML
    Button unblockButton;

    @FXML
    Button checkButton;
    @FXML
    Button uploadButton;
    @FXML
    Button pauseButton;
    @FXML
    Label controlNotificationLabel;
    File file = null;
//    @FXML
//    Label testNotificationLabel;

    private Deps deps;
    Set<PointAction> operations;
    private Stage stage;
    private boolean paused = false;
    private  ObservableList<String> pointList;
    private static final Logger logger = Logger.getLogger(PointController.class.getName());
    public enum PointAction {
        NORMALIZE,
        REVERSE,
        BLOCK,
        UNBLOCK,
        CHECK,

        CONFIGURE,
        TEST
        // Add any other point-specific actions here
    }

    public PointController(Stage stage, Deps deps) throws ParserConfigurationException, IOException, SAXException {
        this.stage = stage;
        this.deps = deps;
    }
    private void handleTestAction(PointAction action, String point, boolean bitPKBool,boolean bitBLKBool){
        controlNotificationLabel.setText("");
        try {
            switch (action) {
                case CONFIGURE:
                    deps.getPointService().configurePointById(point);
                    break;
                case CHECK:
                    deps.getPointService().CheckPointById(point);
                    break;
                case NORMALIZE:
                    this.deps.getPointService().normalisePointById(point,bitPKBool,bitBLKBool);
                    Thread.sleep(8000);
                    this.deps.getPointService().centralisePoint(point,true,bitPKBool);
                    break;
                case REVERSE:
                    this.deps.getPointService().reversePointById(point,bitPKBool,bitBLKBool);
                    Thread.sleep(8000);
                    this.deps.getPointService().centralisePoint(point,true,bitPKBool);
                    break;
            }
        }catch (Exception e) {
            logger.info(e.getMessage());
        }
    }
    private void handleButtonAction(PointAction action) {
        controlNotificationLabel.setText("");
        //testNotificationLabel.setText("");
        ObservableList<String> selectedPointsList = pointListView.getSelectionModel().getSelectedItems();

        new Thread(() -> {
            for (String o : selectedPointsList) {
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
                    String bitPK = "([A-Z]PK)";
                    String bitBLK="([A-Z]?BLK)";
                    deps.getPointService().initialHouseKeeping(o);
                    boolean bitBLKBool = deps.getPointService().extractMnemonic(o,bitBLK);
                    boolean bitPKBool = deps.getPointService().extractMnemonic(o,bitPK);
                    switch (action) {
                        case CONFIGURE:
                            deps.getPointService().configurePointById(o);
                            break;
                        case CHECK:
                            deps.getPointService().CheckPointById(o);
                            break;
                        case NORMALIZE:
                            this.deps.getPointService().normalisePointById(o,bitPKBool,bitBLKBool);
                            Thread.sleep(8000);

                            this.deps.getPointService().centralisePoint(o, true,bitPKBool);
                            break;
                        case REVERSE:
                            this.deps.getPointService().reversePointById(o,bitPKBool,bitBLKBool);
                            Thread.sleep(8000);
                            this.deps.getPointService().centralisePoint(o, true,bitPKBool);
                            break;
                        case TEST:

                            if (noCheckboxSelected()) {
                                Platform.runLater(() ->
                                        controlNotificationLabel.setText("Please select at least one checkbox option"));
                            }
                            else {
                                operations = createOperationsSet();
                                for (PointAction eachAction : operations ) {

                                    handleTestAction(eachAction,o,bitPKBool,bitBLKBool);
                                    Thread.sleep(5000);
                                }
//
                            }
                            break;

                        default:
                            break;
                    }
                    Platform.runLater(() -> {
                        if (selectedPointsList.size() > 1) {
                            controlNotificationLabel.setText("Pause Now");
                        }
                    });
                    Thread.sleep(5000);

                } catch (Exception e) {
                    Platform.runLater(() -> controlNotificationLabel.setText("Error: " + e.getMessage()));
                    logger.info(e.getMessage());
                }
            }

            Platform.runLater(() -> {
                controlNotificationLabel.setText("");
            });
            pauseButton.setDisable(true);
        }).start();
    }
        private Set<PointAction> createOperationsSet() {
        Set<PointAction> operations = EnumSet.noneOf(PointAction.class);
        if (normalCheckbox.isSelected()) operations.add(PointAction.NORMALIZE);
        if (reverseCheckbox.isSelected()) operations.add(PointAction.REVERSE);
        if (checkCheckbox.isSelected()) operations.add(PointAction.CHECK);

        return operations;
        }

    private boolean noCheckboxSelected() {
        return !normalCheckbox.isSelected() &&
                !reverseCheckbox.isSelected() &&
                !checkCheckbox.isSelected()
               ;

    }
    @FXML
    public void initialize() throws ParserConfigurationException, IOException, SAXException, InterruptedException {
        pointListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        pointList = new ObservableListBase() {
            List<Point> points = PointService.getInstance().getPoints();

            @Override
            public Object get(int index) {
                return points.get(index).getId();
            }
            @Override
            public int size() {
                return points.size();
            }
        };
        pointListView.setItems(pointList);


        pointSearchTextField.setOnKeyReleased(event-> {
            pointListView.setItems(pointList.filtered(trackId -> trackId.toUpperCase().contains(pointSearchTextField.getText().toUpperCase())));
            pointListView.refresh();
        });

        pointListView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends String> change) -> {
            pauseButton.setDisable(!(pointListView.getSelectionModel().getSelectedItems().size() > 1));

        });



        // Bind actions to buttons
        configureButton.setOnAction(event -> handleButtonAction(PointAction.CONFIGURE));
        normaliseButton.setOnAction(event -> handleButtonAction(PointAction.NORMALIZE));
        reverseButton.setOnAction(event -> handleButtonAction(PointAction.REVERSE));
        checkButton.setOnAction(event -> handleButtonAction(PointAction.CHECK));
        uploadButton.setOnAction(event ->  openFileChooser());
        testButton.setOnAction(event -> handleButtonAction(PointAction.TEST));
        // Add bindings for other buttons here

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

            pointList = new ObservableListBase() {
                List<Point> Points = deps.getPointService().getUpdatedPoint();

                @Override
                public Object get(int index) {
                    return Points.get(index).getId();
                }

                @Override
                public int size() {
                    return Points.size();
                }
            };

            pointListView.setItems(pointList);
        }

    }
    public void showStage(Pane root){
        Scene scene = new Scene(root,800,600);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setTitle("Test Points");
        stage.show();
    }

}



//
//    @FXML
//    public void initialize() throws ParserConfigurationException, IOException, SAXException {
//        this.pointListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//
//        pointList = new ObservableListBase() {
//            List<Point> points = PointService.getInstance().getPoints();
//
//            @Override
//            public Object get(int index) {
//                return points.get(index).getId();
//            }
//            @Override
//            public int size() {
//                return points.size();
//            }
//        };
//
//        pointListView.setItems(pointList);
//
//        pointSearchTextField.setOnKeyReleased(event->{
//            pointListView.setItems(pointList.filtered(pointId-> pointId.contains(pointSearchTextField.getText())));
//            pointListView.refresh();
//        });
//
//        normaliseButton.setOnAction(event ->{
//            controlNotificationLabel.setText("");
//            testNotificationLabel.setText("");
//            ObservableList selectedTracksList= pointListView.getSelectionModel().getSelectedItems();
//            for(Object o :selectedTracksList){
//                try {
//                        this.deps.getPointService().normalisePointById((String) o);
//                        Thread.sleep(8000);
//                        this.deps.getPointService().centralisePoint((String)o);
//                        //this.trackService.blockTrackByIndex((Integer) o);
//
//                } catch (Exception e) {
//                    controlNotificationLabel.setText(e.getMessage());
//                    logger.info("The request for point "+o+" failed."+e.getMessage());
//                }
//            }
//        });
//       /* centreButton.setOnAction(event ->{
//            controlNotificationLabel.setText("");
//            testNotificationLabel.setText("");
//            ObservableList selectedTracksList= pointListView.getSelectionModel().getSelectedItems();
//            for(Object o :selectedTracksList){
//                try {
//                    if(this.deps.getPointService().preCheckControl(PointOperations.CENTRE,(String)o)) {
//                        List list = this.deps.getPointService().centrePointById((String) o);
//                        Thread.sleep(8000);
//                        this.deps.getPointService().centralisePoint((Screen) list.get(2));
//                        Thread.sleep(8000);
//                        this.deps.getPointService().closePointWindow((Screen) list.get(2));
//                        //this.trackService.blockTrackByIndex((Integer) o);
//                    }else{
//                        controlNotificationLabel.setText("The point is already in normal state");
//                        logger.info("The point is already in normal state "+o);
//                    }
//                } catch (Exception e) {
//                    controlNotificationLabel.setText(e.getMessage());
//                    logger.info("The request for point "+o+" failed."+e.getMessage());
//                }
//            }
//        });*/
//        reverseButton.setOnAction(event ->{
//            controlNotificationLabel.setText("");
//            testNotificationLabel.setText("");
//            ObservableList selectedTracksList= pointListView.getSelectionModel().getSelectedItems();
//            for(Object o :selectedTracksList){
//
//                try {
//                        this.deps.getPointService().reversePointById((String) o);
//                        Thread.sleep(8000);
//                        this.deps.getPointService().closePointWindow((String) o);
//
//                } catch (Exception e) {
//                    controlNotificationLabel.setText("The request for point "+o+" failed."+e.getMessage());
//                    logger.info("The request for point "+o+" failed."+e.getMessage());
//                }
//            }
//        });
//
//        testButton.setOnAction(event->{
//            testNotificationLabel.setText("");
//            controlNotificationLabel.setText("");
//            ObservableList selectedPointsList= pointListView.getSelectionModel().getSelectedItems();
//            if (selectedPointsList!=null){
//                Set operations = new LinkedHashSet<TrackOperations>();
//
//                for(Object o :selectedPointsList){
//                        if (!normalCheckbox.isSelected() && !reverseCheckbox.isSelected()) {
//                            testNotificationLabel.setText("Please select atleast one checkbox option");
//                            return;
//                        } else {
//                            if (reverseCheckbox.isSelected()) {
//                                //this.trackService.testDisregardedTrack((String) o);
//                                operations.add(PointOperations.REVERSE);
//                            }
//                            if (normalCheckbox.isSelected()) {
//                                //this.trackService.testBlockedTrack((String)o);
//                                operations.add(PointOperations.NORMALISE);
//                            }
//                        }
//                        logger.info("\nThe Test Suite Started.\n");
//                        this.deps.getPointService().testPoint(operations, (String) o);
//                        logger.info("The Test Suite completed");
//                }
//            }else{
//                testNotificationLabel.setText("Please select atleast one point");
//            }
//        });
//        configureButton.setOnAction(event->{
//            ObservableList selectedPointsList= pointListView.getSelectionModel().getSelectedItems();
//            for(Object o :selectedPointsList){
//                try {
//                    this.deps.getPointService().configurePointById((String) o);
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
//
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
//        stage.setTitle("Test Points");
//        stage.show();
//    }
//
//}
