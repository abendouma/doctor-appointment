package net.angelspeech.object;

/**
 * This class is used to generate the javascript text representation of a java object
 */
public class BuildJavaScript
{
	/**
	 * Generate the javascript text representation of a generic java object
	 *
	 * @return	Return the javascript text representation of the java object
	 * @param item	The java object that should be converted to javascript text
	 */
	static public String build (Object item) throws Exception
	{
		String type;

		if (item == null) {
			return (buildObjectNull (item));
		} else {
			type = item.getClass ().getName ();
			if (type.startsWith ("[")) {
				return (buildObjectArray (item));
			} else if (type.equals ("java.lang.Boolean")) {
				return (buildObjectBoolean ((Boolean) item));
			} else if (type.equals ("java.lang.Byte")) {
				return (buildObjectByte ((Byte) item));
			} else if (type.equals ("java.lang.Double")) {
				return (buildObjectDouble ((Double) item));
			} else if (type.equals ("java.lang.Float")) {
				return (buildObjectFloat ((Float) item));
			} else if (type.equals ("java.lang.Integer")) {
				return (buildObjectInteger ((Integer) item));
			} else if (type.equals ("java.lang.Long")) {
				return (buildObjectLong ((Long) item));
			} else if (type.equals ("java.lang.Short")) {
				return (buildObjectShort ((Short) item));
			} else {
				return (buildObjectGeneric (item));
			}
		}
	}

	/**
	 * Generate the javascript text representation of a java "boolean" value
	 *
	 * @return	Return the javascript text representation of the java "boolean" value
	 * @param item	The java boolean value that should be converted to javascript text
	 */
	static public String build (boolean item)
	{
		return (String.valueOf (item));
	}

	/**
	 * Generate the javascript text representation of a java "byte" value
	 *
	 * @return	Return the javascript text representation of the java "byte" value
	 * @param item	The java "byte" value that should be converted to javascript text
	 */
	static public String build (byte item)
	{
		return (String.valueOf (item));
	}

	/**
	 * Generate the javascript text representation of a java "char" value
	 *
	 * @return	Return the javascript text representation of the java "char" value
	 * @param item	The java "char" value that should be converted to javascript text
	 */
	static public String build (char item)
	{
		return (String.valueOf (item));
	}

	/**
	 * Generate the javascript text representation of a java "double" value
	 *
	 * @return	Return the javascript text representation of the java "double" value
	 * @param item	The java "double" value that should be converted to javascript text
	 */
	static public String build (double item)
	{
		return (String.valueOf (item));
	}

	/**
	 * Generate the javascript text representation of a java "float" value
	 *
	 * @return	Return the javascript text representation of the java "float" value
	 * @param item	The java "float" value that should be converted to javascript text
	 */
	static public String build (float item)
	{
		return (String.valueOf (item));
	}

	/**
	 * Generate the javascript text representation of a java "int" value
	 *
	 * @return	Return the javascript text representation of the java "int" value
	 * @param item	The java "int" value that should be converted to javascript text
	 */
	static public String build (int item)
	{
		return (String.valueOf (item));
	}

	/**
	 * Generate the javascript text representation of a java "long" value
	 *
	 * @return	Return the javascript text representation of the java "long" value
	 * @param item	The java "long" value that should be converted to javascript text
	 */
	static public String build (long item)
	{
		return (String.valueOf (item));
	}

	/**
	 * Generate the javascript text representation of a java "short" value
	 *
	 * @return	Return the javascript text representation of the java "short" value
	 * @param item	The java "short" value that should be converted to javascript text
	 */
	static public String build (short item)
	{
		return (String.valueOf (item));
	}

	static private String buildObjectNull (Object obj)
	{
		return ("null");
	}		static public String buildObjectMinusOne ()
	{
		return ("-1");
	}

	static private String buildObjectArray (Object obj) throws Exception
	{
		// FIXME: This function should be implemented in a more clean way through reflection.
		Object [] arrayObject = null;
		boolean [] arrayBoolean = null;
		byte [] arrayByte = null;
		char [] arrayChar = null;
		double [] arrayDouble = null;
		float [] arrayFloat = null;
		int [] arrayInt = null;
		long [] arrayLong = null;
		short [] arrayShort = null;
		char typeChar;
		int size;
		String result;
		int i;

		typeChar = obj.getClass ().getName ().charAt (1);
		switch (typeChar) {
		case '[':
		case 'L':
			arrayObject = (Object []) obj;
			size = arrayObject.length;
			break;
		case 'Z':
			arrayBoolean = (boolean []) obj;
			size = arrayBoolean.length;
			break;
		case 'B':
			arrayByte = (byte []) obj;
			size = arrayByte.length;
			break;
		case 'C':
			arrayChar = (char []) obj;
			size = arrayChar.length;
			break;
		case 'D':
			arrayDouble = (double []) obj;
			size = arrayDouble.length;
			break;
		case 'F':
			arrayFloat = (float []) obj;
			size = arrayFloat.length;
			break;
		case 'I':
			arrayInt = (int []) obj;
			size = arrayInt.length;
			break;
		case 'J':
			arrayLong = (long []) obj;
			size = arrayLong.length;
			break;
		case 'S':
			arrayShort = (short []) obj;
			size = arrayShort.length;
			break;
		default:
			throw new Exception ("Unknown type");
		}
		result = "[";
		for (i = 0; i < size; ++i) {
			switch (typeChar) {
			case '[':
			case 'L':
				result += build (arrayObject [i]);
				break;
			case 'Z':
				result += build (arrayBoolean [i]);
				break;
			case 'B':
				result += build (arrayByte [i]);
				break;
			case 'C':
				result += build (arrayChar [i]);
				break;
			case 'D':
				result += build (arrayDouble [i]);
				break;
			case 'F':
				result += build (arrayFloat [i]);
				break;
			case 'I':
				result += build (arrayInt [i]);
				break;
			case 'J':
				result += build (arrayLong [i]);
				break;
			case 'S':
				result += build (arrayShort [i]);
				break;
			default:
				throw new Exception ("Unknown type");
			}
			result += (i != (size - 1)) ? "," : "";
		}
		result += "]";
		return (result);
	}

	static private String buildObjectBoolean (Boolean obj)
	{
		return (build (obj.booleanValue ()));
	}

	static private String buildObjectByte (Byte obj)
	{
		return (build (obj.byteValue ()));
	}

	static private String buildObjectDouble (Double obj)
	{
		return (build (obj.doubleValue ()));
	}

	static private String buildObjectFloat (Float obj)
	{
		return (build (obj.floatValue ()));
	}

	static private String buildObjectInteger (Integer obj)
	{
		return (build (obj.intValue ()));
	}

	static private String buildObjectLong (Long obj)
	{
		return (build (obj.longValue ()));
	}

	static private String buildObjectShort (Short obj)
	{
		return (build (obj.shortValue ()));
	}

	static private String buildObjectGeneric (Object obj)
	{
		String objString, result;
		int i, len;
		char c;

		objString = obj.toString ();
		result = "'";
		for (i = 0, len = objString.length (); i < len; ++i) {
			c = objString.charAt (i);
			switch (c) {
			case '\n':
				result += "\\n";
				break;
			case '\r':
				result += "\\r";
				break;
			case '\\':
				result += "\\\\";
				break;
			case '\'':
				result += "\\'";
				break;
			default:
				result += c;
			}
		}
		result += "'";
		return (result);
	}
}
