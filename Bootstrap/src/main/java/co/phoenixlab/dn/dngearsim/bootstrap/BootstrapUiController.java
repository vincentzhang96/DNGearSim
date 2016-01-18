package co.phoenixlab.dn.dngearsim.bootstrap;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;

public class BootstrapUiController {

    @FXML
    private AnchorPane pane;

    @FXML
    private Rectangle loadingBar;

    @FXML
    private Label statusText;

    @FXML
    private Button exitBtn;

    private Runnable exitBtnAction;
    private NumberBinding barWidthBinding;

    @FXML
    void initialize() {
        assert pane != null : "fx:id=\"pane\" was not injected: check your FXML file 'splash.fxml'.";
        assert loadingBar != null : "fx:id=\"loadingBar\" was not injected: check your FXML file 'splash.fxml'.";
        assert statusText != null : "fx:id=\"statusText\" was not injected: check your FXML file 'splash.fxml'.";

    }

    public void bindToTask(Task<?> task) {
        assert task != null : "Task cannot be null, check invocation site";
        statusText.textProperty().
                bind(task.messageProperty());
        barWidthBinding = Bindings.multiply(pane.widthProperty(), task.progressProperty());
        loadingBar.widthProperty().
                bind(barWidthBinding);
    }

    public void unbind() {
        statusText.textProperty().unbind();
        loadingBar.widthProperty().unbind();
    }

    public void setOnExitButtonAction(Runnable runnable) {
        exitBtnAction = runnable;
    }

    @FXML
    private void onExitBtnPressed(ActionEvent event) {
        if (exitBtnAction != null) {
            exitBtnAction.run();
        }
    }
}
