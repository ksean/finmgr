package sh.kss.finmgr.lib.entity;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.util.Map;

/**
 * A portfolio has some holdings represented by a collection of key values, where values can be numeric or financial
 *
 */
@Value
@With
@Builder(toBuilder = true)
public class Portfolio {

    public static final Portfolio EMPTY_NON_REGISTERED = Portfolio.builder()
        .holdings(Map.of(
            AccountType.NON_REGISTERED,
            Holding.EMPTY))
        .build();

    Map<AccountType, Holding> holdings;
}
