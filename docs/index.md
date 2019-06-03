---
title: POSH for Pepper
description: Android app for the Pepper robot, integrating Java ports of POSH and ABOD3
---

# Background

Building on [previous research](http://www.cs.bath.ac.uk/ai/AmonI-sw.html), this project began as an investigation into the capabilities of the [Pepper robot](https://www.softbankrobotics.com/emea/en/pepper), and the feasibility of porting/extending existing [POSH](http://www.cs.bath.ac.uk/~jjb/web/posh.html) and [ABOD3](https://github.com/RecklessCoding/ABOD3) implementations.

# Progress

There is now a proof-of-concept implementation of a POSH plan being used to achieve prioritised goals by responding to changes in its environment. Challenges that have been encountered include incomplete official documentation, robot software version mismatches, and the autonomous nature of the robot's built-in behaviours.

The intent is to create a base Android application that can be extended to address [specific experimental goals](https://www.bath.ac.uk/announcements/humanoid-robot-tests-to-explore-ai-ethics/) through extension of a set of core Java classes and debugging tools. The application displays basic information about current priorities on the robot's tablet, and also integrates with an external instance of the ABOD3 program for further monitoring.

# What's next?

The next step is to build upon the tools provided in the proof-of-concept to address real world issues through experimentation, and to continue refining the base Android implementation to allow others to do the same without having to start from scratch.

