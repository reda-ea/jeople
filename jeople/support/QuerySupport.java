package jeople.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import jeople.Condition;
import jeople.DataSource;
import jeople.Entity;
import jeople.OrderedQuery;
import jeople.Query;
import jeople.errors.InternalError;

/**
 * Internal {@link Query} implementation. Depends on the
 * {@link DataSourceSupport} implementation of {@link DataSource}.
 * 
 * @author Reda El Khattabi
 * 
 * @param <T>
 *            the {@link Entity} type handled by this {@link Query}
 */
public class QuerySupport<T extends Entity> implements Query<T> {

	private Collection<Condition<T>> conditions;
	private DataSourceSupport datasource;
	private Class<T> type;

	public QuerySupport(DataSourceSupport datasource, Class<T> type) {
		this.type = type;
		this.datasource = datasource;
		this.conditions = new ArrayList<Condition<T>>();
	}

	private T create(Map<String, ?> data) {
		try {
			T t = this.type.newInstance();
			for (Map.Entry<String, ?> e : data.entrySet())
				Utils.runHiddenMethod(t, "set_column_value", e.getKey(),
						e.getValue());
			Utils.setHiddenField(t, "datasource", this.datasource);
			Utils.setHiddenField(t, "key", data);
			return t;
		} catch (InstantiationException e) {
			throw new InternalError(e);
		} catch (IllegalAccessException e) {
			throw new InternalError(e);
		}
	}

	private class QueryIterator implements Iterator<T> {

		private T last;
		private T next;
		private Object state;
		private boolean closed;

		public QueryIterator() {
			this.last = null;
			this.next = null;
			this.state = null;
			this.closed = false;
		}

		private T fetch() {
			if (this.closed)
				return null;
			if (this.state == null)
				this.state = QuerySupport.this.datasource.select((String) Utils
						.runHiddenMethod(null, "get_tablename",
								QuerySupport.this.type));
			Map<String, ?> m = QuerySupport.this.datasource.fetch(this.state);
			if (m == null) {
				this.closed = true;
				return null;
			}
			T t = QuerySupport.this.create(m);
			for (Condition<T> c : QuerySupport.this.conditions)
				if (!c.evaluate(t))
					return this.fetch();
			return t;
		}

		@Override
		public boolean hasNext() {
			if (this.closed)
				return false;
			if (this.next != null)
				return true;
			this.next = this.fetch();
			if (this.next == null) {
				this.closed = true;
				return false;
			} else
				return true;
		}

		@Override
		public T next() {
			if (this.next != null) {
				this.last = this.next;
				this.next = null;
			} else
				this.last = this.fetch();
			if (this.last == null)
				throw new NoSuchElementException();
			return this.last;
		}

		@Override
		public void remove() {
			if (this.last == null)
				throw new IllegalStateException();
			this.last.delete();
		}
	}

	@Override
	public Iterator<T> iterator() {
		return new QueryIterator();
	}

	@Override
	public Query<T> where(Condition<T> condition) {
		QuerySupport<T> q = new QuerySupport<T>(this.datasource, this.type);
		q.conditions.addAll(this.conditions);
		q.conditions.add(condition);
		return q;
	}

	@Override
	public OrderedQuery<T> orderBy(Comparator<T> comparator) {
		FetchedQuery<T> q = new FetchedQuery<T>(this);
		q.sort(comparator);
		return q;
	}

	@Override
	public String toString() {
		return Utils.toString(this);
	}

}
