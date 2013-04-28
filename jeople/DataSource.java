package jeople;

/**
 * CRUD acces to a data source, usually a database.
 * 
 * @author Reda El Khattabi
 */
public interface DataSource {
	public <T extends Entity> Query<T> select(Class<T> type);

	public <T extends Entity> T create(Class<T> type);

	public <T extends Entity> void save(T entity);

	public <T extends Entity> void delete(T entity);
}
