package com.dajodi.scandic;

import java.io.InputStream;
import java.util.Map;

import com.dajodi.scandic.model.MemberInfo;

public interface HtmlScraper {
	
	/**
	 * Parses the input fields from a generic page (about us??)
	 * 
	 * @param uri
	 * @return
	 */
	public Map<String, String> scrapeFormInputFields(InputStream inStream);
	
	/**
	 * Parses the member info from a frequent guest page.
	 * 
	 * @param uri
	 * @return
	 */
	public MemberInfo scrapeMemberInfo(InputStream inStream);
}
