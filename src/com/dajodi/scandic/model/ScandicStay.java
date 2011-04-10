package com.dajodi.scandic.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: jon
 * Date: 11-04-01
 * Time: 3:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScandicStay implements Serializable {

    private String hotelName;
    private Date fromDate;
    private Date toDate;
    private int numNights;
    private int numPoints;
    private int htmlOrder;

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public int getNumNights() {
        return numNights;
    }

    public void setNumNights(int numNights) {
        this.numNights = numNights;
    }

    public int getNumPoints() {
        return numPoints;
    }

    public void setNumPoints(int numPoints) {
        this.numPoints = numPoints;
    }

    @Override
    public String toString() {
        return "ScandicStay{" +
                "hotelName='" + hotelName + '\'' +
                ", fromDate=" + fromDate +
                ", toDate=" + toDate +
                ", numNights=" + numNights +
                ", numPoints=" + numPoints +
                '}';
    }

	public void setHtmlOrder(int htmlOrder) {
		this.htmlOrder = htmlOrder;
	}

	public int getHtmlOrder() {
		return htmlOrder;
	}
}
