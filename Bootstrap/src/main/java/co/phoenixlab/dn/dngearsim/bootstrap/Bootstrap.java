package co.phoenixlab.dn.dngearsim.bootstrap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.lang.reflect.Method;

public class Bootstrap extends Application {

    private Stage mainStage;
    private Scene mainScene;
    private BootstrapUi bootstrapUi;
    private BootstrapTask bootstrapTask;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Platform.setImplicitExit(false);
        mainStage = primaryStage;
        Parent root;
        try {
            root = loadUi();
        } catch (Exception e) {
            e.printStackTrace();
            root = loadErrorUi(e);
        }
        mainScene = new Scene(root);
        mainStage.initStyle(StageStyle.TRANSPARENT);
        mainStage.setTitle("DN Gear Sim");
        mainStage.setResizable(false);
        mainStage.setAlwaysOnTop(true);
        mainStage.setScene(mainScene);
        mainStage.show();
        //  If no error, continue
        if (bootstrapUi != null) {
            bootstrapUi.setOnExitButtonAction(this::stopApp);
            bootstrapTask =  new BootstrapTask(this::onBootstrapOk, this::onBootstrapSelfUpdateRequired,
                    this::onBootstrapFailed);
            bootstrapUi.bindToTask(bootstrapTask);
            Thread thread = new Thread(bootstrapTask, "BootstrapTask");
            thread.setDaemon(true);
            thread.start();
        }
    }

    private Parent loadUi() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/co/phoenixlab/dn/dngearsim/bootstrap/fxml/splash.fxml"));
        Parent root = loader.load();
        bootstrapUi = loader.getController();
        return root;
    }

    private Parent loadErrorUi(Throwable t) {
        VBox root = new VBox(20);
        Label errorLbl = new Label("Fatal error while starting");
        errorLbl.setFont(Font.font(24));
        Label detailLbl = new Label(t.toString());
        Button exitBtn = new Button("Close");
        exitBtn.setOnAction(ae -> stopApp());
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(errorLbl, detailLbl, exitBtn);
        root.setPadding(new Insets(10));
        return root;
    }

    private void onBootstrapOk(Method transferMethod) {
        //  TODO
    }

    private void onBootstrapSelfUpdateRequired() {
        //  TODO
    }

    private void onBootstrapFailed(String error) {
        //  TODO
    }

    @Override
    public void stop() {
        cleanUp();
    }

    private void cleanUp() {
        if (bootstrapUi != null) {
            bootstrapUi.unbind();
        }
        if (bootstrapTask != null) {
            bootstrapTask.cancel(true);
        }
    }

    private void stopApp() {
        Platform.exit();
    }
}
