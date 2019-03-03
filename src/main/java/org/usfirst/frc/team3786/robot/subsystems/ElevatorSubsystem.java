
package org.usfirst.frc.team3786.robot.subsystems;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import org.usfirst.frc.team3786.robot.Dashboard;
import org.usfirst.frc.team3786.robot.Mappings;
import org.usfirst.frc.team3786.robot.Robot;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class ElevatorSubsystem extends Subsystem {

	private static ElevatorSubsystem instance;

	private CANSparkMax rightElevator;
	private CANSparkMax leftElevator;
	private DigitalInput dIn;

	public static final double upMultiplier = 0.8;
	public static final double downMultiplier = 0.55;
	private static final double downMultiplierLow = 0.25 / downMultiplier;
	

	public static final double upAutoMultiplier = 0.8;
	public static final double downAutoMultiplier = 1.0;

	private static final int rotationsAcceptableRange = 10;

	private boolean autoDone = true;
    private double targetLevel;
	private ElevatorSubsystem.VerticalDirection direction;

	public static ElevatorSubsystem getInstance() {
		if (instance == null)
			instance = new ElevatorSubsystem();
		return instance;
	}

	public ElevatorSubsystem() {
		rightElevator = new CANSparkMax(Mappings.rightElevator, MotorType.kBrushless);
		rightElevator.setIdleMode(IdleMode.kBrake);
		rightElevator.setSmartCurrentLimit(30);
		rightElevator.setOpenLoopRampRate(0.2);
		rightElevator.getEncoder().setPosition(0.0);

		leftElevator = new CANSparkMax(Mappings.leftElevator, MotorType.kBrushless);
		leftElevator.setIdleMode(IdleMode.kBrake);
		leftElevator.setSmartCurrentLimit(30);
		leftElevator.setOpenLoopRampRate(0.2);
		leftElevator.getEncoder().setPosition(0.0);

		dIn = new DigitalInput(5);
	}

	@Override
	public void initDefaultCommand() {
	}

	public void safetyRun() {
		//System.out.println("Right: " + rightElevator.getEncoder().getPosition());
		if (!autoDone)
			runAuto();
		if (!dIn.get() && rightElevator.get() < 0) {
			rightElevator.set(0);
			leftElevator.set(0);
		}
		if (!dIn.get()) {
			rightElevator.getEncoder().setPosition(0.0);
			leftElevator.getEncoder().setPosition(0.0);
		}
		if (rightElevator.getEncoder().getPosition() < 50 && rightElevator.get() < 0) {
			rightElevator.set(rightElevator.get() * downMultiplierLow);
			leftElevator.set(leftElevator.get() * downMultiplierLow);
		}
	}

	public void setElevatorSpeed(double speed) {
		if (speed > 0)
			speed *= upMultiplier;
		else
			speed *= downMultiplier;
		rightElevator.set(speed);
		leftElevator.set(-speed);
		Dashboard.getInstance().putNumber(false, "Elevator Speed", speed);
	}

	public void setElevatorPos(double position) {
		if (position < 0)
			position = 0;
		rightElevator.getEncoder().setPosition(position);
		leftElevator.getEncoder().setPosition(-position);
	}

	public double getRotation() { // avg of right and left
		double right = rightElevator.getEncoder().getPosition();
		double left = -leftElevator.getEncoder().getPosition();
		return right;
	}

	public double getHeight() {
		return getRotation();
	}

	public HatchLevels getHatchLevelUp() {
		double currentMotorRotations = getRotation();
		if (!autoDone)
			currentMotorRotations = targetLevel;
		for (HatchLevels level : HatchLevels.values()) {
			if ((currentMotorRotations + rotationsAcceptableRange) < level.getRotations()) {
				System.out.println("Level: " + level.getRotations());
				return level;
			}
		}
		return null;
	}

	public HatchLevels getHatchLevelDown() {
		double currentMotorRotations = getRotation();
		if (!autoDone)
			currentMotorRotations = targetLevel;
		for (int i = HatchLevels.values().length - 1; i >= 0; i--) {
			HatchLevels level = HatchLevels.get(i);
			if ((currentMotorRotations - rotationsAcceptableRange) > level.getRotations()) {
				System.out.println("Level: " + level.getRotations());
				return level;
			}
		}
		return null;
	}

	public BallLevels getBallLevelUp() {
		double currentMotorRotations = getRotation();
		if (!autoDone)
			currentMotorRotations = targetLevel;
		for (BallLevels level : BallLevels.values()) {
			if ((currentMotorRotations + rotationsAcceptableRange) < level.getRotations()) {
				System.out.println("Level: " + level.getRotations());
				return level;
			}
		}
		return null;
	}

	public BallLevels getBallLevelDown() {
		double currentMotorRotations = getRotation();
		if (!autoDone)
			currentMotorRotations = targetLevel;
		for (int i = BallLevels.values().length - 1; i >= 0; i--) {
			BallLevels level = BallLevels.get(i);
			if ((currentMotorRotations - rotationsAcceptableRange) > level.getRotations()) {
				System.out.println("Level: " + level.getRotations());
				return level;
			}
		}
		return null;
	}

	public void incrementHatchLevel() {
		HatchLevels targetLevel = ElevatorSubsystem.getInstance().getHatchLevelUp();
		if (targetLevel == null)
			return;
		direction = ElevatorSubsystem.VerticalDirection.UP;
		if (targetLevel == HatchLevels.ZERO)
			SmartDashboard.putString("Elevator Level", targetLevel.toString());
		else
			SmartDashboard.putString("Elevator Level", "HATCH " + targetLevel.toString());
		this.targetLevel = targetLevel.getRotations();
        if (autoDone) {
            autoDone = false;
			Robot.elevatorRunCommand.cancel();
        }
	}

	public void decrementHatchLevel() {
		HatchLevels targetLevel = ElevatorSubsystem.getInstance().getHatchLevelDown();
		if (targetLevel == null)
			return;
        direction = ElevatorSubsystem.VerticalDirection.DOWN;
		if (targetLevel == HatchLevels.ZERO)
			SmartDashboard.putString("Elevator Level", targetLevel.toString());
		else
			SmartDashboard.putString("Elevator Level", "HATCH " + targetLevel.toString());
		this.targetLevel = targetLevel.getRotations();
        if (autoDone) {
            autoDone = false;
			Robot.elevatorRunCommand.cancel();
        }
	}

	public void incrementBallLevel() {
		BallLevels targetLevel = ElevatorSubsystem.getInstance().getBallLevelUp();
		if (targetLevel == null)
			return;
		direction = ElevatorSubsystem.VerticalDirection.UP;
		if (targetLevel == BallLevels.ZERO)
			SmartDashboard.putString("Elevator Level", targetLevel.toString());
		else
			SmartDashboard.putString("Elevator Level", "BALL " + targetLevel.toString());
		this.targetLevel = targetLevel.getRotations();
        if (autoDone) {
            autoDone = false;
			Robot.elevatorRunCommand.cancel();
        }
	}

	public void decrementBallLevel() {
		BallLevels targetLevel = ElevatorSubsystem.getInstance().getBallLevelDown();
		if (targetLevel == null)
			return;
        direction = ElevatorSubsystem.VerticalDirection.DOWN;
		if (targetLevel == BallLevels.ZERO)
			SmartDashboard.putString("Elevator Level", targetLevel.toString());
		else
			SmartDashboard.putString("Elevator Level", "BALL " + targetLevel.toString());
		this.targetLevel = targetLevel.getRotations();
        if (autoDone) {
            autoDone = false;
			Robot.elevatorRunCommand.cancel();
        }
	}

	public void runAuto() {
        if (!autoDone) {
			double currentMotorRotations = ElevatorSubsystem.getInstance().getRotation();
            if (currentMotorRotations < targetLevel && direction == ElevatorSubsystem.VerticalDirection.UP) {
                ElevatorSubsystem.getInstance().setElevatorSpeed(ElevatorSubsystem.upAutoMultiplier);
            } else if (currentMotorRotations > targetLevel && direction == ElevatorSubsystem.VerticalDirection.DOWN) {
                ElevatorSubsystem.getInstance().setElevatorSpeed(-ElevatorSubsystem.downAutoMultiplier);
            } else {
				autoDone = true;
				ElevatorSubsystem.getInstance().setElevatorSpeed(0);
				System.out.println("Done");
				Robot.elevatorRunCommand.start();
            }
        }
	}

	public enum VerticalDirection {
		UP, DOWN, STOP;
	}

	public enum HatchLevels {
		//ZERO(0.0), ONE(52.7), TWO(202.7), THREE(343.6);
		ZERO(0, 0.0), ONE(1, 25.8), TWO(2, 182.7), THREE(3, 326.2);

		private int level;
		private double rotations;

		HatchLevels(int level, double rotations) {
			this.level = level;
			this.rotations = rotations;
		}

		public int getLevel() {
			return level;
		}

		public static HatchLevels get(int level) {
			for (HatchLevels levels : values()) {
				if (levels.getLevel() == level)
					return levels;
			}
			return null;
		}

		public double getRotations() {
			return rotations;
		}

		public HatchLevels getLevels() {
			return values()[ordinal()];
		}

		public HatchLevels up() {
			return values()[ordinal() + 1];
		}

		public HatchLevels down() {
			return values()[ordinal() - 1];
		}

		public HatchLevels stop() {
			return values()[ordinal() + 0];
		}
	}

	public enum BallLevels {
		ZERO(0, 0.0), ONE(1, 78.3), TWO(2, 222.9), THREE(3, 371.8);

		private int level;
		private double rotations;

		BallLevels(int level, double rotations) {
			this.level = level;
			this.rotations = rotations;
		}

		public int getLevel() {
			return level;
		}

		public static BallLevels get(int level) {
			for (BallLevels levels : values()) {
				if (levels.getLevel() == level)
					return levels;
			}
			return null;
		}

		public double getRotations() {
			return rotations;
		}

		public BallLevels getLevels() {
			return values()[ordinal()];
		}

		public BallLevels up() {
			return values()[ordinal() + 1];
		}

		public BallLevels down() {
			return values()[ordinal() - 1];
		}

		public BallLevels stop() {
			return values()[ordinal() + 0];
		}
	}
}
