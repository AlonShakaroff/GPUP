package exceptions;

import target.Target;

public class TargetAppearTwiceException extends Exception {
    private Target target;
    public TargetAppearTwiceException(Target target){
        this.target = target;
    }

    @Override
    public String getMessage(){
        return ("the target " + target.getName() + " appears more then once \n");
    }
}
