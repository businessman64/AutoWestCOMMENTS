package controller;

import Service.ScreenService;
import Service.ViewService;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.Deps;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;

public class ScreenConfigurationController {
    @FXML
    ComboBox<Integer> comboBoxWestall;
    @FXML
    ComboBox<Integer> comboBoxSpringvale;
    @FXML
    ComboBox<Integer> comboBoxDandenong;
    @FXML
    ComboBox<Integer> comboBoxPakenham;
    @FXML
    ComboBox<Integer> comboBoxCranbourne;
    @FXML
    ComboBox<Integer> comboBoxPeds;
    @FXML
    Button buttonApply;

    @FXML
    Button buttonClose;

    @FXML
    Label labelResult;
    private Deps deps;
    private Stage stage;


    public ScreenConfigurationController(Stage stage, Deps deps) throws ParserConfigurationException, IOException, SAXException {
        this.stage = stage;
        this.deps = deps;
    }
    @FXML
    public void initialize(){
        int count = ScreenService.getInstance().getNumberOfScreens();
        ArrayList<Integer> optionsList = new ArrayList<>();
        for(int i=0;i<count;i++){
            optionsList.add(i);
        }
        ObservableList obl = new ObservableListBase() {
            @Override
            public Object get(int index) {
                return optionsList.get(index);
            }
            @Override
            public int size() {
                return count;
            }
        };
        comboBoxWestall.setValue(ViewService.getInstance().getScreenNumberByViewName("WESTALL"));
        comboBoxSpringvale.setValue(ViewService.getInstance().getScreenNumberByViewName("SPRINGVALE"));
        comboBoxDandenong.setValue(ViewService.getInstance().getScreenNumberByViewName("DANDENONG"));
        comboBoxPakenham.setValue(ViewService.getInstance().getScreenNumberByViewName("PAKENHAM"));
        comboBoxCranbourne.setValue(ViewService.getInstance().getScreenNumberByViewName("CRANBOURNE"));
        comboBoxPeds.setValue(ViewService.getInstance().getScreenNumberByViewName("PEDS"));

        comboBoxWestall.setItems(obl);
        comboBoxSpringvale.setItems(obl);
        comboBoxDandenong.setItems(obl);
        comboBoxPakenham.setItems(obl);
        comboBoxCranbourne.setItems(obl);
        comboBoxPeds.setItems(obl);

        comboBoxWestall.setOnAction(event -> {
            this.labelResult.setVisible(false);
        });
        comboBoxSpringvale.setOnAction(event -> {
            this.labelResult.setVisible(false);
        });
        comboBoxDandenong.setOnAction(event -> {
            this.labelResult.setVisible(false);
        });
        comboBoxPakenham.setOnAction(event -> {
            this.labelResult.setVisible(false);
        });
        comboBoxCranbourne.setOnAction(event -> {
            this.labelResult.setVisible(false);
        });
        comboBoxPeds.setOnAction(event -> {
            this.labelResult.setVisible(false);
        });

        buttonApply.setOnAction(event->{
            int selectedIndex= comboBoxWestall.getSelectionModel().getSelectedItem();
            ViewService.getInstance().changeScreenByView("WESTALL",selectedIndex);
            System.out.println(ViewService.getInstance().getScreenNumberByViewName("WESTALL"));

            selectedIndex= comboBoxSpringvale.getSelectionModel().getSelectedItem();
            ViewService.getInstance().changeScreenByView("SPRINGVALE",selectedIndex);

            selectedIndex= comboBoxDandenong.getSelectionModel().getSelectedItem();
            ViewService.getInstance().changeScreenByView("DANDENONG",selectedIndex);

            selectedIndex= comboBoxPakenham.getSelectionModel().getSelectedItem();
            ViewService.getInstance().changeScreenByView("PAKENHAM",selectedIndex);

            selectedIndex= comboBoxCranbourne.getSelectionModel().getSelectedItem();
            ViewService.getInstance().changeScreenByView("CRANBOURNE",selectedIndex);

            selectedIndex= comboBoxPeds.getSelectionModel().getSelectedItem();
            ViewService.getInstance().changeScreenByView("PEDS",selectedIndex);

            this.labelResult.setVisible(true);

        });

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


    }
    public void showStage(Pane root){
        Scene scene = new Scene(root,600,450);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setTitle("Screen Configuration");
        stage.show();
    }

}
