# robot-dominator

This is the code for **Dominator**, one of the two robots built by FTC team 26532,
the Happy Haptic Doctors (Hanover, NH). The other robot has its own repo in the
same GitHub organisation, `happyhapticdoctors`. The two robots are different
designs and their code is independent.

Everything the team writes lives in `TeamCode/`. Everything else in this repo is
the FIRST Tech Challenge SDK, pulled from the `upstream` remote.

The people you work with are high-school students. They want to build a robot,
not learn software engineering. Handle git, builds and deploys for them, keep
the code readable, and explain what you did in two or three plain sentences.

## The robot

Keep this section current. When you learn something new about the hardware from
the student or from the code, update it here.

- **Drive:** four-motor mecanum, robot-centric.
- **Motors (DC):** `frontLeftMotor`, `frontRightMotor`, `backLeftMotor`,
  `backRightMotor`. Left side is REVERSED, right side FORWARD, all set to BRAKE
  at zero power.
- **Intake:** DC motor `intake` on the Expansion Hub, motor port 0, no encoder,
  runs without encoder. It drives through a two-gear train, so the motor is
  REVERSED to make positive power suck in.
- **Servos and sensors:** none yet.
- **Control Hub:** reachable over Wi-Fi at `192.168.43.1:5555`.

Hardware names above come from `BasicTeleOp.java` and must match the
configuration on the Driver Station "Configure Robot" screen. If a name in the
code and a name on the hub disagree, the hub is right; fix the code.

## Starting, deploying, finishing

**When a session starts:** run `git pull` before touching anything, so the
student is working on the latest code. Mention briefly if anything changed
since their last commit.

**To deploy:** connect to the hub and install the TeamCode app.

```
adb connect 192.168.43.1:5555
gradlew :TeamCode:installDebug
```

(On Windows use `gradlew.bat`.) If the hub is not reachable, say so plainly and
stop; do not keep retrying. The student may need to join the robot's Wi-Fi.

**When the student is done, or says "save":** build, commit, push.

1. Build with `gradlew :TeamCode:assembleDebug`. If it fails, fix it. Never
   push code that does not build.
2. Commit with a message in the student's words about what the robot does
   differently now (see below).
3. `git push`. If the push is rejected because someone else pushed first, run
   `git pull`, resolve any conflict, build again, and push. Do this without
   asking unless a conflict needs a human decision.

## Guardrails

- Only change files under `TeamCode/`. Never edit the SDK modules or the
  Gradle files unless the student explicitly asks.
- Never force push. Never rewrite history that has been pushed.
- Never delete a file without saying which file and why first.
- Do not fetch or merge from `upstream` unless asked. SDK updates are a
  deliberate step done with a mentor.
- The other robot's repo is for reading and porting from, not editing. If
  asked to port something, read it with `gh api` or `gh repo clone` into a
  temporary folder, and adapt it here.

## How to write

**Commit messages** describe what the robot does differently, in the words a
student would use. Good: "Intake now spits out when right bumper is held."
Bad: "Refactor intake logic." One line is usually enough.

**Code** should be readable by a fifteen-year-old who did not write it.

- Plain names. `liftMotor`, not `lm`.
- A tuned number gets a comment saying what it does and how it was chosen.
- Constants at the top of the file, as `BasicTeleOp.java` does.
- No abstractions for their own sake. One op mode that reads top to bottom
  beats a class hierarchy.
- Gamepad controls are documented in the class comment at the top of each
  op mode, so a driver can read it and know the buttons.

**Talking to the student:** say what you did and what to test on the robot.
Skip the reasoning unless asked. If something they asked for is a bad idea
for the robot, say so in one sentence and offer the better version.

## End of session

Before the session ends, write three short lines the student can paste into
the team's engineering notebook:

- What we tried
- What happened
- What's next

Offer to include them in the commit message as well.
