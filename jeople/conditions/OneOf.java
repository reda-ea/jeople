package jeople.conditions;

import jeople.Condition;
import jeople.Entity;

/**
 * Implements the "OR" operations for conditions.
 * 
 * @author Reda El Khattabi
 */
public class OneOf<T extends Entity> implements Condition<T> {

	private Condition<T>[] conditions;

	public OneOf(Condition<T>... conditions) {
		this.conditions = conditions;
	}

	@Override
	public boolean evaluate(T element) {
		for (Condition<T> c : this.conditions)
			if (c.evaluate(element))
				return true;
		return false;
	}

}
