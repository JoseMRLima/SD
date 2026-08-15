package exceptions;

public class MensagemErroException extends RuntimeException {
    public MensagemErroException(String message) {
        super(message);
    }
}
