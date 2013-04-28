package jeople;

/**
 * An ordered query result.<br>
 * Allows the {@link #desc()} statement.
 * 
 * @author Reda El Khattabi
 * 
 * @param <T>
 *            the query entity type
 */
public interface OrderedQuery<T extends Entity> extends Query<T> {

	OrderedQuery<T> desc();
}
