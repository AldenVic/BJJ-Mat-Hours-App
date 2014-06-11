package com.example.jp.jiu_jitsu;

public class EventsData {
	String eventName;
	long eventStart;
	long eventEnd;
	long eventId=0;
	public EventsData(String name, long start, long end) {
		// TODO Auto-generated constructor stub
		this.eventName=name;
		this.eventStart=start;
		this.eventEnd=end;
	}
	public long getEventId() {
		return eventId;
	}
	public void setEventId(long eventId) {
		this.eventId = eventId;
	}
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	public long getEventStart() {
		return eventStart;
	}
	public void setEventStart(long eventStart) {
		this.eventStart = eventStart;
	}
	public long getEventEnd() {
		return eventEnd;
	}
	public void setEventEnd(long eventEnd) {
		this.eventEnd = eventEnd;
	}
}