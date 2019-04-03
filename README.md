# Pepper Documentation

* [Existing documentation](#existing-documentation)
* [Development environment](#development-environment)
* [Pepper robot](#pepper-robot)
* [Application structure](#application-structure)
* [Remote monitoring](#remote-monitoring)


## Existing Documentation
The [Softbank Android API](https://qisdk.softbankrobotics.com/sdk/doc/pepper-sdk/index.html) is the primary source of documentation for controlling a Pepper robot via the new Android API available with robot versions 2.9+

Additionally, once 2.9 is publically released, Softbank have indicated that the ['pepper' tag on StackOverflow](https://stackoverflow.com/questions/tagged/pepper) will be the primary source of support. At the moment it is largely populated by questions concerning the old 2.5 release, but I expect this to change rapidly once the public release occurs. The date for this is currently unknown.

## Development Environment
The existing code has been developed with Android Studio 3 connected to a real Pepper robot. There is also an emulator which can be used for testing certain aspects, but among other limitations, cannot be used with ABOD3 or any other remote service as it does not support network connections.

To set up your development enviroment please review Softbank's [comprehensive documentation](https://qisdk.softbankrobotics.com/sdk/doc/pepper-sdk/ch1_gettingstarted/installation.html#installation).

## Pepper Robot
Please ensure your Pepper robot is running version 2.9.x. If the robot is running version 2.5 or older, it will not work with the Android API. If you need to upgrade, please contact Softbank support. Note that at the time of writing 2.9.2 does not support any of the existing 'channels' or Choregraphe software. Beyond very basic human awareness Pepper will no longer have the default functionality you may have been used to on previous versions.

## Application Structure
The main variable elements of an application are the well-understood plan files and the associated XxxxBehaviourLibrary classes which extend the BaseBehaviourLibrary. Each plan and set of behaviours are custom and related, the base library will contain actions and senses that are easily shared across plans.

## Remote Monitoring
### ABOD3
Load the plan file and see the elements highlighted as the POSH planner considers and prioritises them.

### Web App
To be documented as development continues.
