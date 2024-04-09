import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.*;

import com.jcraft.jsch.JSchException;
import javafx.application.Application;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;

import model.Deps;
import controller.LoginController;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

public class Main extends Application {
	private Deps deps;

	@Override
	public void init() throws ParserConfigurationException, IOException, SAXException, InterruptedException, JSchException, AWTException {
		deps = new Deps();
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			deps.setup();
			Logger rootLogger = Logger.getLogger("");
			FileHandler fh;
			// Remove existing handlers
			Handler[] handlers = rootLogger.getHandlers();
			for (Handler handler : handlers) {
				rootLogger.removeHandler(handler);
			}


			// Create a new console handler
			Handler consoleHandler = new ConsoleHandler();
			consoleHandler.setLevel(Level.INFO);
			fh = new FileHandler("AutoWest.log");
			fh.setLevel(Level.INFO);
			System.setProperty(
					"java.util.logging.SimpleFormatter.format",
					"[%1$tF %1$tr] %3$s %4$s:  %5$s %n");
			fh.setFormatter(new SimpleFormatter());
			rootLogger.addHandler(fh);


			// Add the console handler to the root logger
			rootLogger.addHandler(consoleHandler);
			//rootLogger.setUseParentHandlers(false);

			// Set the logging level for the root logger
			rootLogger.setLevel(Level.INFO);
			//rootLogger.info("The similarity score has been set to "+AppConfig.getProperty(" score.similarityScore"));


			FXMLLoader loader = new FXMLLoader(getClass().getResource("view/LoginView.fxml"));

			// Customize controller instance
			LoginController loginController = new LoginController(primaryStage, deps);

			loader.setController(loginController);

			VBox root = loader.load();

			loginController.showStage(root);
		} catch (IOException | SQLException | RuntimeException e) {
			Scene scene = new Scene(new Label(e.getMessage()), 200, 100);
			primaryStage.setTitle("Error");
			primaryStage.setScene(scene);
			primaryStage.show();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
