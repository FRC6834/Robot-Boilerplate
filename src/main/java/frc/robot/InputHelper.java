/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

///
/// InputHelper.java - Cleaner method to use WPILIB's Joystick
/// -------------------
/// AUTHOR: Ian Bowling
/// -------------------
///

package frc.robot;

import edu.wpi.first.wpilibj.*;

public class InputHelper {
    // Button Definitions
    // Use enums here instead of int spam because it's cleaner and faster - IB (6/13/20)
    public static enum XInput {
        Null,
        // Face Buttons
        ButtonA,
        ButtonB,
        ButtonX,
        ButtonY,
        // Bumpers and Triggers
        BumperLeft,
        BumperRight,
        TriggerLeft,
        TriggerRight,
        // Analog Sticks
        AnalogLeft,
        AnalogRight,
        // Misc.
        ButtonSelect,
        ButtonStart,
        ButtonLS,
        ButtonRS
    }

    // TODO - Potentially have things for button being held or single presses?
}