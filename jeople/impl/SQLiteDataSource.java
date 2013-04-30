package jeople.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import jeople.DataSource;
import jeople.errors.InternalError;

/**
 * Specific SQLite {@link DataSource} implementation.<br>
 * This implementation uses the "ROWID" feature of SQLite to allow closing a
 * select connection as soon as possible, thus avoiding database lock problems.<br>
 * This implementation also allows date usage (although SQLite doesn't support a
 * date type) by storing dates as a number equivalent (depending on the typename
 * specified in the table).
 * 
 * @author Reda El Khattabi
 * 
 * @see http://www.sqlite.org/datatype3.html
 */
public class SQLiteDataSource extends JDBCDataSource {

	/**
	 * @param filepath
	 *            the file path/url (without jdbc:sqlite:.. )
	 */
	public SQLiteDataSource(String filepath) {
		super("org.sqlite.JDBC", "jdbc:sqlite:" + filepath, "", "", "ROWID");
	}

	/**
	 * SQLite doesn't support date or time data types. We need to use the
	 * provide
	 */
	@Override
	protected Object getColumnValue(ResultSet resultSet, int index,
			String typeName) {
		try {
			if (typeName.toLowerCase().contains("date"))
				return resultSet.getDate(index);
			else if (typeName.toLowerCase().contains("time"))
				return resultSet.getTime(index);
			else
				return super.getColumnValue(resultSet, index, typeName);
		} catch (SQLException e) {
			throw new InternalError(e);
		}
	}

}
