package no.ntnu.controlpanel;

import java.util.LinkedList;
import java.util.List;
import no.ntnu.greenhouse.GreenhouseEventListener;
import no.ntnu.greenhouse.SensorReading;

/**
 * The central logic of a control panel node. It uses a communication channel to send commands
 * and receive events. It supports listeners who will be notified on changes (for example, a new
 * node is added to the network, or a new sensor reading is received).
 */
public class ControlPanelLogic implements FakeSpawnerListener {
  private final List<GreenhouseEventListener> listeners = new LinkedList<>();

  /**
   * Add an event listener.
   *
   * @param listener The listener who will be notified on all events
   */
  public void addListener(GreenhouseEventListener listener) {
    if (!listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  /**
   * Initiate some fake events, for testing without real data.
   */
  public void initiateFakeEvents() {
    // TODO - remove this when socket communication with real events is implemented
    FakeSensorNodeSpawner spawner = new FakeSensorNodeSpawner(this);
    spawner.spawnNode("4;3_window", 2);
    spawner.spawnNode("1", 3);
    spawner.spawnNode("1", 4);
    spawner.spawnNode("8;2_heater", 5);
    spawner.advertiseSensorData("4;temperature=27.4 °C,temperature=26.8 °C,humidity=80 %", 4);
    spawner.advertiseSensorData("4;temperature=22.4 °C,temperature=26.0 °C,humidity=81 %", 9);
    spawner.advertiseSensorData("4;temperature=25.4 °C,temperature=27.0 °C,humidity=82 %", 14);
    spawner.advertiseSensorData("1;humidity=80 %,humidity=82 %", 10);
    spawner.advertiseRemovedNode(8, 11);
    spawner.advertiseRemovedNode(4, 12);
    spawner.advertiseSensorData("1;temperature=25.4 °C,temperature=27.0 °C,humidity=67 %", 13);
    spawner.advertiseSensorData("4;temperature=25.4 °C,temperature=27.0 °C,humidity=82 %", 16);
    spawner.advertiseRemovedNode(4, 18);
  }

  @Override
  public void onNodeSpawned(SensorActuatorNodeInfo nodeInfo) {
    listeners.forEach(listener -> listener.onNodeAdded(nodeInfo));
  }

  @Override
  public void onNodeRemoved(int nodeId) {
    listeners.forEach(listener -> listener.onNodeRemoved(nodeId));
  }

  @Override
  public void onSensorData(int nodeId, List<SensorReading> sensors) {
    listeners.forEach(listener -> listener.onSensorData(nodeId, sensors));
  }
}
