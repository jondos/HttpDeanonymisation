package privacytest;

public class AnonPropertyAction
{
	private String m_strName;
	private String m_strUrl;
	
	public AnonPropertyAction(String a_strName, String a_strUrl)
	{
		m_strName = a_strName;
		m_strUrl = a_strUrl;
	}
	
	public String toString()
	{
		return m_strName;
	}
	
	public String getUrl()
	{
		return m_strUrl;
	}
}
