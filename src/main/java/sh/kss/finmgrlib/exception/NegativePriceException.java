package sh.kss.finmgrlib.exception;

public class NegativePriceException extends Exception {

    public NegativePriceException(String message) {
        super(message);
    }
}
