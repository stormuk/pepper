# Pepper Documentation

* [Existing documentation](#existing-documentation)
* [Development environment](#development-environment)
* [Pepper robot](#pepper-robot)
* [Design principles](#design-principles)
* [Application structure](#application-structure)
* [Remote monitoring](#remote-monitoring)


## Existing Documentation
The [Softbank Android API](https://qisdk.softbankrobotics.com/sdk/doc/pepper-sdk/index.html) is the primary source of documentation for controlling a Pepper robot via the new Android API available with robot versions 2.9+

Additionally, once 2.9 is publically released, Softbank have indicated that the ['pepper' tag on StackOverflow](https://stackoverflow.com/questions/tagged/pepper) will be the primary source of support. At the moment it is sparsely populated with questions concerning the old 2.5 release, but I expect this to change rapidly once the public release occurs. The date for this release is currently unknown.

## Development Environment
The existing code has been developed with Android Studio 3 connected to a real Pepper robot. There is also an emulator which can be used for testing certain aspects, but among other limitations, cannot be used with ABOD3 or any other remote service as it does not support network connections.

To set up your development enviroment please review Softbank's [comprehensive documentation](https://qisdk.softbankrobotics.com/sdk/doc/pepper-sdk/ch1_gettingstarted/installation.html#installation). The short version is:

1. Install Android Studio (and the Java Development Kit) and start it
2. From *Tools -> SDK Manager* install "Android SDK version 6.0 (API 23, Marshmallow)" and the latest "SDK Build-tools" for version 23
3.  From *File -> Settings...* go to the *Plugins* section, click the "Browse repositories..." button, and then search for "Pepper". Install the "Pepper SDK"
4. Restart Android Studio and start a new blank project
5. Choose *File -> New -> Robot Application…* and then *Tools -> Pepper SDK -> Robot SDK Manager*
6. Install "API v4" (and v3 if you want to run Softbank's demo code)
7. Restart Android Studio

**Top tip:** Always restart Android Studio after installing or updating anything. This will avoid strange behaviours caused by partially loaded projects/plugins.

## Pepper Robot
Please ensure your Pepper robot is running version 2.9.x. If the robot is running version 2.5 or older, it will not work with the Android API. If you need to upgrade, please contact Softbank support. Note that at the time of writing 2.9.2 does not support any of the existing 'channels' or Choregraphe software. Beyond very basic human awareness Pepper will no longer have the default functionality you may have been used to on previous versions.

It should also be noted that 2.9 is only compatible with Peppers 1.8 and 1.8a. Earlier 1.6 robots will not work. You can identify your robot version [using this guide](http://doc.aldebaran.com/2-5/family/pepper_technical/pepper_versions.html), and should do so before developing Android apps for it.

## Connecting
First ensure that your Pepper robot is connected to the same WiFi network as your development computer, then follow these steps:

1. Use the *Tools -> Pepper SDK* menu and click "Connect". This will open a window to let you select the Pepper robot
2. Highlight a robot, and press the 'Select' button
3. You will be prompted for the password. Once this has been entered correctly, the robot viewer window will open
4. There will also be a popup asking for the tablet IP address, sometimes this appears behind the robot viewer. On the robot's tablet, you can swipe down the notification area and see the IP address for the tablet labelled as "For Run/Debug Config". Enter the IP address and add the port number 5555, eg. "192.168.1.101:5555". Press OK
5. You can now "Run" the Android application onto the Pepper robot.

## Design Principles
Please familiarise yourself with the following concepts to help in understanding the [application structure](#application-structure), as authored by [Professor Joanna J Bryson](http://www.cs.bath.ac.uk/~jjb/index.html) and colleagues:

* [POSH (Parallel-rooted, Ordered Slip-stack Hierarchical) - dynamic plans](http://www.cs.bath.ac.uk/~jjb/web/posh.html)
* [BOD (Behaviour Oriented Design) - behaviour libraries](http://www.cs.bath.ac.uk/~jjb/web/bod.html)

For more information on the academic research behind these (and other) concepts please review the content linked from the University of Bath's [AmonI Software homepage](http://www.cs.bath.ac.uk/ai/AmonI-sw.html).

## Application Structure
### Variation from Softbank example code
In the example code found in Softbank's document the initialisation and callback handling are both managed directly within the Android Activity (`MainActivity`). This is simpler when working with trivial example cases, but was found to frustrate modular development of BOD methods.

It has been decided to keep the basic Android functionality (primarily UI/OS interactions) in the Activity, separate from the implementation of the POSH planner and the BOD library's interaction with the Pepper robot which are designed to be interchangeable.

### Core functionality
#### Start up
When the Android app first starts, the robot will map its surroundings and localise itself within the space. This improves Pepper's ability to identify and track humans.

#### User interface
The UI is intentionally simple, showing the currently active DriveCollection on the left, and the path through the decision tree to the current Action. There is a "Start" and "Stop" button to control plan execution.

It is also possible to store a series of locations that can be used by plans. Use the "Add" and "Clear" buttons in the bottom right to set these before starting a plan execution. Remember to open the robot's charging port before moving the robot into each location or the motors will be damaged. Press "Add" at each location you wish to store. And then remember to close the charging port again.

#### POSH plans
Although the ABOD3 software supports multiple POSH plan formats, currently this app only supports XML plans. Wider plan file support (Lisp, JSON) should be implemented as part of future UI improvements relating to plan selection.

Plans are stored in `/app/resources/raw/`, and selected in `MainActivity`s `onCreate` method by setting `planResourceId` to the associated Android resource value:
```
planResourceId = R.raw.plan_example;
```

To allow for recursive plans, and to avoid duplicate definitions, the elements of a POSH plan are defined from the bottom up (ActionPatterns, CompetenceElements, Competences, DriveElements, Drives). Container elements (eg. a Competence) refer to their child elements (eg. CompetenceElements) by name only, as those named elements will have already been defined earlier in the document. The plan reader first parses the individual elements, and then replaces the placeholder name-only elements with links to their full definitions. It is therefore important to ensure the names match exactly! Senses are always defined in full as their conditions will vary depending on context.

#### BOD behaviour libraries
The behaviour library has an interface to provide access to sense values and a set of actions that can be initiated by the POSH planner. Your behaviour library should extend `BaseBehaviourLibrary` (see [Common elements](#common-elements)) to take advantage of default setup and common senses/actions.
```
behaviourLibrary = new ExampleBehaviourLibrary();
```

#### Common elements
The `BaseBehaviourLibrary` makes some common senses and actions available, as well as some helper methods, to aid in development of your own libraries.
##### Senses
* Boolean
    - HumanPresent
    - BatteryLow
    - Talking
    - Listening
    - Animating
    - HumanPresent
    - HumanClose
    - HumanEngaged
    - FacingNearHuman
* Numeric
    - IdleTime

##### Actions
* DismissHumans - to ask any humans present to leave the room (for mapping)
* PromptForBatteryCharge - to ask to be plugged in when battery is low
* StopListening - to cancel any active listening actions

##### Helpers
* DoMapping
* SaveLocation
* ClearLocations
* GoToLocation
* HoldAwareness
* ReleaseAwareness


## Remote Monitoring
### Android Studio
When running an application from Android Studio it is possible to view raw debug information in the "Logcat" pane, usually accessed from the bottom left of the window.

### ABOD3
Load the plan file and see the elements highlighted as the POSH planner considers and prioritises them.

### Web app
To be documented as development continues. Currently this consists of a simple ruby script which listens for logging events from the robot and then passes them on to a websockets-enabled Rails app for display.


# Credits
The code in this repository is built upon on the work of others, including amongst others:

* [ABOD3](https://github.com/RecklessCoding/ABOD3) - Andreas Theodorou
* [BOD-UNity-Game](https://github.com/RecklessCoding/BOD-UNity-Game) - Andreas Theodorou
* [ABOD3AR](https://github.com/alexs7/ABOD3AR) - Alexandros Rotsidi

