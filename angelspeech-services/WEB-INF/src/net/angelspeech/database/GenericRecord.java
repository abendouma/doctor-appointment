package net.angelspeech.database;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Vector;

/**
 * This abstract class is a generic database object representing one database row.
 * Some of the methods accept a filter which determines on which table rows will operate the method.
 *
 * The filter is a two-dimentional string array which has following contents:
 * {
 *	{field_name_1, value_1},
 *	{field_name_2, value_2},
 *	...,
 *	{field_name_N, value_N}
 * }
 *
 * The filter is transformed into the following SQL clause:
 * WHERE
 *	field_name_1='value_1' AND
 *	field_name_2='value_2' AND
 *	...
 *	field_name_N='value_N'
 *
 * The methods using filters operate only on table rows which match the above SQL clause.
 *
 * Each table in DB may have an object which is a child class of this GenericRecord
 * Each variable in child class is associated with a field in the DB table
 * All of the fields must be defined as String in Child record class regardless their type in DB.
 * So data type needs to be converted to String before they are saved in child record.
 * This restriction is due to a API requirement by DB driver.
 *
 */
public abstract class GenericRecord implements Cloneable, Serializable
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7683138654591895250L;
	private String tableName;
	private String idName;
	private transient Field [] childFields;

	/**
	 * Creates an object instance which maps this instance to a specific database table
	 *
	 * @param tableName	The name of the database table
	 * @param idName	The name of table field which is the primary key
	 * @param childClass	The name of the subclass which extends this abstract class
	 */
	protected GenericRecord (String tableName, String idName, Class childClass) throws Exception
	{
		this.tableName = tableName;
		this.idName = idName;
		this.childFields = childClass.getFields ();
		nullifyFieldValues ();
	}

	/**
	 * Find differing fields between two objects of the same type.
	 *
	 * @return		Return an array of differing field names.
	 * @param firstObj	First object.
	 * @param secondObj	Second object.
	 */
	static public String [] diff (GenericRecord firstObj, GenericRecord secondObj) throws Exception
	{
		String firstClass, secondClass;
		Field [] fields;
		Vector diffNames = new Vector ();
		int i;
		Object valueFirst, valueSecond;

		firstClass = firstObj.getClass ().getName ();
		secondClass = secondObj.getClass ().getName ();
		if (firstClass.equals (secondClass) == false) {
			throw new Exception ("Compared objects have different types");
		}
		fields = firstObj.getClass().getFields ();
		for (i = 0; i < fields.length; ++i) {
			valueFirst = fields [i].get (firstObj);
			valueSecond = fields [i].get (secondObj);
			if ((valueFirst != null) && (valueSecond != null) && (valueFirst.equals (valueSecond) == false)) {
				diffNames.add (fields [i].getName ());
			}
		}
		return ((String []) diffNames.toArray (new String [0]));
	}

	/**
	 * Insert one row in the corresponding database table
	 *
	 * @return	Return a string representing MySQL last insert ID
	 */
	public String create () throws Exception
	{
		String [][] rows;
		String result;
		
		System.out.println("INSERT " +
			"INTO " + SqlQuery.escape (tableName) + " " +
			buildNameValueList ());
		
		SqlQuery.query (
			"INSERT " +
			"INTO " + SqlQuery.escape (tableName) + " " +
			buildNameValueList ()
		);
		if (idName != null) {
			rows = SqlQuery.query ("SELECT LAST_INSERT_ID()");
			result = rows [0][0];
		} else {
			result = null;
		}
		return (result);
	}

	/**
	 * Delete one row from the corresponding database table. This function
	 * deletes the row which has a corresponding primary key value
	 *
	 * @param idValue	Value of the primary key
	 */
	public void destroyById (String idValue) throws Exception
	{
		destroy (new String [][] {{idName, idValue}});
	}

	/**
	 * Delete all rows that match the filter.
	 *
	 * @param filter	The filter which specifies which rows should be deleted
	 */
	public void destroyByFilter (String [][] filter) throws Exception
	{
		destroy (filter);
	}

	private void destroy (String [][] filter) throws Exception
	{
		String filterString;

		filterString = buildFilterList (filter);
		SqlQuery.query (
			"DELETE " +
			"FROM " + SqlQuery.escape (tableName) +
			(filterString.equals ("") ? "" : " WHERE " + filterString)
		);
	}

	/**
	 * Reads one row from the corresponding database table. This function
	 * reads the row which has a corresponding primary key value
	 *
	 * @return		Return a boolean indicating whether the table
	 *			row has been successfully read.
	 * @param idValue	Value of the primary key
	 */
	public boolean readById (String idValue) throws Exception
	{
		return (read (new String [][] {{idName, idValue}}));
	}

	/**
	 * Reads one row from the database table. This function
	 * reads the first returned row that matches the filter
	 *
	 * @return		Return a boolean indicating whether the table
	 *			row has been successfully read.
	 * @param filter	The filter which specifies which row should be read
	 */
	public boolean readByFilter (String [][] filter) throws Exception
	{
		return (read (filter));
	}

	private boolean read (String [][] filter) throws Exception
	{
		String filterString;
		String [][] rows;

		filterString = buildFilterList (filter);
		rows =  SqlQuery.query (
			"SELECT " + buildFieldList () + " " +
			"FROM " + SqlQuery.escape (tableName) + " " +
			(filterString.equals ("") ? "" : " WHERE " + filterString)
		);
		if (rows.length == 0) {
			return (false);
		}
		setFieldValues (rows [0]);
		return (true);
	}

	/**
	 * Updates one row in the corresponding database table. This function
	 * updates the row which has a corresponding primary key value
	 *
	 * @param idValue	Value of the primary key
	 */
	public void writeById (String idValue) throws Exception
	{
		write (new String [][] {{idName, idValue}});
	}

	/**
	 * Updates one row in the corresponding database table. This function
	 * updates the first found row that matches the filter
	 *
	 * @param filter	The filter which specifies which row should be updated
	 */
	public void writeByFilter (String [][] filter) throws Exception
	{
		write (filter);
	}

	private void write (String [][] filter) throws Exception
	{
		String filterString;

		filterString = buildFilterList (filter);
		SqlQuery.query (
			"UPDATE " + SqlQuery.escape (tableName) + " " +
			"SET " + buildAssignList () + " " +
			(filterString.equals ("") ? "" : " WHERE " + filterString)
		);
	}

	private String buildFieldList ()
	{
		String [] fieldNames;
		String fieldString;
		boolean found;
		int i;

		fieldNames = getFieldNames ();
		fieldString = "";
		found = false;
		for (i = 0; i < fieldNames.length; ++i) {
			if (found) {
				fieldString += ", ";
			}
			found = true;
			fieldString += SqlQuery.escape (fieldNames [i]);
		}
		return (fieldString);
	}

	private String buildNameValueList () throws Exception
	{
		String [][] fieldValues;
		String fieldString, valueString;
		boolean found;
		int i;

		fieldValues = getFieldValues ();
		fieldString = "";
		valueString = "";
		found = false;
		for (i = 0; i < fieldValues.length; ++i) {
			if (fieldValues [i][1] != null) {
				if (found) {
					fieldString += ", ";
					valueString += ", ";
				}
				found = true;
				fieldString += SqlQuery.escape (fieldValues [i][0]);
				valueString += "'" + SqlQuery.escape (fieldValues [i][1].trim()) + "'";
			}
		}
		return ("(" + fieldString + ") VALUES (" + valueString + ")");
	}

	private String buildAssignList () throws Exception
	{
		String [][] fieldValues;
		String fieldString;
		boolean found;
		int i;

		fieldValues = getFieldValues ();
		fieldString = "";
		found = false;
		for (i = 0; i < fieldValues.length; ++i) {
			if (fieldValues [i][1] != null) {
				if (found) {
					fieldString += ", ";
				}
				found = true;
				fieldString +=
					SqlQuery.escape (fieldValues [i][0]) +
					"=" +
					"'" + SqlQuery.escape (fieldValues [i][1]) + "'";
			}
		}
		return (fieldString);
	}

	static private String buildFilterList (String [][] fieldValues)
	{
		String fieldString;
		int i;

		fieldString = "";
		for (i = 0; i < fieldValues.length; ++i) {
			if (i > 0) {
				fieldString += " AND ";
			}
			fieldString +=
				SqlQuery.escape (fieldValues [i][0]) +
				"=" +
				"'" + SqlQuery.escape (fieldValues [i][1].trim()) + "'";
		}
		return (fieldString);
	}

	private String [] getFieldNames ()
	{
		String [] fieldNames;
		int i;

		fieldNames = new String [childFields.length];
		for (i = 0; i < fieldNames.length; ++i) {
			fieldNames [i] = childFields [i].getName ();
		}
		return (fieldNames);
	}

	private String [][] getFieldValues () throws Exception
	{
		String [][] fieldValues;
		int i;

		fieldValues = new String [childFields.length][2];
		for (i = 0; i < childFields.length; ++i) {
			fieldValues [i][0] = childFields [i].getName ();
			fieldValues [i][1] = (String) childFields [i].get (this);
		}
		return (fieldValues);
	}

	private void nullifyFieldValues () throws Exception
	{
		int i;

		for (i = 0; i < childFields.length; ++i) {
			childFields [i].set (this, null);
		}
	}

	private void setFieldValues (String [] fieldValues) throws Exception
	{
		int i;

		for (i = 0; i < childFields.length; ++i) {
			childFields [i].set (this, fieldValues [i]);
		}
	}

	public Object clone ()  throws CloneNotSupportedException
	{
		return (super.clone ());
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();		
		childFields = this.getClass().getFields();
	}
}
