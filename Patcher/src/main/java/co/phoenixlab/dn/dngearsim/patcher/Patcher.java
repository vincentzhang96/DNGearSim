package co.phoenixlab.dn.dngearsim.patcher;

import co.phoenixlab.dn.dngearsim.bootstrap.Bootstrap;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Patcher implements co.phoenixlab.dn.dngearsim.bootstrap.BootstrapHandoff {

    public static void main(String[] args) {

    }


    public Patcher() {

    }

    @Override
    public void handOff(Bootstrap bootstrap, Stage mainStage, Scene mainScene) {
        //  TODO
        System.out.println("Potato");

    }
}
