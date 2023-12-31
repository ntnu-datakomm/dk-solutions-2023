# Project solution

Solution for the course project (2023).

Project theme: a distributed smart greenhouse application, consisting of:

* Sensor-actuator nodes
* Visualization nodes
* A server which acts like a broker

See protocol description in [protocol.md](protocol.md).

## Getting started

There are several runnable classes in the project.

To run the greenhouse part (with sensor/actuator nodes):

* Command line version: run the `main` method inside `CommandLineGreenhouse` class.
* GUI version: run the `main` method inside `GreenhouseGuiStarter` class. Note - if you run the
  `GreenhouseApplication` class directly, JavaFX will complain that it can't find necessary modules.

To run the control panel (only GUI-version is available): run the `main` method inside the
`ControlPanelStarter` class