
package org.usfirst.frc.team5514.robot;

import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.CounterBase.EncodingType;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
/** 
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
    /*final String defaultAuto = "Default";
    final String customAuto = "My Auto";
    String autoSelected;
    SendableChooser chooser;*/
	
    //makes empty variables 
    Joystick driveStick;
	CANTalon frontLeft, backLeft, frontRight, backRight, ballRotation, ballInOut;
	RobotDrive myDrive;
	Encoder leftEncoder, rightEncoder;
	DigitalInput ballRotationSwitch, frontSwitch;
	int stage = 0;// used for automated programs
	int ballRotationCurrentLimit;

	CameraServer webcam = CameraServer.getInstance();

	UDPServer udpServer;
	boolean autoLineup;
	DriverStation console;
	
	//Used to make sure ballRotation doesn't burn out the motors when lifting the robot
	double ballRotationCurrentTimer, ballRotationSpeed, liftPortcullisSpeed, autoLoopTimer;
	boolean allowBallRotation, performingButton7;

	/**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
        /*chooser = new SendableChooser();
        chooser.addDefault("Default Auto", defaultAuto);
        chooser.addObject("My Auto", customAuto);
        SmartDashboard.putData("Auto choices", chooser);*/
        
        //initialize all motors to their correct CANTalon
        frontLeft = new CANTalon(1);
        backLeft = new CANTalon(2);
        frontRight = new CANTalon(3);
        backRight = new CANTalon(4);
        ballInOut = new CANTalon(5);
        ballRotation = new CANTalon(6);
        
        driveStick = new Joystick(0);//sets our Joystick
        ballRotationCurrentTimer=0;
        //make the DriveTrain use the drive motors
        myDrive = new RobotDrive(frontLeft,backLeft,frontRight,backRight);
        
        //Makes the encoders with each motor
        leftEncoder = new Encoder(1,2,true,EncodingType.k4X);
        rightEncoder = new Encoder(3,4,true,EncodingType.k4X);
        ballRotationSwitch= new DigitalInput(0);
        ballRotationSpeed = .25;
        allowBallRotation=true;
        ballRotationCurrentLimit=300;
        
        performingButton7=false;
        
        invertMotors();//calls function to invert motors
        
        udpServer = new UDPServer(9876,8);
        autoLineup = false;
        udpServer.printLocalHost();
        console = DriverStation.getInstance();
        myDrive.setSafetyEnabled(false);
        //webcam.startAutomaticCapture("cam0");
    }
    /*132-5 length
     * 319/2-48
     * sqrt((132-5)^2+(319/2-48)^2))
     * tan^-1((319/2-48)/(132-5))
     * from The green line^^^^^^^^^
     * 
     * 12+9.25+(191.5-132)
     * from the lowbar^^^^^
     */
    public void invertMotors(){//set all motors to inverted to make the front
    	frontLeft.setInverted(true);//be the front while driving
        backLeft.setInverted(true);
        frontRight.setInverted(true);
        backRight.setInverted(true);
        ballInOut.setInverted(true);
        ballRotation.setInverted(true);
    }
	/**
	 * This autonomous (along with the chooser code above) shows how to select between different autonomous modes
	 * using the dashboard. The sendable chooser code works with the Java SmartDashboard. If you prefer the LabVIEW
	 * Dashboard, remove all of the chooser code and uncomment the getString line to get the auto name from the text box
	 * below the Gyro
	 *
	 * You can add additional auto modes by adding additional comparisons to the switch structure below with additional strings.
	 * If using the SendableChooser make sure to add them to the chooser code above as well.
	 */
    public void autonomousInit() {
    	/*autoSelected = (String) chooser.getSelected();
    	autoSelected = SmartDashboard.getString("Auto Selector", defaultAuto);
		System.out.println("Auto selected: " + autoSelected);*/
    	stage=0;
    	leftEncoder.reset();
    	rightEncoder.reset();
    	autoLoopTimer=System.currentTimeMillis();
    }

    /**
     * This function is called periodically during autonomous
     */

	public void autonomousPeriodic() {
		
		/*switch(stage){
		case(0):
			if(!frontSwitch.get()){
				ballRotation.set(-1);
			}else{
				stage++;
			}
		case(1):
			ballRotation.set(.1);
			try{
				Thread.sleep(250);
			}catch(Exception e){
			}
			stage++;
		case(2):
			myDrive.drive(0.5, 0);
			if(frontSwitch.get()){//Checks for when robot hits wall
				stage++;
			}
		case(3):
			myDrive.drive(-1, 0);
			try{
				Thread.sleep(500);
			}catch(Exception e){
			}
			stage++;
		case(4):
			myDrive.drive(1,-1);
			try{
				Thread.sleep(200);
			}catch(Exception e){
			}
			stage++;
		case(5):	
			myDrive.drive(0.5,0);
			if(frontSwitch.get()){//Checks if we hit the low bar
				stage=4;
				leftEncoder.reset();
				rightEncoder.reset();
			}
		case(6):
			if(leftEncoder.getDistance()<.5){//mess with these numbers for turning radius
				frontLeft.set(.5);
				backLeft.set(.5);
				
			}
			if(rightEncoder.getDistance()<.5){
				frontRight.set(-.5);
				backRight.set(-.5);
			}
			if(rightEncoder.getDistance()>.5&&leftEncoder.getDistance()>.5){
				leftEncoder.reset();
				rightEncoder.reset();
				stage++;
			}
		case(7):
			if(leftEncoder.getDistance() < 6.31526814 && rightEncoder.getDistance() < 6.31526814){
				myDrive.drive(1, 0);
				if(leftEncoder.getDistance() < rightEncoder.getDistance()){//if the left side is going slower than the right side
					frontLeft.set(0.9);
					backLeft.set(0.9);
				}
				else if(leftEncoder.getDistance() > rightEncoder.getDistance()){//vice versa
					frontRight.set(0.9);
					backRight.set(0.9);
				}
				else{
					myDrive.drive(1, 0);
				}
			}else{
				stage++;
			}
		case(8):
			shootBall();
			stage++;
	
		case(9):
			//drive away from goal and go somewhere where no on will be
			myDrive.drive(-0.5, 0);
			try{
				Thread.sleep(500);
			}catch(Exception e){
			}
			myDrive.drive(1, 1);
			try{
				Thread.sleep(500);
			}catch(Exception e){
			}
			myDrive.drive(1, 0);
			try{
				Thread.sleep(1500);
			}catch(Exception e){
			}
			stage++;
		default:
			System.out.println("Did we score?");
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
		}*/
		
		
		
		/*if(leftEncoder.getDistance()<2.2){
			if(leftEncoder.getDistance()==rightEncoder.getDistance()){
				myDrive.drive(.5, 0);
			}else if(leftEncoder.getDistance()>rightEncoder.getDistance()){
				myDrive.drive(.5,-.3);
			}else{
				myDrive.drive(.5,.3);
			}
		}else{
			System.out.println("Did we cross?");
			try{
				Thread.sleep(500);
			}catch(Exception e){
			} 
		}*/
		if(System.currentTimeMillis()-autoLoopTimer<8000/*time driving foward in miliseconds*/){
			myDrive.drive(-.25/*speed(minus is forward)*/,0);
		}else{
			myDrive.drive(0, 0);
			System.out.print("Did we cross?");
			try{
				Thread.sleep(500);
			}catch(Exception e){
			}
		}
	}

    
    public void teleopInit(){
    	stage=0;
    }
    
    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
    	/*if(driveStick.getRawButton(7) || performingButton7){//auto shooter
    		shootBall();
    	}else{
	    	//This is a safety function for the Cim used to rotate the ball Arm
	    	if(ballRotation.getOutputCurrent()>ballRotationCurrentLimit){//if current Amps going to motor is more than some amount
	    		if(ballRotationCurrentTimer!=100){//if this isn't the first time going through
	    			if(System.currentTimeMillis()-ballRotationCurrentTimer>=5000){//if the motor has been running at high amps for 5 or more seconds
	    				allowBallRotation=false;//disallow ballRotation to not destroy the motors
	    			}
	    		}else{
	    			ballRotationCurrentTimer=System.currentTimeMillis();//start getting a time for check
	    		}
	    	}else{//if amps is in a safe zone
	    		allowBallRotation=true;//allow ball rotator to move freely
	    		ballRotationCurrentTimer=100;//set check timer to a default value
	    	}
	    		
	        myDrive.arcadeDrive(driveStick.getY()*-((driveStick.getThrottle()-1)/2-.5), driveStick.getX()*-((driveStick.getThrottle()-1)/2-.5));
	        
	        if(allowBallRotation){//if ball rotator should be able to spin freely, then let it
		        switch(driveStick.getPOV()){//gets position of the top joystick button
		        	case(0)://if the button is up move ball intake upwards
		        		ballRotation.set(-ballRotationSpeed);
		        		break;
		        	case(180)://if the button is down move ball intake downwards
		        		ballRotation.set(ballRotationSpeed);
		        		break;
		        	default:// if the button is not being pushed up or down make the ball intake stop
		        		//for lifting the portThingy 
		        		ballRotation.set(-liftPortcullisSpeed);
		        }
		    }else{//if motor is in danger of hurting themselves
	        	ballRotation.set(0);//take all stress on motors away by turning it off
	        }
	
	        if(driveStick.getRawButton(2)){// if the trigger is being pushed launch the ball
	        	ballInOut.set(1);
	        }else if(driveStick.getRawButton(1)){//if the second button is pushed
	        	ballInOut.set(-1);				 // intake the ball
	        }else{//if neither are pushed don't move the ball
	        	ballInOut.set(0);
	        }*/
	        
	        
	        if(driveStick.getRawButton(5)){
	        	DriverStation.reportWarning("Entered Loop", false);
	        	if(driveStick.getRawButton(4)){
	        		DriverStation.reportWarning("Button 4= ", false);
	        		autoLineup = false;
	        	}else{
	        		autoLineup = true;
	        	}
	        	
	        	//recieves new packet
	        	DriverStation.reportWarning("Before ", false);
	        	udpServer.getPacket();
	        	DriverStation.reportWarning("After ", false);
	        	udpServer.printLocalHost();
	        	int offset = udpServer.getOffset();
	        	double angleRad = udpServer.getAngleRad();
	        	DriverStation.reportWarning(udpServer.toString(), false);
	        	DriverStation.reportWarning("Offset = " + offset, false);
	        	DriverStation.reportWarning("Angle = " + udpServer.getAngleDeg(), false);
	        	if(offset > 10){
	        		myDrive.drive(.6, 1);//first = speed from -1 to 1; second = curve where negative is left
	        	}else if(offset < -10){
	        		myDrive.drive(.6,-1);
	        	}else{
	        		myDrive.drive(0, 0);
	        		autoLineup = false;
	        		System.out.println("Lined up");
	        	}
	        }else{
	        	myDrive.drive(0, 0);
	        }
    	}
    //}
    
    public void shootBall(){
		switch(stage){
		case(0):
			performingButton7=true;
			stage++;
			break;
		case(1):
			ballRotation.set(ballRotationSpeed);//bring ball to the ground
			try {
				Thread.sleep(850);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			stage++;
			break;
		case(2):
			ballRotation.set(-ballRotationSpeed);//lift the ball rotation up a bit
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			ballRotation.set(0);
			stage++;
		case(3):
    		stage++;
    	case(4):
    		ballInOut.set(1);//run ball shooter
    	try {
			Thread.sleep(200);//lets it get to speed
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	myDrive.drive(-.5, 0);//drive backwards
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	stage++;
		default:
			performingButton7=false;
			myDrive.drive(0, 0);
			stage=0;
			break;
		}
	
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
    	LiveWindow.run();
    }
    
}