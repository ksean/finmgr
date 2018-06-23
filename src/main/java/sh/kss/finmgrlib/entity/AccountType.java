package sh.kss.finmgrlib.entity;

import lombok.Value;

public enum AccountType {
    TFSA,
    RESP,
    RRSP,
    NON_REGISTERED,
    CORPORATE_INVESTMENT,
    CORPORATE_CASH,
    PERSONAL
}