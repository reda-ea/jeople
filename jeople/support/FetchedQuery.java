package jeople.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import jeople.Condition;
import jeople.Entity;
import jeople.OrderedQuery;
import jeople.Query;

/**
 * Internal {@link OrderedQuery} implementation. Doesn't depend on any specific
 * implementation details.<br>
 * This is actually just a simple wrapper around a {@link List}, and thus can safely
 * reused elsewhere.
 * 
 * @author Reda El Khattabi
 * 
 * @param <T>
 */
public class FetchedQuery<T extends Entity> implements OrderedQuery<T> {

	private List<T> data;

	public FetchedQuery(Iterable<T> data) {
		this.data = new ArrayList<T>();
		if (data != null)
			for (T t : data)
				this.data.add(t);
	}

	public void sort(Comparator<T> comparator) {
		Collections.sort(this.data, comparator);
	}

	public void reverse() {
		Collections.reverse(this.data);
	}

	@Override
	public Query<T> where(Condition<T> condition) {
		FetchedQuery<T> q = new FetchedQuery<T>(null);
		for (T t : this)
			if (condition.evaluate(t))
				q.data.add(t);
		return q;
	}

	@Override
	public OrderedQuery<T> orderBy(Comparator<T> comparator) {
		FetchedQuery<T> q = new FetchedQuery<T>(this);
		q.sort(comparator);
		return q;
	}

	@Override
	public Iterator<T> iterator() {
		return this.data.iterator();
	}

	@Override
	public OrderedQuery<T> desc() {
		FetchedQuery<T> q = new FetchedQuery<T>(this);
		q.reverse();
		return q;
	}

	@Override
	public String toString() {
		return Utils.toString(this);
	}

}
