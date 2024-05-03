package controller;

import Service.PointOperations;
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
import javafx.stage.Stage;
import model.Deps;
import model.Point;
import model.Track;
import model.View;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Screen;
import org.xml.sax.SAXException;
import util.XMLParser;

import javax.xml.parsers.ParserConfigurationException;
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
    Button pauseButton;
    @FXML
    Label controlNotificationLabel;

    @FXML
    Label testNotificationLabel;

    private Deps deps;
    private Stage stage;
    private boolean paused = false;
    private  ObservableList<String> pointList;
    private static final Logger logger = Logger.getLogger(PointController.class.getName());
    public enum PointAction {
        NORMALIZE,
        REVERSE,
        BLOCK,
        UNBLOCK,
        CONFIGURE,
        TEST
        // Add any other point-specific actions here
    }

    public PointController(Stage stage, Deps deps) throws ParserConfigurationException, IOException, SAXException {
        this.stage = stage;
        this.deps = deps;
    }

    private void handleButtonAction(PointAction action) {
        controlNotificationLabel.setText("");
        testNotificationLabel.setText("");
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

                    switch (action) {
                        case CONFIGURE:
                            deps.getPointService().configurePointById(o);
                            break;
                        case NORMALIZE:
                            this.deps.getPointService().normalisePointById(o);
                            Thread.sleep(8000);
                            this.deps.getPointService().centralisePoint(o);
                        case REVERSE:
                            this.deps.getPointService().reversePointById(o);
                            Thread.sleep(8000);
                            this.deps.getPointService().closePointWindow(o);

                        default:
                            break;
                    }
                    Platform.runLater(() -> {
                        if (selectedPointsList.size() > 1) {
                            controlNotificationLabel.setText("Pause Now, Or cry forever");
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

    @FXML
    public void initialize() throws ParserConfigurationException, IOException, SAXException {
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
