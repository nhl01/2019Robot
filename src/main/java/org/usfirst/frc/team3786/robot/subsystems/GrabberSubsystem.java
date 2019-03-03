package org.usfirst.frc.team3786.robot.subsystems;

import org.usfirst.frc.team3786.robot.Dashboard;

import edu.wpi.first.wpilibj.command.Subsystem;

import com.ctre.phoenix.motorcontrol.LimitSwitchNormal;
import com.ctre.phoenix.motorcontrol.LimitSwitchSource;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import org.usfirst.frc.team3786.robot.Mappings;

public class GrabberSubsystem extends Subsystem {

	private static GrabberSubsystem instance;

	private WPI_TalonSRX grabber;
	private WPI_TalonSRX rightGripper;
	private WPI_TalonSRX leftGripper;
	private WPI_TalonSRX tilt;

	public static GrabberSubsystem getInstance() {
		if (instance == null)
			instance = new GrabberSubsystem();
		return instance;
	}

	public GrabberSubsystem() {
		grabber = new WPI_TalonSRX(Mappings.grabberMotor);
		grabber.setSafetyEnabled(false);
		grabber.configPeakCurrentLimit(30);
		grabber.setNeutralMode(NeutralMode.Brake);
		//grabber.configForwardLimitSwitchSource(LimitSwitchSource.Deactivated, LimitSwitchNormal.NormallyOpen);
		grabber.configReverseLimitSwitchSource(LimitSwitchSource.Deactivated, LimitSwitchNormal.Disabled);
		grabber.configForwardLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen);
		//grabber.configReverseLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen);

		rightGripper = new WPI_TalonSRX(Mappings.rightGripper);
		rightGripper.setSafetyEnabled(false);
		rightGripper.configPeakCurrentLimit(20);

		leftGripper = new WPI_TalonSRX(Mappings.leftGripper);
		leftGripper.setSafetyEnabled(false);
		leftGripper.configPeakCurrentLimit(20);

		tilt = new WPI_TalonSRX(Mappings.tilt);
		tilt.setSafetyEnabled(false);
		tilt.configPeakCurrentLimit(40);
		tilt.configContinuousCurrentLimit(30);
		tilt.setNeutralMode(NeutralMode.Brake);
	}

	public double getGrabberCurrent() {
		return grabber.getOutputCurrent();
	}

	public void initDefaultCommand() {
		// Set the default command for a subsystem here.
		// setDefaultCommand(new MySpecialCommand());
	}

	public void setGrabberSpeed(double speed) {
		grabber.set(-speed);
		Dashboard.getInstance().putNumber(false, "Grabber Speed", speed);
	}

	public void setGripperSpeed(double speed) {
		rightGripper.set(-speed);
		leftGripper.set(-speed);
		Dashboard.getInstance().putNumber(true, "Gripper Speed", speed);
	}

	public void setTiltSpeed(double speed) {
		tilt.set(-speed);
		Dashboard.getInstance().putNumber(false, "Tilter Speed", speed);
	}

	public void setBrake(boolean brake) {
		if (brake) {
			rightGripper.setNeutralMode(NeutralMode.Brake);
			leftGripper.setNeutralMode(NeutralMode.Brake);
		} else {
			rightGripper.setNeutralMode(NeutralMode.Coast);
			leftGripper.setNeutralMode(NeutralMode.Coast);
		}
	}

}
