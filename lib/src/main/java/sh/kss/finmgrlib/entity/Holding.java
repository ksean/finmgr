package sh.kss.finmgrlib.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import javax.money.MonetaryAmount;
import java.util.Map;
import java.util.Set;

/**
 * A holding is a set of securities owned that should get grouped for some logical segregation
 *
 */
@Value
@With
@Builder(toBuilder = true)
@AllArgsConstructor
public class Holding {

    public static final Holding EMPTY = new Holding(Set.of(), Map.of(), Map.of());

    Set<Security> securities;
    Map<Security, Quantity> quantities;
    Map<Security, MonetaryAmount> costBasis;
}
