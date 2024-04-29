package managers.exeptions;

public class CommandNotFoundException extends RuntimeException {
    public CommandNotFoundException() {
        super("Команда не найдена");
    }
}
