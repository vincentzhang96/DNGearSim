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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.*;

public class Bootstrap extends Application {

    private static final Logger LOGGER = Logger.getLogger("Launcher");
    private static final DateFormat LOGGER_DATE_FORMAT =
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

    private static final Path APP_HOME;

    static {
        APP_HOME = Paths.get("").toAbsolutePath();
        LOGGER.setLevel(Level.FINEST);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return String.format("[%7s] %s%n%s%n",
                        record.getLevel().getLocalizedName(),
                        LOGGER_DATE_FORMAT.format(new Date(record.getMillis())),
                        record.getMessage());
            }
        });
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
    }
    private Stage mainStage;
    private Scene mainScene;
    private BootstrapUiController bootstrapUiController;
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
            root = createBasicErrorUi(e);
        }
        mainScene = new Scene(root);
        mainStage.initStyle(StageStyle.TRANSPARENT);
        mainStage.setTitle("DN Gear Sim");
        mainStage.setResizable(false);
        mainStage.setAlwaysOnTop(true);
        mainStage.setScene(mainScene);
        mainStage.show();
        //  If no error, continue
        if (bootstrapUiController != null) {
            bootstrapUiController.setOnExitButtonAction(this::stopApp);
            runBootstrapTask();
        }
    }

    private Parent loadUi() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/co/phoenixlab/dn/dngearsim/bootstrap/fxml/splash.fxml"));
        Parent root = loader.load();
        bootstrapUiController = loader.getController();
        return root;
    }

    private Parent createBasicErrorUi(Throwable t) {
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

    private void onBootstrapOk(BootstrapHandoff handoff) {
        handoff.handOff(this, mainStage, mainScene);
    }

    private void onBootstrapSelfUpdateRequired() {
        //  TODO
    }

    private void onBootstrapFailed(String error) {
        showErrorWindow(error);
    }

    private void showErrorWindow(String errorMsg) {
        Stage errorStage = new Stage(StageStyle.TRANSPARENT);
        errorStage.initOwner(mainStage);
        errorStage.initModality(Modality.WINDOW_MODAL);
        errorStage.setTitle("DN Gear Sim");
        errorStage.setResizable(false);
        errorStage.setAlwaysOnTop(true);
        Parent root;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/co/phoenixlab/dn/dngearsim/bootstrap/fxml/error.fxml"));
            root = loader.load();
            ErrorWindowController controller = loader.getController();
            controller.setOnExitPressed(this::stopApp);
            controller.setOnRetryPressed(() -> {
                errorStage.close();
                retryBootstrap();
            });
            controller.setErrorMessage(errorMsg);
        } catch (Exception e) {
            e.printStackTrace();
            root = createBasicErrorUi(e);
        }
        Scene scene = new Scene(root);
        errorStage.setScene(scene);
        errorStage.show();
    }

    private void retryBootstrap() {
        bootstrapTask.cancel(true);
        bootstrapUiController.unbind();
        runBootstrapTask();
    }

    private void runBootstrapTask() {
        bootstrapTask = new BootstrapTask(this::onBootstrapOk, this::onBootstrapSelfUpdateRequired,
                this::onBootstrapFailed);
        bootstrapUiController.bindToTask(bootstrapTask);
        Thread thread = new Thread(bootstrapTask, "BootstrapTask");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void stop() {
        cleanUp();
    }

    private void cleanUp() {
        if (bootstrapUiController != null) {
            bootstrapUiController.unbind();
        }
        if (bootstrapTask != null) {
            bootstrapTask.cancel(true);
        }
    }

    private void stopApp() {
        Platform.exit();
    }
}
