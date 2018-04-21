package sh.kss.finmgrlib.entity;

import lombok.Value;

import java.util.Set;

@Value
public class TransactionCategory {
    String name;
    Set<CategoryMatchCondition> matchConditionSet;
}
