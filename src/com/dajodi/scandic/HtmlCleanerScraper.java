package com.dajodi.scandic;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import com.dajodi.scandic.model.MemberInfo;
import com.dajodi.scandic.model.ScandicStay;

/**
 * Scraper based on HtmlCleaner.
 * 
 * This initial implementation may not work long term, since this doesn't seem to play
 * nice on 1.6 devices w/ 16mb RAM.
 * 
 * @author jon
 *
 */
public class HtmlCleanerScraper implements HtmlScraper {

	private HtmlCleaner cleaner;
	
	public HtmlCleanerScraper() {
		cleaner = new HtmlCleaner();
		cleaner.getProperties().setIgnoreQuestAndExclam(true);
		cleaner.getProperties().setOmitComments(true);
	}
	
	@Override
	public Map<String, String> scrapeFormInputFields(InputStream in) {

        try {
        	
        	// helper that prints memory usage
//            HtmlCleaner cleaner = new HtmlCleaner(new DefaultTagProvider() {
//            	@Override
//            	public TagInfo getTagInfo(String tagName) {
//            		Util.printMemory();
//            		return super.getTagInfo(tagName);
//            	}
//            });
            
            
            Log.d("Started cleaning");
            long before = System.currentTimeMillis();
            TagNode node = cleaner.clean(in);
            Log.d("Took " + (System.currentTimeMillis() - before) + "ms");

            String formXpath = "//form[@name='aspnetForm']";

            TagNode formNode = (TagNode) node.evaluateXPath(formXpath)[0];

            Object[] inputNodes = {};

            // get all the <input> fields
            inputNodes = formNode.evaluateXPath("//input");

            Map<String, String> inputMap = new HashMap<String, String>();

            for (Object objectNode : inputNodes) {

                TagNode inputNode = (TagNode) objectNode;
                String name = inputNode.getAttributeByName("name");
                String value = inputNode.getAttributeByName("value");

                if (name != null) {
                    inputMap.put(name,value == null ? "" : value);
                } else {
                	//TODO: remove me
                    Log.d("Something weird");
                }
            }

            return inputMap;

        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
	}

	@Override
	public MemberInfo scrapeMemberInfo(InputStream in) {
		try {
		return parseMemberInfo(in);
		} catch (Exception e) {
			if (e instanceof ScandicHtmlException) {
				throw (ScandicHtmlException)e;
			}
			throw new ScandicHtmlException(e);
		}
	}
	
	public MemberInfo parseMemberInfo(InputStream inputStream) throws Exception {
        
        cleaner.getProperties().setOmitComments(true);

        Log.d("Started cleaning member info");
        long before = System.currentTimeMillis();
        TagNode rootNode = cleaner.clean(inputStream);
        Log.d("Took " + (System.currentTimeMillis() - before) + "ms");


        // need check here
        TagNode[] accountOverviewNodes = rootNode.getElementsByAttValue("id", "AccountOverview", true, true);

        if (accountOverviewNodes.length != 1) {
        	throw new ScandicHtmlException("Could not find AccountOverview div, cannot parse any info");
        }

        TagNode accountOverviewNode = accountOverviewNodes[0];

        String points = getStringFromNode(accountOverviewNode, "ctl00_MainBodyRegion_AccountOverview1_totalPoints", "?");
        String membershipNumber = getStringFromNode(accountOverviewNode, "ctl00_MainBodyRegion_AccountOverview1_membershipNo", "?");
        String membershipLevel = getStringFromNode(accountOverviewNode, "ctl00_MainBodyRegion_AccountOverview1_memberLevel", "?");
        String nights = getStringFromNode(accountOverviewNode, "ctl00_MainBodyRegion_AccountOverview1_strngNights", "?");

        int qualNights = Util.UNKNOWN_NIGHTS;

        if ("?".equals(nights)) {
        	boolean noTransactions = accountOverviewNode.getElementsByAttValue("id", "ctl00_MainBodyRegion_AccountOverview1_NoTransaction", true, true).length == 1;
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

        List<ScandicStay> stays = getStays(accountOverviewNode);

        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setMembershipId(membershipNumber);
        memberInfo.setLevel(level);
        memberInfo.setPoints(Util.parseInt(points, Util.UNKNOWN_POINTS));
        memberInfo.setQualifyingNights(qualNights);
        memberInfo.setStaysLast12Months(stays);
        memberInfo.setLastUpdated(new Date());

        return memberInfo;
    }
	
	static String getStringFromNode(TagNode node, String id, String defaultValue) {
    	TagNode[] nodes = node.getElementsByAttValue("id", id, true, true);

    	if (nodes.length != 1)
    		return defaultValue;

    	return Util.trimIfNonNull(nodes[0].getText().toString());
    }

    static List<ScandicStay> getStays(TagNode accountOverviewNode) {

    	TagNode[] tableNodes = accountOverviewNode.getElementsByAttValue("id", "ctl00_MainBodyRegion_AccountOverview1_tableTransactions", true, true);

    	if (tableNodes.length != 1) {
    		return Collections.emptyList();
    	}

    	TagNode tableNode = tableNodes[0];
        TagNode[] rows = tableNode.getElementsByName("tr", true);

        List<ScandicStay> stays = new ArrayList<ScandicStay>();
        int order = 0;
        for (TagNode row : rows) {
            if (row.findElementByName("th", false) == null) {
                TagNode[] tdNodes = row.getElementsByName("td", false);

                if (tdNodes.length == 3) {

                	// i think we can assume these are non-null
                	String location = Util.trimIfNonNull(tdNodes[0].getText().toString());
                    String date = Util.trimIfNonNull(tdNodes[1].getText().toString());
                    String stayPoints = Util.trimIfNonNull(tdNodes[2].getText().toString());

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
