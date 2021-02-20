/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

// Added imports 2/1/20
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.cscore.MjpegServer;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.Timer;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.Faults;
import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
import com.ctre.phoenix.motorcontrol.TalonFXInvertType;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.motorcontrol.can.TalonFXConfiguration;
import com.ctre.phoenix.motorcontrol.NeutralMode;

import edu.wpi.first.wpilibj.*;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;

import edu.wpi.first.networktables.*;


/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
    private static final String kDefaultAuto = "Default";
    private static final String kCustomAuto = "My Auto";
    private String m_autoSelected;
    private final SendableChooser<String> m_chooser = new SendableChooser<>();

    // Joystick Initialization
    private final Joystick DriverOne = new Joystick(0);
    private final Joystick DriverTwo = new Joystick(1);
  
 
    /*
        DRIVE & SPEED CONTROLLER INITIALIZATION

        We utilize File IO to determine which speed controllers to initialize (VictorSP or TalonSRX)

        The RIO on the robot containing a file in the root dir named 'victor' (/victor) will be checked for. If this
        file exists, we know this robot utilizes VictorSPs, and those should be initialized.

        If this file does not exist, by default we initialize TalonSRXs instead.

        Use SSH to echo the file into existence.

        - IB
    */

    // Need to init both Victor and Talons
    private final WPI_VictorSPX leftVictor = new WPI_VictorSPX(1);
    private final WPI_VictorSPX rightVictor = new WPI_VictorSPX(0);
    private final WPI_VictorSPX rightVictor2 = new WPI_VictorSPX(1);
    private final WPI_VictorSPX leftVictor2 = new WPI_VictorSPX(2);

    private WPI_TalonSRX leftTalon = new WPI_TalonSRX(02);
    private WPI_TalonSRX rightTalon = new WPI_TalonSRX(03);
    private WPI_TalonSRX rightTalon2 = new WPI_TalonSRX(0);
    private WPI_TalonSRX leftTalon2 = new WPI_TalonSRX(1);


    // Default to false, only set true if /victor is found.
    boolean victorUse = false;
    File victorFile = new File("/victor");
    public int autoTick = 0;

    // Just declare as null, properly initialize after we determine speed controller.
    private DifferentialDrive drive = null;
    private DifferentialDrive drive2 = null;

    // Gyro (Talons only!)
    private ADXRS450_Gyro gyro = new ADXRS450_Gyro();

    // XInput Helper
    private InputHelper Controller;

  
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    @Override
    public void robotInit() {
        m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
        m_chooser.addOption("My Auto", kCustomAuto);
        SmartDashboard.putData("Auto choices", m_chooser);

        //try {
        //    Logger.Initialize();
        //} catch(IOException e) {
        //    e.printStackTrace();
        //}

        // /victor exists, so we know to utilize VictorSPs
        if (victorFile.exists()) {
            victorUse = true;
            drive = new DifferentialDrive(rightVictor, leftVictor);
            drive2 = new DifferentialDrive(rightVictor2, leftVictor2);
            //Logger.SendLog("Using VictorSP as Speed Controller");
        } else {
            drive = new DifferentialDrive(rightTalon, leftTalon);
            drive2 = new DifferentialDrive(rightTalon2, leftTalon2);
            leftTalon.setNeutralMode(NeutralMode.Coast);
            rightTalon.setNeutralMode(NeutralMode.Coast);
            //Logger.SendLog("Using TalonSRX as Speed Controller");
        }
    }

    /**
     * This function is called every robot packet, no matter the mode. Use
     * this for items like diagnostics that you want ran during disabled,
     * autonomous, teleoperated and test.
     *
     * <p>This runs after the mode specific periodic functions, but before
     * LiveWindow and SmartDashboard integrated updating.
     */
    @Override
    public void robotPeriodic() {
        //robot.WinMatch();
    }

    @Override
    public void disabledInit() {

    }

    @Override
    public void teleopInit() {
        // TODO Auto-generated method stub
        super.teleopInit();
    }

    /**
     * This autonomous (along with the chooser code above) shows how to select
     * between different autonomous modes using the dashboard. The sendable
     * chooser code works with the Java SmartDashboard. If you prefer the
     * LabVIEW Dashboard, remove all of the chooser code and uncomment the
     * getString line to get the auto name from the text box below the Gyro
     *
     * <p>You can add additional auto modes by adding additional comparisons to
     * the switch structure below with additional strings. If using the
     * SendableChooser make sure to add them to the chooser code above as well.
     */
    @Override
    public void autonomousInit() {
        // Victors cannot utilize autonomous -IB
        if (victorUse)
            return;

        gyro.reset();

        leftTalon.setSelectedSensorPosition(0, 0, 10);
        rightTalon.setSelectedSensorPosition(0, 0, 10);
    }

    /**
     * This function is called periodically during autonomous.
     */
    @Override
    public void autonomousPeriodic() {
        // Victors cannot utilize autonomous -IB
        if (victorUse)
            return;
    }

    /**
     * This function is called periodically during operator control.
     */
    @Override
    public void teleopPeriodic() {
        //Drive
        if (DriverOne.getRawAxis(Controller.TRIGGER_RIGHT) > 0) {
            drive.curvatureDrive(DriverOne.getRawAxis(Controller.TRIGGER_RIGHT), DriverOne.getRawAxis(Controller.ANALOG_LEFT), false);
            drive2.curvatureDrive(DriverOne.getRawAxis(Controller.TRIGGER_RIGHT), DriverOne.getRawAxis(Controller.ANALOG_LEFT), false);
        }else if (DriverOne.getRawAxis(Controller.TRIGGER_LEFT) > 0){
            drive.curvatureDrive(DriverOne.getRawAxis(Controller.TRIGGER_LEFT)*-1, DriverOne.getRawAxis(Controller.ANALOG_LEFT)*-1, false);
            drive2.curvatureDrive(DriverOne.getRawAxis(Controller.TRIGGER_LEFT)*-1, DriverOne.getRawAxis(Controller.ANALOG_LEFT)*-1, false);
        }

        int POV = DriverOne.getPOV();
        int POV2 = DriverTwo.getPOV();

        /*
            POV is the directional pad.
            ----------------------------------
            0: Backwards
            90: Right
            180: Forward
            270: Left
            ----------------------------------
        */
        switch (POV) {
            case 0:
                drive.arcadeDrive(0.5, 0);

                if (drive2 != null)
                    drive2.arcadeDrive(0.5, 0);
                break;
            case 90:
                drive.arcadeDrive(0, 0.5);

                if (drive2 != null)
                    drive2.arcadeDrive(0, 0.5);
                break;
            case 180:
                drive.arcadeDrive(-0.5, 0);

                if (drive2 != null)
                    drive2.arcadeDrive(-0.5, 0);
                break;
            case 270:
                drive.arcadeDrive(0, -0.5);

                if (drive2 != null)
                    drive2.arcadeDrive(0, -0.5);
                break;
        }
    }

    /**
     * This function is called periodically during test mode.
     */
    @Override
    public void testPeriodic() {

    }
}
