package net.angelspeech.database;

import org.apache.log4j.Logger;

/**
 * This class is a wrapper around the SqlAccess class which allows calling its functions statically.
 */
public class SqlQuery
{
	//static private SqlAccess access = new SqlAccess ("jdbc/mysql-angelspeech");

	private static Logger logger = Logger.getLogger(SqlQuery.class);

	private static SqlAccess readOnlyAccess = new SqlAccess("jdbc/angelspeech-readOnly");
	private static SqlAccess defaultAccess = new SqlAccess ("jdbc/mysql-angelspeech");

	private static ThreadLocal<SqlAccess> access = new ThreadLocal<SqlAccess>(){

		protected SqlAccess initialValue() {
			return defaultAccess;
		}
	};

	/**
	 * Perform an SQL query and return a two-dimentional string array with
	 * query resulting rows. If the query did not return any rows, then the
	 * result is an empty array
	 *
	 * @return		Return an array of query results.
	 * @param query		The query that is sent to the SQL server
	 */
	static public String [][] query (String query) throws Exception
	{
		return (access.get().query (query));
	}

	/**
	 * Returns the SQL update count for the last issued SQL query.
	 *
	 * @return		Return the update count for the last query.
	 */
	static public int getUpdateCount ()
	{
		return (access.get().getUpdateCount ());
	}

	public static void setReadOnlyConnection(boolean readOnly)
	{
			if(readOnly) {				
				access.set(readOnlyAccess);
			} else {				
				access.set(defaultAccess);
			}
			logger.info("Switching to connection: " + access.get().getDsName());
	}

	/**
	 * SQL escapes a given text string and returns the result.
	 *
	 * @return		Return a string after SQL escaping it.
	 * @param text		The string that is escaped
	 */
	static public String escape (String text)
	{
		return (SqlAccess.escape (text));
	}

	/**
	 * Prepares a given text string to be used in an SQL LIKE expression.
	 *
	 * @return		Return a string after SQL escaping it.
	 * @param text		The query that is changed
	 */
	static public String like (String text)
	{
		return (SqlAccess.like (text));
	}
}
