package task;

import target.Target;
//import userinterface.Communicator;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;

public class SimulationTask extends GPUPTask {


    private final int processTimeInMS;
    private final boolean isRandom;
    private final double successChance;
    private final double warningChance;
    private final Random random;

    public SimulationTask(String taskName, int processTimeInMS, boolean isRandom,
                          double successChance, double warningChance, Target target, ExecutorThread taskManager) {
        super(taskName, target,taskManager);
        this.processTimeInMS = processTimeInMS;
        this.isRandom = isRandom;
        this.successChance = successChance;
        this.warningChance = warningChance;
        this.random = new Random();
    }

    @Override
    public void run() {
        synchronized (this.taskManager.getIsPauseDummy()){
            try {
                while(this.taskManager.getPaused()) {
                    this.taskManager.getIsPauseDummy().wait();
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        try {
            target.setStatus(Target.Status.IN_PROCESS);
            int runTime;
            double randSuccess = random.nextDouble();
            double randWarning = random.nextDouble();
            if (isRandom)
                runTime = random.nextInt(processTimeInMS);
            else
                runTime = processTimeInMS;

            target.setTargetTaskBegin(Instant.now());

            this.taskManager.getTargetGraph().currentTaskLog += "Target " + target.getName() + " is going to sleep for " + runTime + " milliseconds\n\n";
            System.out.println("target " + target.getName() + " is going to sleep for " + runTime + " milliseconds");
            Thread.sleep(runTime);

            target.setTargetTaskEnd(Instant.now());

            if (randSuccess > successChance) {
                target.setResult(Target.Result.FAILURE);
            } else if (randWarning < warningChance)
                target.setResult(Target.Result.WARNING);
            else
                target.setResult(Target.Result.SUCCESS);

            target.setTargetTaskTime(Duration.between(target.getTargetTaskBegin(), target.getTargetTaskEnd()));


            System.out.println("Target " + target.getName() + " woke up with result: " + target.getRunResult().toString() + "\n");
            this.taskManager.getTargetGraph().currentTaskLog += "Target " + target.getName() + " woke up with result: " + target.getRunResult().toString() + "\n\n";

            target.setStatus(Target.Status.FINISHED);

        } catch (InterruptedException exception) {
            System.out.println("target " + target.getName() + " was interrupted! \n");
            target.setStatus(Target.Status.SKIPPED);
            target.setResult(Target.Result.SKIPPED);
        }
    }
}
