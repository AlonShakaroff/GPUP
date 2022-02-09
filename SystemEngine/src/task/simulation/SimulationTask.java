package task.simulation;

import target.Target;
import target.TargetForWorker;
import task.GPUPTask;

import java.util.Random;

public class SimulationTask extends GPUPTask {

    private final int processTimeInMS;
    private final boolean isRandom;
    private final double successChance;
    private final double warningChance;
    private final Random random;

    public SimulationTask(String taskName, int processTimeInMS, boolean isRandom, double successChance,
                            double warningChance, TargetForWorker target) {
        super(taskName, target, "Simulation");
        this.processTimeInMS = processTimeInMS;
        this.isRandom = isRandom;
        this.successChance = successChance;
        this.warningChance = warningChance;
        this.random = new Random();
    }

    @Override
    public void run() {
        try {
            target.setStatus(Target.Status.IN_PROCESS);
            int runTime;
            double randSuccess = random.nextDouble();
            double randWarning = random.nextDouble();
            if (isRandom)
                runTime = random.nextInt(processTimeInMS);
            else
                runTime = processTimeInMS;


            target.setRunLog(target.getRunLog().concat("Target " + target.getName() + " is going to sleep for " + runTime + " milliseconds\n\n"));

            Thread.sleep(runTime);

            if (randSuccess > successChance) {
                target.setResult(Target.Result.FAILURE);
            } else if (randWarning < warningChance)
                target.setResult(Target.Result.WARNING);
            else
                target.setResult(Target.Result.SUCCESS);



            target.setRunLog(target.getRunLog().concat("Target " + target.getName() + " woke up with result: " + target.getResult().toString() + "\n\n"));

            target.setStatus(Target.Status.FINISHED);

        } catch (InterruptedException exception) {
            target.setRunLog(target.getRunLog().concat("Target " + target.getName() + " was interrupted! \n\n"));
            target.setStatus(Target.Status.SKIPPED);
            target.setResult(Target.Result.SKIPPED);
        }
    }
}
