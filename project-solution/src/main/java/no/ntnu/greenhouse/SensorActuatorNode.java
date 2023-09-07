package no.ntnu.greenhouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import no.ntnu.tools.Logger;

/**
 * Represents one node with sensors and actuators.
 */
public class SensorActuatorNode {
  // How often to generate new sensor values, in seconds.
  private static final long SENSING_DELAY = 5000;
  private final int id;

  private final List<Sensor> sensors = new LinkedList<>();
  private final Map<String, List<Actuator>> actuators = new HashMap<>();

  private final List<SensorListener> sensorListeners = new LinkedList<>();
  private final List<ActuatorListener> actuatorListeners = new LinkedList<>();

  Timer sensorReadingTimer;

  /**
   * Create a sensor/actuator node. Note: the node itself does not check whether the ID is unique.
   * This is done at the greenhouse-level.
   *
   * @param id A unique ID of the node
   */
  public SensorActuatorNode(int id) {
    this.id = id;
  }

  /**
   * Get the unique ID of the node.
   *
   * @return the ID
   */
  public int getId() {
    return id;
  }

  /**
   * Add sensors to the node.
   *
   * @param template The template to use for the sensors. The template will be cloned.
   *                 This template defines the type of sensors, the value range, value
   *                 generation algorithms, etc.
   * @param n        The number of sensors to add to the node.
   */
  public void addSensors(Sensor template, int n) {
    if (template == null) {
      throw new IllegalArgumentException("Sensor template is missing");
    }
    String type = template.getType();
    if (type == null || type.isEmpty()) {
      throw new IllegalArgumentException("Sensor type missing");
    }
    if (n <= 0) {
      throw new IllegalArgumentException("Can't add a negative number of sensors");
    }

    for (int i = 0; i < n; ++i) {
      sensors.add(template.createClone());
    }
  }

  /**
   * Add a set of actuators to the node.
   *
   * @param template The actuator to use as a template.
   * @param n        The number of actuators to add
   */
  public void addActuators(Actuator template, int n) {
    if (template == null) {
      throw new IllegalArgumentException("Actuator template is missing");
    }
    if (n <= 0) {
      throw new IllegalArgumentException("Can't add a negative number of actuators");
    }

    List<Actuator> actuatorsOfThatType = getActuatorsOfGivenType(template.getType());
    for (int i = 0; i < n; ++i) {
      actuatorsOfThatType.add(template.createClone());
    }
  }

  /**
   * Register a new listener for sensor updates.
   *
   * @param listener The listener which will get notified every time sensor values change.
   */
  public void addSensorListener(SensorListener listener) {
    if (!sensorListeners.contains(listener)) {
      sensorListeners.add(listener);
    }
  }

  /**
   * Register a new listener for actuator updates.
   *
   * @param listener The listener which will get notified every time actuator state changes.
   */
  public void addActuatorListener(ActuatorListener listener) {
    if (!actuatorListeners.contains(listener)) {
      actuatorListeners.add(listener);
    }
  }

  private List<Actuator> getActuatorsOfGivenType(String type) {
    return actuators.computeIfAbsent(type, k -> new ArrayList<>());
  }

  /**
   * Start simulating the sensor node's operation.
   */
  public void startSimulation() {
    startPeriodicSensorReading();
    openCommunicationChannel();
  }

  /**
   * Stop simulating the sensor node's operation.
   */
  public void stopSimulation() {
    Logger.info("-- Stopping simulation of node " + id);
    stopPeriodicSensorReading();
    closeCommunicationChannel();
  }

  private void openCommunicationChannel() {
    // TODO
  }

  private void closeCommunicationChannel() {
    // TODO
  }

  private void startPeriodicSensorReading() {
    sensorReadingTimer = new Timer();
    TimerTask newSensorValueTask = new TimerTask() {
      @Override
      public void run() {
        generateNewSensorValues();
      }
    };
    long randomStartDelay = (long) (Math.random() * SENSING_DELAY);
    sensorReadingTimer.scheduleAtFixedRate(newSensorValueTask, randomStartDelay, SENSING_DELAY);
  }

  private void stopPeriodicSensorReading() {
    if (sensorReadingTimer != null) {
      sensorReadingTimer.cancel();
    }
  }

  /**
   * Generate new sensor values and send a notification to all listeners.
   */
  public void generateNewSensorValues() {
    Logger.infoNoNewline("Node #" + id);
    addRandomNoiseToSensors();
    notifySensorChanges();
    debugPrint();
  }

  private void addRandomNoiseToSensors() {
    for (Sensor sensor : sensors) {
      sensor.addRandomNoise();
    }
  }

  private void debugPrint() {
    for (Sensor sensor : sensors) {
      Logger.infoNoNewline(" " + sensor.getCurrent() + sensor.getUnit());
    }
    Logger.infoNoNewline(" :");
    for (List<Actuator> actuatorList : actuators.values()) {
      for (Actuator actuator : actuatorList) {
        Logger.infoNoNewline(" " + actuator.getType() + (actuator.isOn() ? " ON" : " off"));
      }
    }
    Logger.info("");
  }

  /**
   * Toggle an actuator attached to this device.
   *
   * @param type  The type of the actuator
   * @param index The index of the actuator (within the list of actuators with the specified type).
   *              Indexing starts at zero.
   * @throws IllegalArgumentException If no actuator with given configuration is found on this node
   */
  public void toggleActuator(String type, int index) {
    Actuator actuator = getActuator(type, index);
    if (actuator == null) {
      throw new IllegalArgumentException(type + "[" + index + "] not found on node " + id);
    }
    actuator.toggle();
    notifyActuatorChange(actuator);
  }

  private Actuator getActuator(String type, int index) {
    Actuator actuator = null;
    List<Actuator> actuatorsOfThatType = actuators.get(type);
    if (actuatorsOfThatType != null && index >= 0 && index < actuatorsOfThatType.size()) {
      actuator = actuatorsOfThatType.get(index);
    }
    return actuator;
  }

  private void notifySensorChanges() {
    for (SensorListener listener : sensorListeners) {
      listener.sensorsUpdated(sensors);
    }
  }

  private void notifyActuatorChange(Actuator actuator) {
    for (ActuatorListener listener : actuatorListeners) {
      listener.actuatorUpdated(actuator);
    }
  }

  /**
   * An actuator has been turned on or off. Apply an impact from it to all sensors of given type.
   *
   * @param sensorType The type of sensors affected
   * @param impact     The impact to apply
   */
  public void applyActuatorImpact(String sensorType, double impact) {
    for (Sensor sensor : sensors) {
      if (sensor.getType().equals(sensorType)) {
        sensor.applyImpact(impact);
      }
    }
  }
}