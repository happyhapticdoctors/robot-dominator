package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * First team OpMode: four-motor mecanum drive plus an intake.
 *
 * Drive (robot-centric):
 *   Left stick Y  = forward / back
 *   Left stick X  = strafe left / right
 *   Right stick X = rotate
 *   X button (hold) = turbo: full speed. Otherwise the drive is limited to 60%.
 *   Straight assist: if the left stick is within 10 degrees of straight
 *   forward or back, the strafe is ignored so the robot drives dead straight.
 * Intake:
 *   Right trigger (press once) toggles the intake on/off.
 *   Right bumper (hold) runs the intake in reverse to spit game pieces back out.
 *
 * Hardware configuration (set on the Driver Station "Configure Robot" screen):
 *   - DC motors named "frontLeftMotor", "frontRightMotor", "backLeftMotor", "backRightMotor"
 *   - DC motor named "intake"  (Expansion Hub, motor port 0, no encoder)
 *
 * Testing motor directions: push the left stick forward and watch each wheel.
 * Any wheel that spins backward needs its setDirection() line flipped below.
 * Then push the left stick right: the robot should strafe right. If it spins
 * or wobbles instead, a wheel is still reversed.
 */
@TeleOp(name = "Basic TeleOp", group = "TeamCode")
public class BasicTeleOp extends LinearOpMode {

    /** Intake power while running forward (0.0 to 1.0). Lower it if the intake is too aggressive. */
    private static final double INTAKE_POWER = 1.0;

    /** Drive speed multiplier when the X button is NOT held (0.0 to 1.0). */
    private static final double NORMAL_DRIVE_SPEED = 0.6;

    /** Drive speed multiplier while the X button IS held (turbo mode). */
    private static final double TURBO_DRIVE_SPEED = 1.0;

    /** If the left stick is within this many degrees of straight forward/back, drive straight. */
    private static final double STRAIGHT_SNAP_DEGREES = 10.0;

    /** Ignore the straight-drive snap when the stick is barely moved (avoids noise near center). */
    private static final double STRAIGHT_SNAP_DEADBAND = 0.1;

    private final ElapsedTime runtime = new ElapsedTime();
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;
    private DcMotor intake;

    /** True when the driver has toggled the intake on with the right trigger. */
    private boolean intakeOn = false;

    @Override
    public void runOpMode() {
        // --- INIT: runs once when the driver presses INIT ---
        frontLeft  = hardwareMap.get(DcMotor.class, "frontLeftMotor");
        frontRight = hardwareMap.get(DcMotor.class, "frontRightMotor");
        backLeft   = hardwareMap.get(DcMotor.class, "backLeftMotor");
        backRight  = hardwareMap.get(DcMotor.class, "backRightMotor");
        intake     = hardwareMap.get(DcMotor.class, "intake");

        // Motors on the left side face the opposite way from the right side, so
        // the left side is reversed. Adjust after your first test drive.
        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        backLeft.setDirection(DcMotor.Direction.REVERSE);
        frontRight.setDirection(DcMotor.Direction.FORWARD);
        backRight.setDirection(DcMotor.Direction.FORWARD);

        // Actively hold position when power is zero instead of coasting.
        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // The intake motor drives through a two-gear train, which flips the
        // rotation, so the motor is REVERSED to make positive power suck in.
        // The intake has no encoder wire, so tell the SDK not to expect one.
        intake.setDirection(DcMotor.Direction.REVERSE);
        intake.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        telemetry.addData("Status", "Initialized - press START");
        telemetry.update();

        waitForStart();
        runtime.reset();

        // --- LOOP: runs repeatedly until the driver presses STOP ---
        while (opModeIsActive()) {
            // ---- Mecanum drive ----
            // Gamepad Y axis is inverted: pushing forward gives a negative value.
            double axial   = -gamepad1.left_stick_y;   // forward / back
            double lateral =  gamepad1.left_stick_x;   // strafe
            double yaw     =  gamepad1.right_stick_x;  // rotate

            // Straight-drive assist: if the left stick is within STRAIGHT_SNAP_DEGREES
            // of straight forward or back, drop the strafe so the robot drives
            // perfectly straight instead of drifting sideways from a slightly off stick.
            double stickMagnitude = Math.hypot(axial, lateral);
            boolean snappedStraight = false;
            if (stickMagnitude > STRAIGHT_SNAP_DEADBAND) {
                double degreesFromStraight = Math.toDegrees(Math.atan2(Math.abs(lateral), Math.abs(axial)));
                if (degreesFromStraight <= STRAIGHT_SNAP_DEGREES) {
                    axial = Math.copySign(stickMagnitude, axial); // keep the same speed
                    lateral = 0.0;
                    snappedStraight = true;
                }
            }

            // Turbo: full speed while X is held, otherwise a calmer 60%.
            boolean turbo = gamepad1.x;
            double speed = turbo ? TURBO_DRIVE_SPEED : NORMAL_DRIVE_SPEED;

            double frontLeftPower  = speed * (axial + lateral + yaw);
            double frontRightPower = speed * (axial - lateral - yaw);
            double backLeftPower   = speed * (axial - lateral + yaw);
            double backRightPower  = speed * (axial + lateral - yaw);

            // Scale everything down together if any wheel would exceed 100%,
            // so the robot keeps the intended direction instead of clipping.
            double max = Math.max(Math.abs(frontLeftPower), Math.abs(frontRightPower));
            max = Math.max(max, Math.abs(backLeftPower));
            max = Math.max(max, Math.abs(backRightPower));
            if (max > 1.0) {
                frontLeftPower  /= max;
                frontRightPower /= max;
                backLeftPower   /= max;
                backRightPower  /= max;
            }

            frontLeft.setPower(frontLeftPower);
            frontRight.setPower(frontRightPower);
            backLeft.setPower(backLeftPower);
            backRight.setPower(backRightPower);

            // ---- Intake ----
            // rightTriggerWasPressed() is true for exactly one loop per squeeze,
            // so holding the trigger down does not rapidly flip the intake on and off.
            if (gamepad1.rightTriggerWasPressed()) {
                intakeOn = !intakeOn;
            }

            double intakePower;
            String intakeState;
            if (gamepad1.right_bumper) {
                // Reverse takes priority while the bumper is held.
                intakePower = -INTAKE_POWER;
                intakeState = "REVERSE";
            } else if (intakeOn) {
                intakePower = INTAKE_POWER;
                intakeState = "ON";
            } else {
                intakePower = 0.0;
                intakeState = "OFF";
            }
            intake.setPower(intakePower);

            // ---- Telemetry ----
            telemetry.addData("Run Time", runtime.toString());
            telemetry.addData("Drive", "%s (%.0f%%)%s", turbo ? "TURBO" : "NORMAL", speed * 100,
                    snappedStraight ? " STRAIGHT" : "");
            telemetry.addData("Front", "left (%.2f), right (%.2f)", frontLeftPower, frontRightPower);
            telemetry.addData("Back",  "left (%.2f), right (%.2f)", backLeftPower, backRightPower);
            telemetry.addData("Intake", "%s (%.2f)", intakeState, intakePower);
            telemetry.update();
        }
    }
}
