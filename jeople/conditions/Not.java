package jeople.conditions;

import jeople.Condition;
import jeople.Entity;

/**
 * Implements the "NOT" operations for conditions.
 * 
 * @author Reda El Khattabi
 */
public class Not<T extends Entity> implements Condition<T> {

	private Condition<T> condition;

	public Not(Condition<T> condition) {
		this.condition = condition;
	}

	@Override
	public boolean evaluate(T element) {
		return !this.condition.evaluate(element);
	}

}
