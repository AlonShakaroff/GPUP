package task;

import target.Target;
//import userinterface.Communicator;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;

public class SimulationTask extends Task {


    private final int processTimeInMS;
    private final boolean isRandom;
    private final double successChance;
    private final double warningChance;
    private final Random random;

    public SimulationTask(String taskName, int processTimeInMS, boolean isRandom,
                          double successChance, double warningChance, Target target) {
        super(taskName, target);
        this.processTimeInMS = processTimeInMS;
        this.isRandom = isRandom;
        this.successChance = successChance;
        this.warningChance = warningChance;
        this.random = new Random();
    }

    @Override
    public void run() {
        int runTime;
        double randSuccess = random.nextDouble();
        double randWarning = random.nextDouble();
        if (isRandom)
            runTime = random.nextInt(processTimeInMS);
        else
            runTime = processTimeInMS;

        target.setTargetTaskBegin(Instant.now());

        System.out.println("target " + target.getName() + " is going to sleep");
        try { Thread.sleep(runTime);
        } catch (InterruptedException ignored) { }
        target.setTargetTaskEnd(Instant.now());
        System.out.println("target " + target.getName() + " woke up");

        if (randSuccess > successChance){
            target.setResult(Target.Result.FAILURE);
        }
        else if (randWarning < warningChance)
            target.setResult(Target.Result.WARNING);
        else
            target.setResult(Target.Result.SUCCESS);
        target.setTargetTaskTime(Duration.between(target.getTargetTaskBegin(),
                target.getTargetTaskEnd()));

        target.setStatus(Target.Status.FINISHED);
    }
}
