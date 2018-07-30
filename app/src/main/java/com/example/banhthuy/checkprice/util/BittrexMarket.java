package com.example.banhthuy.checkprice.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by banhthuy on 1/13/2018.
 */

public class BittrexMarket {
    NumberFormat formatter = new DecimalFormat("#0.0000000");

    private String market = "";
    private double lastPrice = 0;
    private double highPrice = 0;
    private double lowPrice = 0;
    private double change = 0;
    private double previousDay = 0;
    private int type = -1;
    private double volumn = 0;

    public BittrexMarket(JSONObject json){

        try {
            market = json.getString("MarketName");
        } catch (JSONException e) {
            market = "null";
//            e.printStackTrace();
        }
        try {
            lastPrice = json.getDouble("Last");
        } catch (JSONException e) {
//            e.printStackTrace();
            lastPrice = 0;
        }
        try {
            highPrice = json.getDouble("High");
        } catch (JSONException e) {
//            e.printStackTrace();
            highPrice = 0;
        }
        try {
            lowPrice = json.getDouble("Low");
        } catch (JSONException e) {
//            e.printStackTrace();
            lowPrice = 0;
        }
        try {
            previousDay = json.getDouble("PrevDay");
        } catch (JSONException e) {
//            e.printStackTrace();
            previousDay = -1;
        }
        try {
            volumn = json.getDouble("BaseVolume");
        } catch (JSONException e) {
//            e.printStackTrace();
            volumn = 0;
        }
        change = (lastPrice / previousDay - 1) * 100;
        setType();
        return;
    }

    private void setType() {
        if (market.substring(0, 4).toUpperCase().equals("BTC-")) {
            type = 1;
        } else if (market.substring(0, 4).toUpperCase().equals("USDT")) {
            type = 0;
        } else if (market.substring(0, 4).toUpperCase().equals("ETH-")) {
            type = 2;
        } else {
            type = -1;
        }
    }

    public double getVolumn() {
        return volumn;
    }

    public String getMarket() {
        return market;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public double gethighPrice() {
        return highPrice;
    }

    public double getlowPrice() {
        return lowPrice;
    }

    public int getType() {
        return type;
    }

    public double getChange() {
        return change;
    }

    public String toStringBTC() {
        String rs = "";
        if (market.length() >= 9) {
            rs = market.concat(":");
        } else if (market.length() == 8) {
            rs = market.concat(":");
        } else if (market.length() < 7) {
            rs = market.concat(":  ");
        } else {
            rs = market.concat(": ");
        }

        rs = rs.replace("BTC-", "");
        if (market.length() >= 9) {
            rs = rs.concat(formatBTC(lastPrice)).concat(" " + formatChange(change));
        } else if (market.length() > 7) {
            rs = rs.concat(formatBTC(lastPrice)).concat("  " + formatChange(change));
        } else {
            rs = rs.concat(formatBTC(lastPrice)).concat("  " + formatChange(change));
        }

        return rs;
    }

    public String formatChange(double change) {
        String rString = "";
        if (change > 0) {
            rString = " " + new DecimalFormat("0.00").format(change).concat("%");
        } else {
            rString = new DecimalFormat("0.00").format(change).concat("%");
        }

        return rString;
    }

    public String formatBTC(double price) {
        String rString = "";
        rString = new DecimalFormat("0.0000000").format(lastPrice);
        return rString;

    }

    public String toStringCSV() {
        if (type == 0) {
            return market.replace("USDT-", "") + "," + lastPrice + "," + highPrice + "," + lowPrice;
        } else if (type == 1) {
            return market.replace("BTC-", "") + "," + lastPrice + "," + change;
        }
        return "";
    }

    public String toStringUSDT() {
        String rs = "";
        if (market.length() >= 9) {
            rs = market.concat(":");
        } else {
            rs = market.concat(": ");
        }

        rs = rs.replace("USDT-", "");

        if (market.length() >= 9) {
            rs = rs.concat(formatUSDT(lastPrice) + "  " + formatUSDT(highPrice) + "  " + formatUSDT(lowPrice) + "");
        } else {
            rs = rs.concat(formatUSDT(lastPrice) + "  " + formatUSDT(highPrice) + "  " + formatUSDT(lowPrice) + "");
        }

        return rs;
    }

    public String formatUSDT(double data) {
        String rString = "";
        double temp = data;
        // NumberFormat formatter = new DecimalFormat("#0.0000000");

        if (temp > 10000) {
            rString = new DecimalFormat("00000").format(data);
            return rString;
        }
        if (temp > 1000) {
            rString = new DecimalFormat("0000").format(data);
            return rString.concat(" ");
        }
        if (temp > 100) {
            rString = new DecimalFormat("000").format(data);
            return rString.concat("  ");
        }
        if (temp > 10) {
            rString = new DecimalFormat("00.0").format(data);
            return rString.concat(" ");
        }
        if (temp > 0) {
            rString = new DecimalFormat("0.000").format(data);
            return rString.concat("");
        }

        rString = new DecimalFormat("0.000").format(data);
        return rString.concat("");
    }

}
