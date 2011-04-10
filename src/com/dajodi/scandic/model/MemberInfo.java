package com.dajodi.scandic.model;

import java.util.Date;
import java.util.List;

import com.dajodi.scandic.R;

/**
 * Represents scandic membership info.
 * 
 * THIS MUST stay consistent between releases.
 */
public class MemberInfo {

    public enum Level {
        FIRST_FOOR("1st", R.string.level_first_floor),
        SECOND_FLOOR("2nd", R.string.level_second_floor),
        THIRD_FLOOR("3rd", R.string.level_third_floor),
        TOP_FLOOR("top", R.string.level_top_floor),
        UNKNOWN("?", R.string.level_unknown);

        private final String englishText;
        private final int resId;
        
        private Level(String englishText, int resId) {
            this.englishText = englishText;
            this.resId = resId;
        }

        public static Level fromEnglishText(String text) {
            for (Level level : values()) {
                if (text.toLowerCase().contains(level.englishText)) {
                    return level;
                }
            }
            return UNKNOWN;
        }
        
        public int getResourceId() {
        	return this.resId;
        }
    }

    private int points;
    private String membershipId;
    private Level level;
    private int qualifyingNights;
    private List<ScandicStay> staysLast12Months;
    private Date lastUpdated;

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(String membershipId) {
        this.membershipId = membershipId;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public int getQualifyingNights() {
        return qualifyingNights;
    }

    public void setQualifyingNights(int qualifyingNights) {
        this.qualifyingNights = qualifyingNights;
    }

    public List<ScandicStay> getStaysLast12Months() {
        return staysLast12Months;
    }

    public void setStaysLast12Months(List<ScandicStay> staysLast12Months) {
        this.staysLast12Months = staysLast12Months;
    }

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}
}
