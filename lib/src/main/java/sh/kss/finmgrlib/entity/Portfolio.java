package sh.kss.finmgrlib.entity;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import javax.money.MonetaryAmount;
import java.util.Map;

/**
 *
 *
 */
@Value
@With
@Builder(toBuilder = true)
public class Portfolio {

    Map<String, Quantity> quantities;
    Map<String, MonetaryAmount> monies;
}
