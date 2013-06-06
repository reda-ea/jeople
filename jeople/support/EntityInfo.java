package jeople.support;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jeople.Entity;
import jeople.errors.InternalError;

public class EntityInfo {

	private String get_tablename() {
		String[] ss = this.type.getName().split("[\\.\\$]");
		return ss[ss.length - 1];
	}

	private List<String> get_columns() {
		List<String> columns = new ArrayList<String>();
		for (Field f : this.type.getFields())
			columns.add(f.getName());
		return columns;
	}

	private Class<?> get_column_type(String column) {
		try {
			return this.type.getField(column).getType();
		} catch (NoSuchFieldException e) {
			throw new InternalError(e);
		} catch (SecurityException e) {
			throw new InternalError(e);
		}
	}

	public EntityInfo(Class<? extends Entity> type) {
		this.type = type;
		this.name = this.get_tablename();
		this.columns = this.get_columns();
		this.columnTypes = new TreeMap<String, Class<?>>(
				String.CASE_INSENSITIVE_ORDER);
		for (String s : this.columns)
			this.columnTypes.put(s, this.get_column_type(s));
	}

	public Class<?> type;
	public String name;
	public List<String> columns;
	public Map<String, Class<?>> columnTypes;
}