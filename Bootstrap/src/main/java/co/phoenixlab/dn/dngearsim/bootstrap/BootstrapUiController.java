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
