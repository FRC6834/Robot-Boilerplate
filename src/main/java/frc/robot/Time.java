/*----------------------------------------------------------------------------*/
/* Copyright (c) 2020-2021 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

///
/// Time.java - Single threaded Time system, relying on Java's Clock class
/// -------------------
/// Ian Bowling
/// -------------------
///

package frc.robot;

import java.time.Clock;
import java.util.ArrayList;

public class Time {
    // Definitions
    private static ArrayList<Integer> Delay_Time = new ArrayList<>(); // Storage for the CPU-Time, in milliseconds, when IterReady will be true.
    private static Clock CPUTime = Clock.systemDefaultZone(); // Will be used to obtain the current time, in milliseconds.
    // END Definitions

    // IterReady takes the provided id and verifies
    // the CPU time is greater or equal to the time
    // initially declared in the Delay_Time array.
    // Ex.
    // if (Time.IterReady(0)) { Robot.DoCoolStuff(); }
    public static boolean IterReady(int id) {
        // First, check if Delay_Time at index of id is actually valid
        if (Delay_Time.get(id) == null) {
            // Alert that an invalid id was used
            // This is actually recoverable (technically), however we want
            // this to be addressed ASAP, so we'll ERR and Quit.
            Logger.SendError("Time.IterReady: Invalid id (" + id + ") referenced. Stopping..");

            // Exit with code 0, because Logger already gave us a useful error.
            System.exit(0);
        }

        // We have reached our threshold/the time is right.
        if (CPUTime.millis() >= Delay_Time.get(id))
            return true;

        // Not yet there, return false to be checked next iteration.
        return false;
    }

    // IterDelay takes an integer and reserves a slot in
    // the Delay_Time array, which contains the CPUTime in
    // milliseconds to where the Timer is now considered
    // valid/ready. This has NO check to verify the id
    // is not already defined and unused. Be wise.
    // Ex.
    // Time.IterDelay(1000, 0);
    public static void IterDelay(int ms, int id) {
        Delay_Time.set(id, (int)CPUTime.millis() + ms);
    }
}