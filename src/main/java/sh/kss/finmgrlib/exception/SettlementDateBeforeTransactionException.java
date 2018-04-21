package sh.kss.finmgrlib.exception;

public class SettlementDateBeforeTransactionException extends Exception {

    public SettlementDateBeforeTransactionException(String message) {
        super(message);
    }
}
