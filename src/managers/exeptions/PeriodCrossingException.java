package managers.exeptions;

public class PeriodCrossingException extends RuntimeException {
    public PeriodCrossingException() {
        super("Периоды задач пересекаются");
    }
}
