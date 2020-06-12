/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

///
/// Logger.java - Handles logging of warns, errs, etc.
/// -------------------
/// Spencer Kleeh, Ian Bowling, Ian Kinkade
/// Ian Bowling is the best
/// Ian Kinkade is the one guy who burned his wallet for First Merchandise
/// Spencer Kleeh is the guy who left us but is kinda still here idk he wasn't there for the photo
/// -------------------
///

package frc.robot;

// FIXME - Clear unneccessary imports! - IB
import java.io.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import java.util.Scanner;
import java.util.Vector;
import java.time.format.DateTimeFormatter;

import java.time.LocalDateTime;

public class Logger {

    private static Vector<String> ErrorBacklogVector = new Vector();
    private static Vector<String> WarningBacklogVector = new Vector();
    private static Vector<String> GeneralBacklogVector = new Vector();
    
    private static FileWriter Log;
    private static boolean VerboseMode;

    public static void main() throws IOException {
        // Input variable
        Scanner conInput = new Scanner(System.in);

        /*
            Calling log functions (e.g. Logger.SendError) will add to respective vector,
            main function will parse said array, print (or write) and then remove when
            action is done. 
            
            1/11/20 - Have to use Vectors instead because Java can't push or pop in Arrays..
            
            - IB   
        */

        // [[maybe have 'comp mode' that just disregards this to make startup faster?]]
        // FIXME - yeah defo do this.. but how? could use file i/o like 2018? - IB
       
        // TO-DO: determine if we're in competition  - IB
        // if (!Competition Mode)
        System.out.println("Enter Log Mode");
        System.out.println("1. Write Mode (Log will be Written to a File)");
        System.out.println("2. Verbose Mode (Log will be print to console and Written to File)");
        System.out.println("3. DEFAULT - No Logs");
        System.out.print("Enter Selection: ");
        int LogMode = conInput.nextInt();
        
        conInput.close();
        
        //If LogMode doesn't equal 1 or 2 it equals 3 - SK
        // FIXME - blank entry breaks things lol - IB
        if ((LogMode != 1 && LogMode != 2))
            return;
        
        //If LogMode equals 2 Verbose mdoe is true, if not its false -SK
        if (LogMode == 2) {
            VerboseMode = true;
        } else {
            VerboseMode = false;
        }
            
        // Probably a good idea to differentiate logs using datetime! :) -IB
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        LocalDateTime CurrentTime = LocalDateTime.now();
        
        // Create new log file
        Log = new FileWriter("C:/Users/bioni/Desktop/Logs/log-" + dtf.format(CurrentTime) + ".txt");
    }

    // Check status of our backlogs, print if not empty
    // - IB
    public static void ParseBacklogs() throws IOException {
        // We used to do forEach's here, but there's no easy way to do index retrieval
        // (for removal after print)
        for (int i = 0; i < ErrorBacklogVector.size(); i++) {
            if (VerboseMode) {
                System.out.print(ErrorBacklogVector.get(i));
            }

            Log.write(ErrorBacklogVector.get(i));
            ErrorBacklogVector.remove(i);

            Log.flush();
        }
        for (int i = 0; i < WarningBacklogVector.size(); i++) {
            if (VerboseMode) {
                System.out.print(WarningBacklogVector.get(i));
            }

            Log.write(WarningBacklogVector.get(i));
            WarningBacklogVector.remove(i);

            Log.flush();
        }
        for (int i = 0; i < GeneralBacklogVector.size(); i++) {
            if (VerboseMode) {
                System.out.print(GeneralBacklogVector.get(i));
            }

            Log.write(GeneralBacklogVector.get(i));
            GeneralBacklogVector.remove(i);

            Log.flush();
        }

        if (RobotFullyInitialized()) {
            if (VerboseMode)
                System.out.println("LOG FINISHED. KILLING.");

            Log.close();
        }
    }

    // TEMP
    public static boolean RobotFullyInitialized() {
        return false;
    }
    
    /*
        Pretty simple (and inefficient but it's Java so I don't really care), but
        we use these functions for readability. All they do is push the, [err, warn, log]
        to the Vector for main to parse.  
        
        - IB
    */
    
    //
    // Example Usage:
    //
    // if (!joystick)
    //    Logger.SendWarning("Couldn't find Joystick!");
    // ------------------------------------------------------
    // - IB 1/10/20 cuz Spencer didn't get it lol
    //
    public static void SendError(String info) {
        ErrorBacklogVector.add("[ERROR]: " + info + "\n");
    }
    
    public static void SendWarning(String info) {
        WarningBacklogVector.add("[WARNING]: " + info + "\n");    
    }
    
    public static void SendLog(String info){
        GeneralBacklogVector.add("[LOG]: " + info + "\n");
    }
}