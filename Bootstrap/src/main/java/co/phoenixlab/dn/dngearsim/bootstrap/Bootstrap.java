/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Vincent Zhang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package co.phoenixlab.dn.dngearsim.bootstrap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.*;

public class Bootstrap extends Application {

    public static final Logger LOGGER = Logger.getLogger("Launcher");
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
    private ResourceBundle localeBundle;

    private boolean startOk;
    private Throwable initThrowable;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        try {
            localeBundle = ResourceBundle.getBundle("co.phoenixlab.dn.dngearsim.bootstrap.locale.bootstrap",
                    Locale.forLanguageTag(System.getProperty("dngs.locale", "en_US")));
        } catch (Exception e) {
            initThrowable = e;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        Platform.setImplicitExit(false);
        Thread.setDefaultUncaughtExceptionHandler(this::uncaughtException);
        mainStage = primaryStage;
        Parent root;
        if (initThrowable != null) {
            root = createBasicErrorUi(initThrowable);
        } else {
            try {
                root = loadUi();
            } catch (Exception e) {
                e.printStackTrace();
                root = createBasicErrorUi(e);
            }
        }
        mainScene = new Scene(root);
        mainStage.initStyle(StageStyle.TRANSPARENT);
        mainStage.setTitle("DN Gear Sim");
        mainStage.setResizable(false);
        mainStage.setAlwaysOnTop(true);
        mainStage.setScene(mainScene);
        mainStage.show();
        startOk = true;
        centerWindow(mainStage);
        //  If no error, continue
        if (bootstrapUiController != null) {
            bootstrapUiController.setOnExitButtonAction(this::stopApp);
            runBootstrapTask();
        }
    }

    private Parent loadUi() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/co/phoenixlab/dn/dngearsim/bootstrap/fxml/splash.fxml"),
                localeBundle);
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
        Stage updateStage = new Stage(StageStyle.TRANSPARENT);
        updateStage.initOwner(mainStage);
        updateStage.initModality(Modality.WINDOW_MODAL);
        updateStage.setTitle("DN Gear Sim");
        updateStage.setResizable(false);
        updateStage.setAlwaysOnTop(true);
        Parent root;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/co/phoenixlab/dn/dngearsim/bootstrap/fxml/selfupdate.fxml"),
                    localeBundle);
            root = loader.load();
            SelfUpdateWindowController controller = loader.getController();
            controller.setOnExitPressed(this::stopApp);
            //  TODO
//            controller.setOnContinuePressed();
//            controller.setOnDownloadPressed();
        } catch (Exception e) {
            e.printStackTrace();
            root = createBasicErrorUi(e);
        }
        Scene scene = new Scene(root);
        updateStage.setScene(scene);
        updateStage.show();
        centerWindow(updateStage);
    }

    private void onBootstrapFailed(String error) {
        showErrorWindow(error);
    }

    private Stage showErrorWindow(String errorMsg) {
        Stage errorStage = new Stage(StageStyle.TRANSPARENT);
        errorStage.initOwner(mainStage);
        errorStage.initModality(Modality.WINDOW_MODAL);
        errorStage.setTitle("DN Gear Sim");
        errorStage.setResizable(false);
        errorStage.setAlwaysOnTop(true);
        Parent root;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/co/phoenixlab/dn/dngearsim/bootstrap/fxml/error.fxml"),
                    localeBundle);
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
        centerWindow(errorStage);
        return errorStage;
    }

    private void retryBootstrap() {
        bootstrapTask.cancel(true);
        bootstrapUiController.unbind();
        runBootstrapTask();
    }

    private void runBootstrapTask() {
        bootstrapTask = new BootstrapTask(this::onBootstrapOk, this::onBootstrapSelfUpdateRequired,
                this::onBootstrapFailed, localeBundle);
        bootstrapUiController.bindToTask(bootstrapTask);
        Thread thread = new Thread(bootstrapTask, "BootstrapTask");
        thread.setDaemon(true);
        thread.start();
    }

    private void centerWindow(Window window) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        window.setX(bounds.getWidth() / 2 - window.getWidth() / 2);
        window.setY(bounds.getHeight() / 2 - window.getHeight() / 2 - 10);
    }

    private void uncaughtException(Thread thread, Throwable throwable) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        String formatted = String.format("Fatal exception in thread %s \"%s\": %s",
                thread.getId(), thread.getName(), writer.toString());
        System.err.println(formatted);
        System.err.println("Application will now exit");
        //  Check first if the JFX application started successfully
        if (startOk) {
            try {
                Stage stage = createFatalErrorPopup(thread, throwable);
                stage.showAndWait();
            } catch (Exception e) {
                //  Failed to create the window, take note
                writer = new StringWriter();
                printWriter = new PrintWriter(writer);
                e.printStackTrace(printWriter);
                formatted = String.format("Fatal exception while handling exception: %s",
                        writer.toString());
                System.err.println(formatted);
                System.exit(-2);
                return;
            }
        }
        System.exit(-1);
    }

    private Stage createFatalErrorPopup(Thread thread, Throwable throwable) {
        Stage stage = new Stage();
        VBox root = new VBox(20);
        Scene scene = new Scene(root);
        Label errorLbl = new Label("There was an unexpected problem");
        errorLbl.setFont(Font.font(24));
        Label detailLbl = new Label("Something bad happened, and the program must close.\n" +
                "Sorry about that.\nCaused by " + throwable.toString() + "\nin " + thread.getName());
        Button exitBtn = new Button("Exit");
        exitBtn.setPadding(new Insets(5, 10, 5, 10));
        detailLbl.setTextAlignment(TextAlignment.CENTER);
        exitBtn.setOnAction(ae -> stage.close());
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(errorLbl, detailLbl, exitBtn);
        root.setPadding(new Insets(10));
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("DN Gear Sim");
        stage.setResizable(false);
        stage.setAlwaysOnTop(true);
        stage.setScene(scene);
        //  Add some basic style
        root.setStyle("-fx-background-color: #151515");
        errorLbl.setStyle("-fx-text-fill: #7BAFDC");
        detailLbl.setStyle("-fx-text-fill: #B2B2B2");
        Platform.runLater(() -> centerWindow(stage));
        return stage;
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
