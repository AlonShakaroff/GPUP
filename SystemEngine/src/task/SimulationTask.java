package task;

import target.Target;
//import userinterface.Communicator;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;

public class SimulationTask extends Task{
    private final int processTimeInMS;
    private final boolean isRandom;
    private final float successChance;
    private final float warningChance;
    private final Random random;

    public SimulationTask(int processTimeInMS,boolean isRandom,float successChance,float warningChance/*, Communicator communicator*/){
        super(/*communicator,*/"Simulation task" );
        this.isRandom  = isRandom;
        this.processTimeInMS = processTimeInMS;
        this.successChance = successChance;
        this.warningChance = warningChance;
        random = new Random();
    }
    @Override
    public void runTaskOnTarget(Target target) {
        int runTime;
        float randSuccess = random.nextFloat();
        float randWarning = random.nextFloat();
        if (isRandom)
            runTime = random.nextInt(processTimeInMS);
        else
            runTime = processTimeInMS;

        target.setTargetTaskBegin(Instant.now());
        //communicator.printTaskIsStarting(this,target);
        //fileSaver.printTaskIsStarting(target.getName(),getTaskName());

        try { Thread.sleep(runTime);
        } catch (InterruptedException ignored) { }
        target.setTargetTaskEnd(Instant.now());

        //communicator.printTaskIsEnding(this,target ,runTime*0.001f);
        //fileSaver.printTaskIsEnding(target.getName(),getTaskName() ,runTime*0.001f);

        if (randSuccess > successChance){
            target.setResult(Target.Result.FAILURE);
        }
        else if (randWarning < warningChance)
            target.setResult(Target.Result.WARNING);
        else
            target.setResult(Target.Result.SUCCESS);

        target.setTargetTaskTime(Duration.between(target.getTargetTaskBegin(),
                target.getTargetTaskEnd()));
    }
}
