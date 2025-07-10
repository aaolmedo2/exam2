package ec.edu.espe.exam2.exception;

public class EntityNotFoundException extends RuntimeException {
    private final Integer errorCode;
    private final String entityName;

    public EntityNotFoundException(String entityName, String message) {

        super(message);
        this.errorCode = 1;
        this.entityName = entityName;
    }

    @Override
    public String getMessage() {
        return "Error Code: " + errorCode + "; Entity Name: " + entityName + "; Message: " + super.getMessage();
    }
}
