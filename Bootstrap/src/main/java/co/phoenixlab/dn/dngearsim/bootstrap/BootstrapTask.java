package co.phoenixlab.dn.dngearsim.bootstrap;

import javafx.concurrent.Task;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class BootstrapTask extends Task<Method> {

    private final Consumer<Method> onBootstrapOk;
    private final Runnable onBootstrapSelfUpdateRequired;
    private volatile boolean selfUpdateRequred;

    public BootstrapTask(Consumer<Method> onBootstrapOk, Runnable onBootstrapSelfUpdateRequired) {
        this.onBootstrapOk = onBootstrapOk;
        this.onBootstrapSelfUpdateRequired = onBootstrapSelfUpdateRequired;
        this.selfUpdateRequred = false;
    }

    @Override
    protected Method call() throws Exception {
        //  TODO
        return null;
    }

    @Override
    protected void succeeded() {
        if (selfUpdateRequred) {
            onBootstrapSelfUpdateRequired.run();
        } else {
            onBootstrapOk.accept(getValue());
        }
    }
}
