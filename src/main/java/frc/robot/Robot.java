/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

/*
WIP
Xinput Button 'Docs':

RAW BUTTONS:
1 - A
2 - B
3 - X
4 - Y
5 - Left Bumper
6 - Right Bumper
7 - Select
8 - Start
9 - Left Stick (DOWN)
10 - Left Stick (DOWN)
- IB
*/

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

// Color Sensor imports
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.util.Color;
import com.revrobotics.ColorSensorV3;
import com.revrobotics.ColorMatchResult;
import com.revrobotics.ColorMatch;
import com.revrobotics.CANError;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import edu.wpi.first.wpilibj.Compressor;


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

    // The amount of iterations full-throttle until we have reached 5ft
    private final int TICKS_PER_FIVE_FEET = 4246;

    // Joystick Initialization
    // FIXME - Actions such as getButton and getAxis are DEPRECATED
    // and will soon be removed! - IB
    private InputHelper Controller;
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

    // Wench Initialization
    private CANSparkMax rightWench = new CANSparkMax(4, MotorType.kBrushless);
    private CANSparkMax leftWench = new CANSparkMax(5, MotorType.kBrushless);
    // IB - change to neos
    //private TalonFX rightWench = new TalonFX(4);
    //private TalonFX leftWench = new TalonFX(5);

    // Pneumatics Initialization
    private final Compressor comp = new Compressor(0);
    private final Solenoid BucketSolenoid = new Solenoid(4);
    private final Solenoid WheelSolenoid = new Solenoid(5);
    private final Solenoid LipSolenoid = new Solenoid(6);
    boolean WheelOn;
    int WheelTicker;

    // Lift Initialization
    private final PWMSparkMax LiftPWM = new PWMSparkMax(2);

    // SPARK
    private Spark SparkWheel = new Spark(1);

    /*
        ============
        COLOR SENSOR
        ============

        Big heap of variables for managing the color sensor, modes,
        and color RGB targets.

        - IB
    */
    // General
    private final I2C.Port i2cPort = I2C.Port.kOnboard;
    private final ColorSensorV3 rainbowDetector = new ColorSensorV3(i2cPort);
    private final ColorMatch m_colorMatcher = new ColorMatch();
    private final Color kBlueTarget = ColorMatch.makeColor(0.143, 0.427, 0.429);
    private final Color kGreenTarget = ColorMatch.makeColor(0.197, 0.561, 0.240);
    private final Color kRedTarget = ColorMatch.makeColor(0.531, 0.372, 0.114);
    private final Color kYellowTarget = ColorMatch.makeColor(0.361, 0.524, 0.113);
    private boolean ColorSensorIsConfident;
    
    /*
        0: RED
        1: GREEN
        2: BLUE
        3: YELLOW

        - IB
    */
    private int colorMendable; 
    private int lastColorMendable;

    // Color Spinner
    private boolean isAutoSpinning;
    private int AutoSpinColor;

    // Revolution Spinner
    private boolean isRevolving;
    private int colorCycles;
    private int WHEEL_REVOLUTIONS_BEFORE_STOP = 3; // WHEEL_REVOLUTIONS_BEFORE_STOP * 8
/*Testing issue w/ cam - EG
    //Camera
    // Creates UsbCamera and MjpegServer [1] and connects them
    UsbCamera werk = CameraServer.getInstance().startAutomaticCapture();

    // Creates the CvSink and connects it to the UsbCamera
    CvSink cvSink = CameraServer.getInstance().getVideo();
*/

  
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    @Override
    public void robotInit() {
        m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
        m_chooser.addOption("My Auto", kCustomAuto);
        SmartDashboard.putData("Auto choices", m_chooser);

        // /victor exists, so we know to utilize VictorSPs
        if (victorFile.exists()) {
            victorUse = true;
            drive = new DifferentialDrive(rightVictor, leftVictor);
            //log.SendLog("Using VictorSP as Speed Controller");
        } else {
            drive = new DifferentialDrive(rightTalon, leftTalon);
            drive2 = new DifferentialDrive(rightTalon2, leftTalon2);
            leftTalon.setNeutralMode(NeutralMode.Coast);
            rightTalon.setNeutralMode(NeutralMode.Coast);
            //log.SendLog("Using TalonSRX as Speed Controller");
        }

        // Add Color Targets to the Matcher
        m_colorMatcher.addColorMatch(kBlueTarget);
        m_colorMatcher.addColorMatch(kGreenTarget);
        m_colorMatcher.addColorMatch(kRedTarget);
        m_colorMatcher.addColorMatch(kYellowTarget);
        //compressor because FRC is redundant and stupid
        comp.start();
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
        // FIXME - Potentially move color sensor stuff here - IB
        // best
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
        /*if (victorUse)
            return;

        int distance = (TICKS_PER_FIVE_FEET/5) * 13;*/

        // Use a janky tick-based timer to use autonomous in a horrible but functional
        // way - IB

        if(autoTick < 100) {
            drive.arcadeDrive(.5,0);
            drive2.arcadeDrive(.5,0);
            autoTick++;
        }

    }

    /**
     * This function is called periodically during operator control.
     */
    @Override
    public void teleopPeriodic() {
        SparkWheel.set(0);

        if (isRevolving || isAutoSpinning)
            SparkWheel.set(1);

        if (DriverTwo.getRawButton(Controller.BUTTON_B) && !isRevolving && !isAutoSpinning)
            SparkWheel.set(1);
    
        //Drive
        if (DriverOne.getRawAxis(Controller.TRIGGER_RIGHT) > 0) {
            drive.curvatureDrive(DriverOne.getRawAxis(Controller.TRIGGER_RIGHT), DriverOne.getRawAxis(Controller.ANALOG_LEFT), false);
            drive2.curvatureDrive(DriverOne.getRawAxis(Controller.TRIGGER_RIGHT), DriverOne.getRawAxis(Controller.ANALOG_LEFT), false);
        }else if (DriverOne.getRawAxis(Controller.TRIGGER_LEFT) > 0){
            drive.curvatureDrive(DriverOne.getRawAxis(Controller.TRIGGER_LEFT)*-1, DriverOne.getRawAxis(Controller.ANALOG_LEFT)*-1, false);
            drive2.curvatureDrive(DriverOne.getRawAxis(Controller.TRIGGER_LEFT)*-1, DriverOne.getRawAxis(Controller.ANALOG_LEFT)*-1, false);
        }

        if (DriverOne.getRawButton(Controller.BUMPER_LEFT)){
            BucketSolenoid.set(true);
        } else {
            BucketSolenoid.set(false);
        }

        if (DriverOne.getRawButton(Controller.BUMPER_RIGHT)) {
            LipSolenoid.set(true);
        } else {
            LipSolenoid.set(false);
        }

        //Tick based timer for wheel toggle
        if(DriverTwo.getRawButton(Controller.BUTTON_A)) {
            WheelTicker++;
        } else {
            WheelTicker = 0;
        }

        //wheel pneumatics
        if (WheelTicker == 5 && !WheelOn) {
            WheelOn = true;
        } else if (WheelTicker == 5) {
            WheelOn = false;
        }

        WheelSolenoid.set(WheelOn);

        //SCANDALOUS WENCH
        if (DriverTwo.getRawAxis(Controller.TRIGGER_RIGHT) > 0){
            SmartDashboard.putString("trigger right","true");
 
            rightWench.set(0.5);
            leftWench.set(0.5);
            //rightWench.set(ControlMode.PercentOutput, 0.2);
            //leftWench.set(ControlMode.PercentOutput, 0.2);
        }
        else if(DriverTwo.getRawAxis(Controller.TRIGGER_LEFT) > 0){
            rightWench.set(-0.5);
            leftWench.set(-0.5);
            SmartDashboard.putString("trigger left","true");
            //rightWench.set(ControlMode.PercentOutput, -0.2);
            //leftWench.set(ControlMode.PercentOutput, -0.2);
        }else{
            SmartDashboard.putString("trigger default","true");
            rightWench.set(0);
            leftWench.set(0);
            //rightWench.set(ControlMode.PercentOutput, 0);
            //leftWench.set(ControlMode.PercentOutput, 0);
        }

        LiftPWM.setSpeed(0);

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
                drive2.arcadeDrive(0.5, 0);
                break;
            case 90:
                drive.arcadeDrive(0, 0.5);
                drive2.arcadeDrive(0, 0.5);
                break;
            case 180:
                drive.arcadeDrive(-0.5, 0);
                drive2.arcadeDrive(-0.5, 0);
                break;
            case 270:
                drive.arcadeDrive(0, -0.5);
                drive2.arcadeDrive(0, -0.5);
                break;
        }
        if(DriverTwo.getRawButton(Controller.BUMPER_RIGHT)){
            LiftPWM.setSpeed(0.5);
        }else if(DriverTwo.getRawButton(Controller.BUMPER_LEFT)){
            LiftPWM.setSpeed(-0.5);
        }
        //old lift 
        /*switch(POV2) {
            case 90:
                LiftPWM.setSpeed(0.5);
                break;
            case 270:
                LiftPWM.setSpeed(-0.5);
                break;
        }*/
        /*Removing color sensor code - EG
        //Color
        Color detectedColor = rainbowDetector.getColor();
        String colorString;
        ColorMatchResult match = m_colorMatcher.matchClosestColor(detectedColor);

        if (match.color == kBlueTarget) {
            colorString = "Blue";
            colorMendable = 2;
        } else if (match.color == kRedTarget) {
            colorString = "Red";
            colorMendable = 0;
        } else if (match.color == kGreenTarget) {
            colorString = "Green";
            colorMendable = 1;
        } else if (match.color == kYellowTarget) {
            colorString = "Yellow";
            colorMendable = 3;
        } else {
            colorString = "Unknown";
        }

        if (colorString != "Unknown") {
            if (match.confidence < 0.88) {
                colorString += " (NOT CONFIDENT)";

                // set back if we weren't confident
                colorMendable = lastColorMendable;
                ColorSensorIsConfident = false;
            } else {
                ColorSensorIsConfident = true;
            }
        }

        // On Color Change
        if (colorMendable != lastColorMendable && ColorSensorIsConfident) {
            // Check Regarding Color Location
            if (isAutoSpinning) {
                if (colorMendable == AutoSpinColor)
                    isAutoSpinning = false;
            } else if (isRevolving) {
                colorCycles++;

                if (colorCycles >= WHEEL_REVOLUTIONS_BEFORE_STOP * 8) {
                    colorCycles = 0;
                    isRevolving = false;
                }
            }
        }

        if (colorMendable != lastColorMendable && isAutoSpinning && ColorSensorIsConfident) {

            if (colorMendable == AutoSpinColor) {
                isAutoSpinning = false;
            }
        }

        lastColorMendable = colorMendable;

        SmartDashboard.putNumber("Red", detectedColor.red);
        SmartDashboard.putNumber("Green", detectedColor.green);
        SmartDashboard.putNumber("Blue", detectedColor.blue);
        SmartDashboard.putNumber("Confidence", match.confidence);
        SmartDashboard.putString("Detected Color", colorString);
        SmartDashboard.putNumber("Color Cycles", colorCycles);

        if (!isAutoSpinning)
            SmartDashboard.putString("Target Color", "Waiting");

        if (!isRevolving)
            SmartDashboard.putString("Revolving", "Non-Factual");


        // Color Automation.. Thing -IB
        if (!isRevolving) {
            if (DriverTwo.getRawButton(Controller.BUTTON_A) && !isAutoSpinning) {
                SmartDashboard.putString("Target Color", "Green");
                AutoSpinColor = 1;
                isAutoSpinning = true;
            } else if (DriverTwo.getRawButton(Controller.BUTTON_B) && !isAutoSpinning) {
                SmartDashboard.putString("Target Color", "Red");
                AutoSpinColor = 0;
                isAutoSpinning = true;
            } else if (DriverTwo.getRawButton(Controller.BUTTON_X) && !isAutoSpinning) {
                SmartDashboard.putString("Target Color", "Blue");
                AutoSpinColor = 2;
                isAutoSpinning = true;
            } else if (DriverTwo.getRawButton(Controller.BUTTON_Y) && !isAutoSpinning) {
                SmartDashboard.putString("Target Color", "Yellow");
                AutoSpinColor = 3;
                isAutoSpinning = true;
            }
        }

        // Revolve X times
        if (!isAutoSpinning) {
            if (DriverTwo.getRawButton(Controller.BUTTON_LS) && !isRevolving) {
                isRevolving = true;
                SmartDashboard.putString("Revolving", "Factual");
            }
        }

        //failsafe
        if (DriverTwo.getRawButton(Controller.BUTTON_RS)) {
            isAutoSpinning = isRevolving = false;
        }
    */
    }

    /**
     * This function is called periodically during test mode.
     */
    @Override
    public void testPeriodic() {

    }
}
