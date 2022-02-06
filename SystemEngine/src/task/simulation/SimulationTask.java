package task.simulation;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import target.Target;
import task.ExecutorThread;
import task.GPUPTask;
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
                          double successChance, double warningChance, Target target, ExecutorThread taskManager, TextArea runLogTextArea) {
        super(taskName, target,taskManager,runLogTextArea);
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
            catch(Exception ignore){}
        }
        try {
            target.setStatus(Target.Status.IN_PROCESS);
            target.setStartTimeInCurState();
            target.setTargetTaskBegin(Instant.now());

            int runTime;
            double randSuccess = random.nextDouble();
            double randWarning = random.nextDouble();
            if (isRandom)
                runTime = random.nextInt(processTimeInMS);
            else
                runTime = processTimeInMS;


            Platform.runLater(()->{runLogTextArea.appendText("Target " + target.getName() + " is going to sleep for " + runTime + " milliseconds\n\n"); });

            Thread.sleep(runTime);

            if (randSuccess > successChance) {
                target.setResult(Target.Result.FAILURE);
            } else if (randWarning < warningChance)
                target.setResult(Target.Result.WARNING);
            else
                target.setResult(Target.Result.SUCCESS);



            Platform.runLater(()->{runLogTextArea.appendText("Target " + target.getName() + " woke up with result: " + target.getRunResult().toString() + "\n\n"); });

            target.setStatus(Target.Status.FINISHED);

        } catch (InterruptedException exception) {
            Platform.runLater(()->{runLogTextArea.appendText("Target " + target.getName() + " was interrupted! \n\n"); });
            target.setStatus(Target.Status.SKIPPED);
            target.setResult(Target.Result.SKIPPED);
        }
        finally {
            target.setTargetTaskEnd(Instant.now());
            target.setTargetTaskTime(Duration.between(target.getTargetTaskBegin(),target.getTargetTaskEnd()));
        }
    }
}
