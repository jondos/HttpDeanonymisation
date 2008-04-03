package util;

import java.util.Locale;
import java.util.Hashtable;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.text.MessageFormat;

/**
 * Use this class to display GUI texts in the user's language. The texts Strings are loaded from
 * properties files, so-called Resource Bundles. The default resource bundle is english and its
 * name ends with '_en.properties'. Resource bundle files in other languages must have language specific
 * endings as given by ISO 639. The default resource bundle must always be present and must contain all
 * language strings. The other bundles may contain a subset of these strings.
 * @see http://www.w3.org/WAI/ER/IG/ert/iso639.htm
 */
public final class Messages
{
	private static ResourceBundle ms_resourceBundle = null;
	private static ResourceBundle ms_defaultResourceBundle = null;
	private static Locale ms_locale;
	private static Hashtable ms_cachedMessages;

	private Messages()
	{
	}

	/**
	 * Initialises the resource bundle with the System default Locale. The initialisation may be repeated
	 * with a new Locale.
	 * @param a_resourceBundleFilename a file name for the resource bundle; the language code for the
	 * locale will be added programmatically (e.g. _en, _de, ...).
	 */
	public static void init(String a_resourceBundleFilename)
	{
		// Load Texts for Messages and Windows
		init(Locale.getDefault(), a_resourceBundleFilename);
	}

	private static String getBundleLocalisedFilename(String a_resourceBundleFilename, Locale a_locale)
	{
		String strLocale = "_";

		if (a_resourceBundleFilename == null)
		{
			return null;
		}

		if (a_locale == null)
		{
			a_locale = Locale.getDefault();
		}

		if (a_locale == null || a_locale.getLanguage().trim().length() == 0)
		{
			strLocale += "en";
		}
		else
		{
			strLocale += a_locale.getLanguage();
		}
		strLocale += ".properties";

		return a_resourceBundleFilename + strLocale;
	}

	/**
	 * Initialises the resource bundle with the specified Locale. The initialisation may be repeated
	 * with a new Locale.
	 * @param locale a Locale
	 * @param a_resourceBundleFilename a file name for the resource bundle; the language code for the
	 * locale will be added programmatically (e.g. _en, _de, ...).
	 */
	public static synchronized void init(Locale locale, String a_resourceBundleFilename)
	{
		if (ms_locale != null)
		{
			// the first bundle has been loaded; set Englisch as safe default
			Locale.setDefault(Locale.ENGLISH);
		}

		try
		{
			if (ms_defaultResourceBundle == null)
			{
				ms_defaultResourceBundle = PropertyResourceBundle.getBundle(a_resourceBundleFilename,
					Locale.ENGLISH);
			}
		}
		catch (Exception a_e)
		{
			System.exit(1);
		}
		ms_resourceBundle = ms_defaultResourceBundle;

		try
		{
			//ms_resourceBundle = PropertyResourceBundle.getBundle(a_resourceBundleFilename, locale);
			ms_resourceBundle = new PropertyResourceBundle(
				 ResourceLoader.loadResourceAsStream(
						 getBundleLocalisedFilename(a_resourceBundleFilename, locale), true));
		}
		catch (Exception a_e)
		{
			try
			{
				ms_resourceBundle = PropertyResourceBundle.getBundle(a_resourceBundleFilename, locale);
			}
			catch (Exception a_e2)
			{
				try
				{
					if (locale == null || !locale.equals(Locale.getDefault()))
					{
						locale = Locale.getDefault();
						ms_resourceBundle = PropertyResourceBundle.getBundle(a_resourceBundleFilename, locale);
					}
					else
					{
						throw a_e;
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}


		ms_cachedMessages = new Hashtable();
		ms_locale = locale;
	}

	public static boolean isInitialised()
	{
		return ms_locale != null;
	}

	/**
	 * Returns the Locale that is used by this class to get the messages.
	 * @return the Locale that is used by this class to get the messages
	 */
	public static Locale getLocale()
	{
		return ms_locale;
	}

	public static void setLocale(Locale a_locale)
	{
		if (a_locale != null)
		{
			ms_locale = a_locale;
		}
	}

	/**
	 * Gets the localised String for a given key.
	 * @param a_key a key for the localised String
	 * @return the localised String
	 */
	public static String getString(String a_key)
	{
		String string = (String)ms_cachedMessages.get(a_key);

		if (string != null)
		{
			return string;
		}

		try
		{
			string = ms_resourceBundle.getString(a_key);
			if (string == null || string.trim().length() == 0)
			{
				throw new MissingResourceException("Resource is empty",
					PropertyResourceBundle.class.getName(), a_key);
			}

		}
		catch (Exception e)
		{
			try
			{
				if (ms_resourceBundle != ms_defaultResourceBundle)
				{
					string = ms_defaultResourceBundle.getString(a_key);
				}
			}
			catch (Exception a_e)
			{
				string = null;
			}

			if (string == null || string.trim().length() == 0)
			{
				string = a_key;
			}
		}

		ms_cachedMessages.put(a_key, string);
		return string;
	}

	/**
	 * Gets the localised String for a given key. If the String contains formatting patterns,
	 * these patterns are replaced by the corresponding arguments given in an object array.
	 * For a detailed description of the formatting options please see class
	 * <code> java.text.MessageFormat </code>.
	 * @param a_key a key for the localised String
	 * @param a_arguments an object array that contains the objects that replace
	 * @return the localised String with inserted arguments
	 * @see java.text.MessageFormat
	 */
	public static String getString(String a_key, Object[] a_arguments)
	{
		return MessageFormat.format(getString(a_key), a_arguments);
	}

	/**
	 * Gets the localised String for a given key. If the String contains a formatting pattern,
	 * this pattern is replaced by the given argument object. Note that this method allows only
	 * one argument.
	 * For a detailed description of the formatting options please see class
	 * <code> java.text.MessageFormat </code>.
	 * @param a_key a key for the localised String
	 * @param a_argument a object that is inserted into the message String
	 * @return the localised String with inserted arguments
	 * @see java.text.MessageFormat
	 */
	public static String getString(String a_key, Object a_argument)
	{
		return getString(a_key, Messages.toArray(a_argument));
	}
	
	/**
	 * Creates an Object array from a single Object.
	 * @param a_object an Object
	 * @return an Object array containing the given Object or an empty array if the Object was null
	 */
	private static Object[] toArray(Object a_object)
	{
		Object[] value;

		if (a_object != null)
		{
			value = new Object[1];
			value[0] = a_object;
		}
		else
		{
			value = new Object[0];
		}

		return value;
	}
}
