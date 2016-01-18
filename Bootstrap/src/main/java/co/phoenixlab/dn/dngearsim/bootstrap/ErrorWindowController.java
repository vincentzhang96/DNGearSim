/*
 * The MIT License (MIT)
 *
 * Copyright (c) ${year} Vincent Zhang
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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class ErrorWindowController {
    @FXML
    private Button retryDialogBtn;

    @FXML
    private Label detailText;

    @FXML
    private Button exitBtn;

    @FXML
    private Button exitDialogBtn;

    private Runnable onRetryPressed;

    private Runnable onExitPressed;

    public void setOnRetryPressed(Runnable onRetryPressed) {
        this.onRetryPressed = onRetryPressed;
    }

    public void setOnExitPressed(Runnable onExitPressed) {
        this.onExitPressed = onExitPressed;
    }

    public void setErrorMessage(String msg) {
        detailText.setText(msg);
    }

    @FXML
    void onRetryBtnPressed(ActionEvent event) {
        if (onRetryPressed != null) {
            onRetryPressed.run();
        }
    }

    @FXML
    void onExitBtnPressed(ActionEvent event) {
        if (onExitPressed != null) {
            onExitPressed.run();
        }
    }
}
