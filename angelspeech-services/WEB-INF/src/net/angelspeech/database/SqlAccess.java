package net.angelspeech.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

/**
 * This class contains generic low-level SQL helper functions.
 */
public class SqlAccess
{
	static private Logger logger = Logger.getLogger (SqlAccess.class);

	private String dsName;
	//private int updateCount;
	private ThreadLocal<Integer> updateCount = new ThreadLocal<Integer>() {
		@Override
		protected Integer initialValue() {
			return -1;
		}
	};
	private DataSource dataSource;

	/**
	 * Creates an SqlAccess object.
	 *
	 * @param dsName	The name of the datasource
	 */
	public SqlAccess (String dsName)
	{
		this.dsName = dsName;		
		try {
			InitialContext contextInitial = new InitialContext ();
			Context contextEnv = (Context) contextInitial.lookup ("java:comp/env");
			dataSource = (DataSource) contextEnv.lookup (dsName);
		} catch (NamingException ex) {
			logger.error ("Naming exception \"" + ex.getMessage () + "\" when initializing SQL connection");			
		}
	}

	/**
	 * Perform an SQL query and return a two-dimentional string array with
	 * query resulting rows. If the query did not return any rows, then the
	 * result is an empty array
	 *
	 * @return		Return an array of query results.
	 * @param query		The query that is sent to the SQL server
	 */
	public String [][] query (String query) throws Exception
	{		 
		 
		Connection connection;
		Statement statement;
		ResultSet rs;
		Vector rows;
		int columnCount;

		logger.debug ("Executing SQL query \"" + query + "\"");
		updateCount.set(-1);		
		rows = new Vector ();
		connection = null;
		statement = null;
		rs = null;
		try {
			connection = dataSource.getConnection ();
			statement = connection.createStatement ();
			if (statement.execute (query)) {
				rs = statement.getResultSet ();
				columnCount = rs.getMetaData ().getColumnCount ();
				while (rs.next ()) {
					rows.add (getRowData (rs, columnCount));
				}
			} else {
				updateCount.set(statement.getUpdateCount());
			}
		} catch (SQLException ex) {
			logger.error ("SQL exception \"" + ex.getMessage () + "\" when executing SQL query \"" + query + "\"");
			throw ex;
		} finally {
			if (rs != null) {
				rs.close ();
			}
			if (statement != null) {
				statement.close ();
			}
			if (connection != null) {
				connection.close ();
			}
		}
		return ((String [][]) rows.toArray (new String [0][0]));
	}

	/**
	 * Returns the SQL update count for the last issued SQL query.
	 *
	 * @return		Return the update count for the last query.
	 */
	public int getUpdateCount ()
	{
		return updateCount.get();
	}

	/**
	 * SQL escapes a given text string and returns the result.
	 *
	 * @return		Return a string after SQL escaping it.
	 * @param text		The string that is escaped
	 */
	static public String escape (String text)
	{
		String result;
		int i, len;
		char c;

		result = "";
		for (i = 0, len = text.length (); i < len; ++i) {
			c = text.charAt (i);
			if ((c == '\\') || (c == '\'')) {
				result += "\\";
			}
			result += Character.toString (c);
		}
		return (result);
	}

	/**
	 * Prepares a given text string to be used in an SQL LIKE expression.
	 *
	 * @return		Return a string after SQL escaping it.
	 * @param text		The query that is changed
	 */
	static public String like (String text)
	{
		String result;
		int i, len;
		char c;

		if (text.startsWith ("*") == false) {
			text = "*" + text;
		}
		if (text.endsWith ("*") == false) {
			text = text + "*";
		}
		result = "";
		for (i = 0, len = text.length (); i < len; ++i) {
			c = text.charAt (i);
			switch (c) {
			case '\\':
			case '%':
			case '_':
				result += "\\" + Character.toString (c);
				break;
			case '*':
				result += "%";
				break;
			default:
				result += Character.toString (c);
				break;
			}
		}
		return (result);
	}

	static private String [] getRowData (ResultSet rs, int columnCount) throws SQLException
	{
		Vector data;
		int i;

		data = new Vector ();
		for (i = 1; i <= columnCount; ++i) {
			data.add (rs.getString (i));
		}
		return ((String []) data.toArray (new String [0]));
	}
	
	public String getDsName() {
		return dsName;
	}
}
