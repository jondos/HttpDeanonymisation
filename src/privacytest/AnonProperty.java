package privacytest;
import java.awt.Color;

/**
 * 
 */

class AnonProperty
{
	private String m_strName;
	private String m_strValue;
	private AnonPropertyAction m_action;
	private int m_rating;
	
	public final static int RATING_NONE = 0;
	public final static int RATING_GOOD = 1;
	public final static int RATING_BAD = 2;
	public final static int RATING_OKISH = 3;
	
	public AnonProperty(String a_strName, String a_strValue, int a_rating)
	{
		m_strName = a_strName;
		m_strValue = a_strValue;
		m_rating = a_rating;
	}
	
	public AnonProperty(String a_strName, String a_strValue, String a_strAction, String a_strActionUrl, int a_rating)
	{
		m_strName = a_strName;
		m_strValue = a_strValue;
		m_action = new AnonPropertyAction(a_strAction, a_strActionUrl);
		m_rating = a_rating;
	}
	
	public Color getRatingColor()
	{
		switch(m_rating)
		{
		default:
		case RATING_NONE:
			return Color.white;
		case RATING_BAD:
			return Color.red;
		case RATING_OKISH:
			return new Color(255, 165, 0);
		case RATING_GOOD:
			return Color.green;
		}
	}
	
	public String toString()
	{
		return m_strName;
	}
	
	public String getValue()
	{
		return m_strValue;
	}
	
	public AnonPropertyAction getAction()
	{
		return m_action;
	}
	
	public int getRating()
	{
		return m_rating;
	}
}