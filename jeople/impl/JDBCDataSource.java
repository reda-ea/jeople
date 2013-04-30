package jeople.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jeople.DataSource;
import jeople.Query;
import jeople.support.DataSourceSupport;
import jeople.errors.InternalError;

/**
 * Basic JDBC {@link DataSource} implementation.<br>
 * For {@link #save(jeople.Entity)} and {@link #delete(jeople.Entity)}
 * operations, connections are closed immediately.<br>
 * For {@link #select(Class)}, the default behavior is to open a connection on
 * each {@link Query} iteration (the first call to {@link Iterator#next()} or
 * {@link Iterator#hasNext()}) and close it when the JDBC {@link ResultSet} has
 * no more elements.<br>
 * An alternative method (available to subclasses through the protected
 * {@link #JDBCDataSource(String, String, String, String, String)} constructor)
 * is to specify a "ROWID" column, that will be completely fetched on iteration
 * start, allowing the connection to be immediately closed. Each subsequent
 * iteration will retrieve the record data using the "ROWID" column.<br>
 * Subclasses can also override the
 * {@link #getColumnValue(ResultSet, int, String)} and
 * {@link #setColumnValue(PreparedStatement, int, String, Object)} methods to
 * customize how data is retrieved from the columns depending on their type.
 * 
 * @author Reda El Khattabi
 */
public class JDBCDataSource extends DataSourceSupport {

	private String url;
	private String user;
	private String password;
	private String rowid;

	public JDBCDataSource(String driver, String url, String user,
			String password) {
		this(driver, url, user, password, null);
	}

	/**
	 * Allows a select query to free the JDBC connection between fetches (by
	 * indexing the rowid column on initial select)
	 * 
	 * @param rowIdColumn
	 */
	protected JDBCDataSource(String driver, String url, String user,
			String password, String rowIdColumn) {
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			throw new InternalError("Driver not found: " + "org.sqlite.JDBC", e);
		}
		this.url = url;
		this.user = user;
		this.password = password;
		this.rowid = rowIdColumn;
	}

	private static class ConnectionStatus {
		public Connection connection;
		public PreparedStatement statement;
		public ResultSet resultSet;

		public ConnectionStatus(String url, String user, String password,
				String query) {
			try {
				this.connection = DriverManager.getConnection(url, user,
						password);
				this.statement = this.connection.prepareStatement(query);
				if (query.contains("?")) {
					this.resultSet = null;
					return;
				}
				if (query.toLowerCase().startsWith("select"))
					this.resultSet = this.statement.executeQuery();
				else {
					this.statement.executeUpdate();
					this.resultSet = null;
				}
			} catch (SQLException e) {
				throw new InternalError(e);
			}
		}

		public void close() {
			try {
				if (this.resultSet != null) {
					this.resultSet.close();

					this.resultSet = null;
				}
				if (this.statement != null) {
					this.statement.close();
					this.statement = null;
				}
				if (this.connection != null) {
					// this.connection.close();
					this.connection = null;
				}
			} catch (SQLException e) {
				throw new InternalError(e);
			}
		}
	}

	private static class TableFetchStatus {
		private String table;
		public List<Object> index;
		public int position;

		public TableFetchStatus(String table) {
			this.table = table;
			this.index = new ArrayList<Object>();
			this.position = 0;
		}
	}

	@Override
	public Object select(String table) {
		if (this.rowid == null || this.rowid.isEmpty())
			return new ConnectionStatus(this.url, this.user, this.password,
					"select * from " + table + ";");
		else {
			TableFetchStatus tfs = new TableFetchStatus(table);
			ConnectionStatus cs = new ConnectionStatus(this.url, this.user,
					this.password, "select " + this.rowid + " from " + table
							+ ";");
			try {
				while (cs.resultSet.next())
					tfs.index.add(cs.resultSet.getObject(1));
			} catch (SQLException e) {
				throw new InternalError(e);
			}
			cs.close();
			return tfs;
		}
	}

	// TODO use a proxy object instead of the actual ResultSet
	protected Object getColumnValue(ResultSet resultSet, int index,
			String typeName) {
		try {
			return resultSet.getObject(index);
		} catch (SQLException e) {
			throw new InternalError(e);
		}
	}

	// TODO use a proxy object instead of the actual PreparedStatement
	protected void setColumnValue(PreparedStatement statement, int index,
			String typeName, Object value) {
		try {
			statement.setObject(index, value);
		} catch (SQLException e) {
			throw new InternalError(e);
		}
	}

	private static String getColumnTypeName(Connection connection,
			String table, String column) {
		try {
			ResultSet rs = connection.getMetaData().getColumns(null, null,
					table, column);
			if (!rs.next())
				throw new InternalError("Table or column not found for " + table + "."
						+ column);
			String ret = rs.getString("TYPE_NAME");
			rs.close();
			return ret;
		} catch (SQLException e) {
			throw new InternalError(e);
		}
	}

	/**
	 * get data from an already positioned ResultSet
	 */
	private Map<String, ?> fetchData(ResultSet resultSet) {
		try {
			Map<String, Object> m = new TreeMap<String, Object>(
					String.CASE_INSENSITIVE_ORDER);
			ResultSetMetaData md = resultSet.getMetaData();
			for (int i = 0; i < md.getColumnCount(); ++i)
				m.put(md.getColumnName(i + 1),
						this.getColumnValue(resultSet, i + 1,
								md.getColumnTypeName(i + 1)));
			return m;
		} catch (SQLException e) {
			throw new InternalError(e);
		}
	}

	private Map<String, ?> fetchTFS(TableFetchStatus tfs) {
		if (tfs.position >= tfs.index.size())
			return null;
		ConnectionStatus cs = new ConnectionStatus(this.url, this.user,
				this.password, "select * from " + tfs.table + " where "
						+ this.rowid + " = ?;");
		try {
			cs.statement.setObject(1, tfs.index.get(tfs.position));
			cs.resultSet = cs.statement.executeQuery();
			if (!cs.resultSet.next())
				throw new InternalError("Record not found for " + tfs.table + "."
						+ this.rowid + " = " + tfs.index.get(tfs.position));
			Map<String, ?> m = this.fetchData(cs.resultSet);
			if (cs.resultSet.next())
				throw new InternalError("Multiple record found for " + tfs.table + "."
						+ this.rowid + " = " + tfs.index.get(tfs.position));
			cs.close();
			++tfs.position;
			return m;
		} catch (SQLException e) {
			throw new InternalError(e);
		}
	}

	private Map<String, ?> fetchCS(ConnectionStatus cs) {
		if (cs.resultSet == null)
			return null;
		try {
			if (!cs.resultSet.next()) {
				cs.close();
				return null;
			}
			return this.fetchData(cs.resultSet);
		} catch (SQLException e) {
			throw new InternalError(e);
		}
	}

	@Override
	public Map<String, ?> fetch(Object status) {
		if (this.rowid == null || this.rowid.isEmpty())
			return this.fetchCS((ConnectionStatus) status);
		else
			return this.fetchTFS((TableFetchStatus) status);
	}

	@Override
	public void insert(String table, Map<String, ?> data) {
		List<String> columns = new ArrayList<String>(data.keySet());
		String query = "insert into " + table + "(";
		String sep = "";
		for (String s : columns) {
			query += sep + s;
			sep = ", ";
		}
		query += ") values(";
		sep = "";
		for (int i = 0; i < columns.size(); ++i) {
			query += sep + "?";
			sep = ", ";
		}
		query += ");";
		ConnectionStatus cs = new ConnectionStatus(this.url, this.user,
				this.password, query);
		for (int i = 0; i < columns.size(); ++i)
			this.setColumnValue(cs.statement, i + 1, JDBCDataSource
					.getColumnTypeName(cs.connection, table, columns.get(i)),
					data.get(columns.get(i)));
		try {
			if (cs.statement.executeUpdate() != 1)
				throw new InternalError("Affected more than one record");
		} catch (SQLException e) {
			throw new InternalError(e);
		}
		cs.close();
	}

	@Override
	public void update(String table, Map<String, ?> key, Map<String, ?> data) {
		List<String> datacols = new ArrayList<String>(data.keySet());
		List<String> keycols = new ArrayList<String>(key.keySet());
		String query = "update " + table + " set ";
		String sep = "";
		for (String s : datacols) {
			query += sep + s + " = ?";
			sep = ", ";
		}
		query += " where ";
		sep = "";
		for (String s : keycols) {
			query += sep + s + " = ?";
			sep = " and ";
		}
		query += ";";
		ConnectionStatus cs = new ConnectionStatus(this.url, this.user,
				this.password, query);
		for (int i = 0; i < datacols.size(); ++i)
			this.setColumnValue(cs.statement, i + 1, JDBCDataSource
					.getColumnTypeName(cs.connection, table, datacols.get(i)),
					data.get(datacols.get(i)));
		for (int i = 0; i < keycols.size(); ++i)
			this.setColumnValue(cs.statement, datacols.size() + i + 1,
					JDBCDataSource.getColumnTypeName(cs.connection, table,
							keycols.get(i)), key.get(keycols.get(i)));
		try {
			if (cs.statement.executeUpdate() != 1)
				throw new InternalError("Affected more than one record");
		} catch (SQLException e) {
			throw new InternalError(e);
		}
		cs.close();
	}

	@Override
	public void delete(String table, Map<String, ?> key) {
		List<String> columns = new ArrayList<String>(key.keySet());
		String query = "delete from " + table + " where ";
		String sep = "";
		for (String s : columns) {
			query += sep + s + " = ?";
			sep = " and ";
		}
		query += ";";
		ConnectionStatus cs = new ConnectionStatus(this.url, this.user,
				this.password, query);
		for (int i = 0; i < columns.size(); ++i)
			this.setColumnValue(cs.statement, i + 1, JDBCDataSource
					.getColumnTypeName(cs.connection, table, columns.get(i)),
					key.get(columns.get(i)));
		try {
			if (cs.statement.executeUpdate() != 1)
				throw new InternalError("Affected more than one record");
		} catch (SQLException e) {
			throw new InternalError(e);
		}
		cs.close();
	}

}
