package codes.nibby.yi.io;

public class GameParseException extends Exception {

    public GameParseException() {
        super();
    }

    public GameParseException(String message) {
        super(message);
    }

    public GameParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public GameParseException(Throwable cause) {
        super(cause);
    }

    protected GameParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
