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
    /*
        Static integers for button or axis names, as remembering
        numbers can be a major pain. Layout/Names are based on
        Generic xinput (Xbox)'s scheme, as that's what WPILIB
        natively supports.

        - IB
    */
    // Face Buttons
    public static int BUTTON_A = 1;
    public static int BUTTON_B = 2;
    public static int BUTTON_X = 3;
    public static int BUTTON_Y = 4;

    // Bumpers and Triggers
    public static int BUMPER_LEFT = 5;
    public static int BUMPER_RIGHT = 6;
    public static int TRIGGER_LEFT = 2;
    public static int TRIGGER_RIGHT = 3;

    // Analog Sticks
    public static int ANALOG_LEFT = 0;
    public static int ANALOG_RIGHT = 1;

    // Misc.
    public static int BUTTON_SELECT = 7;
    public static int BUTTON_START = 8;
    public static int BUTTON_LS = 9;
    public static int BUTTON_RS = 10;

    // TODO - Potentially have things for button being held or single presses?- IB
}