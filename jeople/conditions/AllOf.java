package jeople.conditions;

import jeople.Condition;
import jeople.Entity;

/**
 * Implements the "AND" operations for conditions.
 * 
 * @author Reda El Khattabi
 */
public class AllOf<T extends Entity> implements Condition<T> {

	private Condition<T>[] conditions;

	public AllOf(Condition<T>... conditions) {
		this.conditions = conditions;
	}

	@Override
	public boolean evaluate(T element) {
		for (Condition<T> c : this.conditions)
			if (!c.evaluate(element))
				return false;
		return true;
	}

}
