package no.ntnu.gui.greenhouse;

import java.util.HashMap;
import java.util.Map;
import javafx.application.Application;
import javafx.stage.Stage;
import no.ntnu.greenhouse.GreenhouseSimulator;
import no.ntnu.greenhouse.SensorActuatorNode;
import no.ntnu.listeners.greenhouse.NodeStateListener;
import no.ntnu.tools.Logger;

/**
 * Run a greenhouse simulation with a graphical user interface (GUI), with JavaFX.
 */
public class GreenhouseApplication extends Application implements NodeStateListener {
  private static GreenhouseSimulator simulator;
  private final Map<SensorActuatorNode, NodeGuiWindow> nodeWindows = new HashMap<>();

  @Override
  public void start(Stage mainStage) {
    mainStage.setScene(new MainGreenhouseGuiWindow());
    mainStage.setMinWidth(MainGreenhouseGuiWindow.WIDTH);
    mainStage.setMinHeight(MainGreenhouseGuiWindow.HEIGHT);
    mainStage.setTitle("Greenhouse simulator");
    mainStage.show();
    Logger.info("GUI subscribes to lifecycle events");
    simulator.initialize();
    simulator.subscribeToLifecycleUpdates(this);
    mainStage.setOnCloseRequest(event -> {
      simulator.stop();
      try {
        stop();
      } catch (Exception e) {
        Logger.error("Could not stop the application: " + e.getMessage());
      }
    });
    simulator.start();
  }

  /**
   * Start the GUI Application.
   */
  public static void startApp() {
    Logger.info("Running greenhouse simulator with JavaFX GUI...");
    simulator = new GreenhouseSimulator();
    launch();
  }

  @Override
  public void onNodeReady(SensorActuatorNode node) {
    Logger.info("Starting window for node " + node.getId());
    NodeGuiWindow window = new NodeGuiWindow(node);
    nodeWindows.put(node, window);
    window.show();
  }

  @Override
  public void onNodeStopped(SensorActuatorNode node) {
    NodeGuiWindow window = nodeWindows.remove(node);
    if (window != null) {
      window.close();
    }
  }
}
