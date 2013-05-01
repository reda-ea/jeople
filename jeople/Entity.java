package jeople;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jeople.errors.InternalError;

/**
 * Represents an entity. Every entity has to subclass this.<br>
 * public fields in subclasses will be used as entity attributes.<br>
 * 
 * @author Reda El Khattabi
 */
public class Entity {
	private DataSource datasource;

	@SuppressWarnings("unused")
	private Map<String, ?> key; // only accessed through reflection

	// ////////////////////////////// ENTITY INFO
	@SuppressWarnings("unused")
	private String tablename;
	private List<String> columns;
	private Map<String, Class<?>> types;

	private static String get_tablename(Class<? extends Entity> type) {
		String[] ss = type.getName().split("[\\.\\$]");
		return ss[ss.length - 1];
	}

	private List<String> get_columns() {
		List<String> columns = new ArrayList<String>();
		for (Field f : this.getClass().getFields())
			columns.add(f.getName());
		return columns;
	}

	private Class<?> get_column_type(String column) {
		try {
			return this.getClass().getField(column).getType();
		} catch (NoSuchFieldException e) {
			throw new InternalError(e);
		} catch (SecurityException e) {
			throw new InternalError(e);
		}
	}

	@SuppressWarnings("unused")
	private Object get_column_value(String column) {
		try {
			return this.getClass().getField(column).get(this);
		} catch (IllegalArgumentException e) {
			throw new InternalError(e);
		} catch (IllegalAccessException e) {
			throw new InternalError(e);
		} catch (NoSuchFieldException e) {
			throw new InternalError(e);
		} catch (SecurityException e) {
			throw new InternalError(e);
		}
	}

	@SuppressWarnings("unused")
	private void set_column_value(String column, Object value) {
		try {
			this.getClass().getField(column).set(this, value);
		} catch (IllegalArgumentException e) {
			throw new InternalError(e);
		} catch (IllegalAccessException e) {
			throw new InternalError(e);
		} catch (NoSuchFieldException e) {
			throw new InternalError(e);
		} catch (SecurityException e) {
			throw new InternalError(e);
		}
	}

	// //////////////////////////////////////////

	protected Entity() {
		this.tablename = Entity.get_tablename(this.getClass());
		this.columns = this.get_columns();
		this.types = new TreeMap<String, Class<?>>(
				String.CASE_INSENSITIVE_ORDER);
		for (String s : this.columns)
			this.types.put(s, this.get_column_type(s));
	}

	/**
	 * First element in query or null, useful when querying one element.<br>
	 * This method will loop through the entire query (to guarantee iterator
	 * completion / connection closing / etc), but only return the first
	 * element.
	 */
	public static <T extends Entity> T firstIn(Query<T> query) {
		T ret = null;
		for (T t : query)
			if (ret == null)
				ret = t;
		return ret;
	}

	public void save() {
		this.datasource.save(this);
	}

	public void delete() {
		this.datasource.delete(this);
	}

	@Override
	public String toString() {
		String s = "{";
		String sep = "";
		for (Field f : this.getClass().getFields())
			try {
				s += sep + f.getName() + "=" + f.get(this).toString();
				sep = ", ";
			} catch (IllegalArgumentException e) {
				throw new InternalError(e);
			} catch (IllegalAccessException e) {
				throw new InternalError(e);
			}
		s += "}";
		return s;
	}
}
