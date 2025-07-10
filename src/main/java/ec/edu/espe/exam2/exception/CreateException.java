package ec.edu.espe.exam2.exception;

public class CreateException extends RuntimeException {
    private final Integer errorCode;
    private final String entityName;

    public CreateException(String entityName, String message) {
        super(message);
        this.errorCode = 2;
        this.entityName = entityName;
    }

    public CreateException(String message) {
        super(message);
        this.errorCode = 2;
        this.entityName = "Unknown";
    }

    @Override
    public String getMessage() {
        return "Error Code: " + errorCode + "; Entity Name: " + entityName + "; Message: " + super.getMessage();
    }
}
