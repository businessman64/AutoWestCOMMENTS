package controller;

import java.io.IOException;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.Deps;
import model.View;
import model.ViewManager;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

public class LoginController {

	@FXML
	private Button tracks;

	@FXML
	private Button routes;

	@FXML
	private Button points;

	@FXML
	private Button signals;

	@FXML
	private Button buttonConfigureScreen;
	private Deps deps;
	private Stage stage;
	private static final Logger logger = Logger.getLogger(LoginController.class.getName());

	View currentView;
	@FXML
	private Button buttonConfigureView;
	
	public LoginController(Stage stage, Deps deps) {
		this.stage = stage;
		this.deps = deps;
	}
	
	@FXML
	public void initialize() {
		currentView = ViewManager.getInstance().getCurrentView();
		tracks.setOnAction(event -> {
			// Redirect to tracks page
			if (currentView != null) {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TrackView.fxml"));
				TrackController trackController = null;

				try {
					logger.info("---------------------------------Tracks---------------------------------");

					trackController = new TrackController(stage, deps);
				} catch (ParserConfigurationException e) {
					throw new RuntimeException(e);
				} catch (IOException e) {
					throw new RuntimeException(e);
				} catch (SAXException e) {
					throw new RuntimeException(e);
				}
				loader.setController(trackController);
				HBox root = null;
				try {
					root = loader.load();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				trackController.showStage(root);
				//stage.close();
			}else{
				getConfigurationScreen();
			}
		});
//		buttonConfigureScreen.setOnAction(event->{
//			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ScreenConfigurationView.fxml"));
//			try {
//				ScreenConfigurationController screenConfigurationController = new ScreenConfigurationController(stage, deps);
//				loader.setController(screenConfigurationController);
//				Pane root = loader.load();
//				screenConfigurationController.showStage(root);
//			} catch (ParserConfigurationException e) {
//				throw new RuntimeException(e);
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			} catch (SAXException e) {
//				throw new RuntimeException(e);
//			}
//
//
//		});

		buttonConfigureView.setOnAction(event->{getConfigurationScreen();});

		points.setOnAction(event -> {
			// Redirect to tracks page
			if (currentView != null) {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/PointView.fxml"));
				PointController pointController = null;
				try {
					logger.info("---------------------------------Points---------------------------------");

					pointController = new PointController(stage, deps);
				} catch (ParserConfigurationException e) {
					throw new RuntimeException(e);
				} catch (IOException e) {
					throw new RuntimeException(e);
				} catch (SAXException e) {
					throw new RuntimeException(e);
				}
				loader.setController(pointController);
				HBox root = null;
				try {
					root = loader.load();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				pointController.showStage(root);
				//stage.close();
			}else{
				getConfigurationScreen();

			}
		});

		signals.setOnAction(event ->{
			// Redirect to tracks page
			if (currentView != null) {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SignalView.fxml"));
				SignalController signalController = null;
				try {
					logger.info("---------------------------------Signals---------------------------------");

					signalController = new SignalController(stage, deps);
				} catch (ParserConfigurationException e) {
					throw new RuntimeException(e);
				} catch (IOException e) {
					throw new RuntimeException(e);
				} catch (SAXException e) {
					throw new RuntimeException(e);
				}
				loader.setController(signalController);
				HBox root = null;
				try {
					root = loader.load();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				signalController.showStage(root);
				//stage.close();
			}else{
				getConfigurationScreen();

			}
		});

		routes.setOnAction(event -> {
			// Redirect to tracks page
			if (currentView != null) {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RouteView.fxml"));
				RouteController routeController = null;
				try {
					logger.info("---------------------------------Routes---------------------------------");
					routeController = new RouteController(stage, deps);
				} catch (ParserConfigurationException e) {
					throw new RuntimeException(e);
				} catch (IOException e) {
					throw new RuntimeException(e);
				} catch (SAXException e) {
					throw new RuntimeException(e);
				}
				loader.setController(routeController);
				HBox root = null;
				try {
					root = loader.load();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				routeController.showStage(root);
			}else{
				getConfigurationScreen();

			}
			//stage.close();
		});

	}

	public void getConfigurationScreen(){
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ConfigureScreenView.fxml"));

		try {
			ConfigureScreenController configureScreenController = new ConfigureScreenController(stage, deps);
			loader.setController(configureScreenController);
			Pane root = loader.load();
			configureScreenController.showStage(root);

		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		}
	}
	public void showStage(Pane root) {
		Scene scene = new Scene(root, 600, 700);
		stage.setScene(scene);
		stage.setResizable(true);
		stage.setTitle("" +
				"AutoWEST");
		stage.show();
	}
}

