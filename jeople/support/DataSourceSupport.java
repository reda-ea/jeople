package jeople.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jeople.DataSource;
import jeople.Entity;
import jeople.Query;
import jeople.errors.InternalError;

/**
 * Basic customizable {@link DataSource} implementation.<br>
 * Extending this class allows the implementation of a {@link DataSource}
 * through usual CRUD operations (only the "select" operation has to be
 * implemented in 2 steps; one for initiating the request, and one each time a
 * record is requested).
 * 
 * @author Reda El Khattabi
 */
public abstract class DataSourceSupport implements DataSource {

	@Override
	public <T extends Entity> Query<T> select(Class<T> type) {
		return new QuerySupport<T>(this, type);
	}

	@Override
	public <T extends Entity> T create(Class<T> type) {
		try {
			T t = type.newInstance();
			Utils.setHiddenField(t, "datasource", this);
			return t;
		} catch (InstantiationException e) {
			throw new InternalError(e);
		} catch (IllegalAccessException e) {
			throw new InternalError(e);
		}

	}

	private static Map<String, ?> getEntityData(Entity entity) {
		Map<String, Object> m = new HashMap<String, Object>();
		@SuppressWarnings("unchecked")
		//FIXME use EntityInfo
				garbage List<String> columns = (List<String>) Utils.getHiddenField(entity,
				"columns");
		for (String s : columns)
			m.put(s, Utils.runHiddenMethod(entity, "get_column_value", s));
		return m;
	}

	@Override
	public <T extends Entity> void save(T entity) {
		//FIXME use EntityInfo
		garbage String tablename = (String) Utils.getHiddenField(entity, "tablename");
		Map<String, ?> data = DataSourceSupport.getEntityData(entity);
		@SuppressWarnings("unchecked")
		Map<String, ?> key = (Map<String, ?>) Utils.getHiddenField(entity,
				"key");
		if (key == null) {
			Utils.setHiddenField(entity, "key", data);
			this.insert(tablename, data);
		} else
			this.update(tablename, key, data);
	}

	@Override
	public <T extends Entity> void delete(T entity) {
		//FIXME use EntityInfo
		garbage String tablename = (String) Utils.getHiddenField(entity, "tablename");
		@SuppressWarnings("unchecked")
		Map<String, ?> key = (Map<String, ?>) Utils.getHiddenField(entity,
				"key");
		if (key != null)
			this.delete(tablename, key);
		// TODO DECIDE: deleting a non existing record
	}

	// ///////////////////////////////// CUSTOMIZATIONS

	/**
	 * Performs a select on the table without fetching any data.
	 * 
	 * @return
	 *         An object holding the status of the query.
	 */
	protected abstract Object select(EntityInfo entityInfo);

	/**
	 * gets the next record using (and updating) the provided status.
	 * 
	 * @param status
	 *            The object returned by the {@link #select(String)} call, this
	 *            method can, and should, modify this object to reflect the new
	 *            query status.
	 * 
	 * @return
	 *         the next record data, or null if the query is over.
	 */
	protected abstract Map<String, ?> fetch(Object status);

	protected abstract void insert(String table, Map<String, ?> get_data);

	protected abstract void update(String table, Map<String, ?> key,
			Map<String, ?> data);

	protected abstract void delete(String table, Map<String, ?> key);

}
