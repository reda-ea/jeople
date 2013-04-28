package jeople;

/**
 * Client side condition for {@link Query#where(Condition)} statements.
 * 
 * @author Reda El Khattabi
 * 
 * @param <T>
 *            the entity type.
 */
public interface Condition<T extends Entity> {
	/**
	 * Evaluate the condition for the given element.
	 */
	boolean evaluate(T element);
}
