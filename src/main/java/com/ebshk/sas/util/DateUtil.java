package com.ebshk.sas.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
	
	public static Date toDate(String dateStr, SimpleDateFormat sdf) {
		Date date = null;
		try {
			date = sdf.parse(dateStr);
			return date;
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}		
	}
	
	public static String addDate(String currentDate, SimpleDateFormat sdf) {
		Calendar c = Calendar.getInstance();
		try {
			c.setTime(sdf.parse(currentDate));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		c.add(Calendar.DATE, 1);  // number of days to add
		return sdf.format(c.getTime());
	}

}
