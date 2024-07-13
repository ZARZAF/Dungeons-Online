package logger;

public enum LogType {

    ERROR( "logs/error.log"),
    GAME("logs/game.log");
    private final String value;

    LogType(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }

}
