package com.example.jp.jiu_jitsu;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ScheduleParser {
	public ArrayList<EventsData> readSchedule(InputStream input)
	{
		ArrayList<EventsData> eventList=new ArrayList<EventsData>();
		try {
			InputStream XmlFile = input;
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(XmlFile);
			doc.getDocumentElement().normalize();
			long startMillis=0;
			long endMillis=0;
			//Parse xml files by "event" node
			NodeList nList = doc.getElementsByTagName("event");
			
			for (int temp = 0; temp < nList.getLength(); temp++) 
			{
			   Node nNode = nList.item(temp);
			   if (nNode.getNodeType() == Node.ELEMENT_NODE) 
			   { 
			      Element eElement = (Element) nNode;
			      startMillis=getMilliSeconds(getTagValue("eventStartTime", eElement),getTagValue("eventDay", eElement));
			      endMillis=getMilliSeconds(getTagValue("eventEndTime", eElement),getTagValue("eventDay", eElement));
			      eventList.add(new EventsData(getTagValue("eventName", eElement),startMillis,endMillis));
			   }
			}
		  } catch (Exception e) {
			e.printStackTrace();
		  }
		return eventList;
	}
	private long getMilliSeconds(String time, String eventDay)
	{
		String days[] = {"sunday","monday","tuesday","wednesday","thursday","friday","saturday"};
		int dayOfWeek=0;
		for(int i=0;i<days.length;i++)
		{
			if(days[i].equalsIgnoreCase(eventDay))
			{
				dayOfWeek=i+1;
				break;
			}
		}
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_WEEK,dayOfWeek);
		cal.set(Calendar.HOUR_OF_DAY, 0);            
		cal.set(Calendar.MINUTE, 0);                 
		cal.set(Calendar.SECOND, 0);                 
		cal.set(Calendar.MILLISECOND, 0);
		Float parseTime = Float.parseFloat(time);
		int hours = parseTime.intValue();
		parseTime-=hours;
		cal.add(Calendar.HOUR_OF_DAY, hours);
		cal.add(Calendar.MINUTE, (int) (parseTime*60));
		return cal.getTimeInMillis();
	}
	private static String getTagValue(String sTag, Element eElement) 
	{
			NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
			Node nValue = (Node) nlList.item(0);
			return nValue.getNodeValue();
	}
}
