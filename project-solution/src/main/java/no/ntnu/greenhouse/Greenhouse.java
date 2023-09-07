package no.ntnu.greenhouse;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import no.ntnu.tools.Logger;

/**
 * Represents a greenhouse with sensor/actuator nodes inside.
 */
public class Greenhouse implements ActuatorListener {
  private final Map<Integer, SensorActuatorNode> nodes = new HashMap<>();
  private final List<PeriodicActuator> periodicActuators = new LinkedList<>();

  public void addNode(SensorActuatorNode node) {
    nodes.put(node.getId(), node);
  }

  /**
   * Start simulating the greenhouse - all the sensor/actuator nodes in it.
   */
  public void startSimulation() {
    for (SensorActuatorNode node : nodes.values()) {
      node.startSimulation();
    }
    for (PeriodicActuator periodicActuator : periodicActuators) {
      periodicActuator.start();
    }
  }

  /**
   * Stop simulating the greenhouse - all the sensor/actuator nodes in it.
   */
  public void stopSimulation() {
    Logger.info("Stopping greenhouse simulation...");
    for (SensorActuatorNode node : nodes.values()) {
      node.stopSimulation();
    }
    for (PeriodicActuator periodicActuator : periodicActuators) {
      periodicActuator.stop();
    }
  }

  public void addPeriodicActuator(PeriodicActuator periodicActuator) {
    periodicActuators.add(periodicActuator);
  }

  @Override
  public void actuatorUpdated(Actuator actuator) {
    for (SensorActuatorNode node : nodes.values()) {
      actuator.applyImpact(node);
    }
  }
}