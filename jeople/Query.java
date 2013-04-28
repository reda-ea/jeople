package jeople;

import java.util.Comparator;

/**
 * An unordered query result.
 * 
 * @author Reda El Khattabi
 * 
 * @param <T>
 *            the query entity type
 */
public interface Query<T extends Entity> extends Iterable<T> {

	Query<T> where(Condition<T> condition);

	OrderedQuery<T> orderBy(Comparator<T> comparator);

}