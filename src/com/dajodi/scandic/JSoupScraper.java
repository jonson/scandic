package com.dajodi.scandic;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.protocol.HTTP;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dajodi.scandic.model.MemberInfo;
import com.dajodi.scandic.model.ScandicStay;

public class JSoupScraper implements HtmlScraper {

	@Override
	public Map<String, String> scrapeFormInputFields(InputStream inStream) {
		
		try {
			Document doc = Jsoup.parse(inStream, HTTP.ISO_8859_1, "");
			
			Element form = doc.body().getElementById("aspnetForm");
			
			Elements inputNodes = form.getElementsByTag("input");
			Map<String, String> inputMap = new HashMap<String, String>();

            for (Element element : inputNodes) {

                String name = element.attr("name");
                String value = element.attr("value");

                if (name != null) {
                    inputMap.put(name,value == null ? "" : value);
                } else {
                	//TODO: remove me
                    Log.d("Something weird");
                }
            }

            doc.empty();
            return inputMap;
		} catch (Exception e) {
			throw new ScandicHtmlException(e);
		}
	}
	
	@Override
	public MemberInfo scrapeMemberInfo(InputStream inStream) {
		
		Document doc;
		try {
			doc = Jsoup.parse(inStream, HTTP.ISO_8859_1, "");
			
			Element accountOverview = doc.getElementById("AccountOverview");
						
			String points = getStringFromNode(accountOverview, "ctl00_MainBodyRegion_AccountOverview1_totalPoints", "?");
	        String membershipNumber = getStringFromNode(accountOverview, "ctl00_MainBodyRegion_AccountOverview1_membershipNo", "?");
	        String membershipLevel = getStringFromNode(accountOverview, "ctl00_MainBodyRegion_AccountOverview1_memberLevel", "?");
	        String nights = getStringFromNode(accountOverview, "ctl00_MainBodyRegion_AccountOverview1_strngNights", "?");
	        
	        int qualNights = Util.UNKNOWN_NIGHTS;

	        if ("?".equals(nights)) {
	        	boolean noTransactions = accountOverview.select("#ctl00_MainBodyRegion_AccountOverview1_NoTransaction").size() == 1;
	        	if (noTransactions) {
	        		// as expected
	        		qualNights = Util.NO_NIGHTS;
	        	} else {
	        		Log.d("somethign really strange, number of nights could not be found");
	        	}
	        } else {
	        	qualNights = Util.parseNumNights(nights);
	        }
	        
	        MemberInfo.Level level = MemberInfo.Level.fromEnglishText(membershipLevel);

	        List<ScandicStay> stays = getStays(accountOverview);

	        MemberInfo memberInfo = new MemberInfo();
	        memberInfo.setMembershipId(membershipNumber);
	        memberInfo.setLevel(level);
	        memberInfo.setPoints(Util.parseInt(points, Util.UNKNOWN_POINTS));
	        memberInfo.setQualifyingNights(qualNights);
	        memberInfo.setStaysLast12Months(stays);
	        memberInfo.setLastUpdated(new Date());

	        return memberInfo;
        
		} catch (IOException e) {
			throw new ScandicHtmlException(e);
		}
		
	}

	private static String getStringFromNode(Element accountOverview,
			String id, String defaultValue) {
		Element node = accountOverview.getElementById(id);
		if (node == null)
			return defaultValue;
		return Util.trimIfNonNull(node.text());
	}

	private List<ScandicStay> getStays(Element accountOverview) {
		Element tableNode = accountOverview.getElementById("ctl00_MainBodyRegion_AccountOverview1_tableTransactions");
		
		if (tableNode == null) {
			return Collections.emptyList();
		}
		
		Elements trs = tableNode.getElementsByTag("tr");
		
		List<ScandicStay> stays = new ArrayList<ScandicStay>();
        int order = 0;
        for (Element tr : trs) {
        	if (tr.getElementsByTag("th").isEmpty()) {
        		Elements tds = tr.getElementsByTag("td");
        		if (tds.size() == 3) {
        			String location = Util.trimIfNonNull(tds.get(0).text());
                    String date = Util.trimIfNonNull(tds.get(1).text());
                    String stayPoints = Util.trimIfNonNull(tds.get(2).text());
                    ScandicStay stay = new ScandicStay();
                    
                    Date[] dates = Util.parseDates(date);
                    int numNights = Util.daysBetween(dates[0],dates[1]);

                    stay.setHotelName(location);
                    stay.setNumPoints(Integer.parseInt(stayPoints));
                    stay.setFromDate(dates[0]);
                    stay.setToDate(dates[1]);
                    stay.setNumNights(numNights);
                    stay.setHtmlOrder(order);
                    stays.add(stay);
                    order++;
        		} else {
        			throw new ScandicHtmlException("unknown table node, html is funky.  could hide row if this is a serious problem.");
        		}
        	}
        }

		return stays;
	}

}
