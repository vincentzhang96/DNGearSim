package co.phoenixlab.dn.dngearsim.bootstrap;

import javafx.event.ActionEvent;

public class SelfUpdateWindowController {

    private Runnable onDownloadPressed;
    private Runnable onContinuePressed;
    private Runnable onExitPressed;

    public void setOnDownloadPressed(Runnable onDownloadPressed) {
        this.onDownloadPressed = onDownloadPressed;
    }

    public void setOnContinuePressed(Runnable onContinuePressed) {
        this.onContinuePressed = onContinuePressed;
    }

    public void setOnExitPressed(Runnable onExitPressed) {
        this.onExitPressed = onExitPressed;
    }

    public void onDownloadBtnPressed(ActionEvent event) {
        if (onDownloadPressed != null) {
            onDownloadPressed.run();
        }
    }

    public void onContinueBtnPressed(ActionEvent event) {
        if (onContinuePressed != null) {
            onContinuePressed.run();
        }
    }

    public void onExitBtnPressed(ActionEvent event) {
        if (onExitPressed != null) {
            onExitPressed.run();
        }
    }
}
