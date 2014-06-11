package com.example.jp.jiu_jitsu;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;

public class CalendarMapper {

	private static final String ACCOUNT_NAME = "aditya619@googlemail.com";
	private static final String INT_NAME_PREFIX = "priv";

	private static Uri buildCalUri() {
	    return CalendarContract.Calendars.CONTENT_URI
	            .buildUpon()
	            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
	            .appendQueryParameter(Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
	            .appendQueryParameter(Calendars.ACCOUNT_TYPE, ACCOUNT_NAME)
	            .build();
		}

	private static ContentValues buildContentValues() {
	    String dispName = "JP Jiu Jitsu Calendar";  //Calendar.getName() returns a String
	    String intName = INT_NAME_PREFIX + dispName;
	    final ContentValues cv = new ContentValues();
	    cv.put(Calendars.ACCOUNT_NAME, ACCOUNT_NAME);
	    cv.put(Calendars.ACCOUNT_TYPE, ACCOUNT_NAME);
	    cv.put(Calendars.NAME, intName);
	    cv.put(Calendars.CALENDAR_DISPLAY_NAME, dispName);
	    cv.put(Calendars.CALENDAR_COLOR, "-9326937");  //Calendar.getColor() returns int
	    cv.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER);
	    cv.put(Calendars.OWNER_ACCOUNT, ACCOUNT_NAME);
	    cv.put(Calendars.VISIBLE, 1);
	    cv.put(Calendars.SYNC_EVENTS, 1);
	    return cv;
		}

	public static void addCalendar(ContentResolver cr) {
	    final ContentValues cv = buildContentValues();

	    Uri calUri = buildCalUri();
	    cr.insert(calUri, cv);
		}
}
