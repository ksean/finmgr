package sh.kss.finmgrlib.entity;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import javax.money.MonetaryAmount;
import java.util.Map;

/**
 * A portfolio has some holdings represented by a collection of key values, where values can be numeric or financial
 *
 */
@Value
@With
@Builder(toBuilder = true)
public class Portfolio {

    Map<String, Quantity> quantities;
    Map<String, MonetaryAmount> monies;
}
