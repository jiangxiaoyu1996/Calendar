package calendar;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.io.*;
import java.util.*;

/**
 * The calendar model in MVC Pattern.
 */
public class CalendarModel{

  /**
   * A specific day.
   */
  private GregorianCalendar cal;
  /**
   * Day and events map relationship.
   */
  private HashMap<String, ArrayList<Event>> map;
  /**
   * The array list of listeners.
   */
  private ArrayList<ChangeListener> listeners;
  /**
   * Determine the month change.
   */
  private boolean monthChanged = false;
  /**
   * The max days in a month.
   */
  private int maxDays;
  /**
   * The selected day in a month.
   */
  private int selectedDay;

  /**
   * Construct a calendar object. Initialize instances variables.
   */
  public CalendarModel(){
	//Set it to today.
	cal = new GregorianCalendar();
	map = new HashMap<>();
	listeners = new ArrayList<>();
	maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
	selectedDay = cal.get(Calendar.DATE);
	//Load event.txt.
	loadEvents();
  }

  /**
   * Adds ChangeListeners to listeners array list.
   *
   * @param c the ChangeListener to add
   */
  public void addChangeListener(ChangeListener c) {
	listeners.add(c);
  }

  /**
   * Updates all ChangeListeners in listeners array list.
   */
  public void update() {
	for (ChangeListener c : listeners) {
	  c.stateChanged(new ChangeEvent(this));
	}
  }

  /**
   * Sets the user selected day.
   *
   * @param day the day to set
   */
  public void setSelectedDate(int day) {
	selectedDay = day;
  }

  /**
   * Gets the user selected day.
   *
   * @return the selected day
   */
  public int getSelectedDay() {
	return selectedDay;
  }

  /**
   * Gets the current year the calendar is at.
   *
   * @return the current year
   */
  public int getCurrentYear() {
	return cal.get(Calendar.YEAR);
  }

  /**
   * Gets the current month the calendar is at.
   *
   * @return the current month
   */
  public int getCurrentMonth() {
	return cal.get(Calendar.MONTH);
  }

  /**
   * Gets the value representing the day of the week
   *
   * @param i the day of the month
   * @return the day of the week (1-7)
   */
  public int getDayOfWeek(int i) {
	cal.set(Calendar.DAY_OF_MONTH, i);
	return cal.get(Calendar.DAY_OF_WEEK);
  }

  /**
   * Gets the max number of days in a month.
   *
   * @return the max number of days in a month
   */
  public int getMaxDays() {
	return maxDays;
  }

  /**
   * Moves the calendar forward by one month.
   */
  public void nextMonth() {
	cal.add(Calendar.MONTH, 1);
	maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
	monthChanged = true;
	update();
  }

  /**
   * Moves the calendar backward by one month.
   */
  public void prevMonth() {
	cal.add(Calendar.MONTH, -1);
	maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
	monthChanged = true;
	update();
  }

  /**
   * Moves the selected day forward by one.
   */
  public void nextDay() {
	selectedDay++;
	if (selectedDay > cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
	  nextMonth();
	  selectedDay = 1;
	}
	update();
  }

  /**
   * Moves the selected day backward by one.
   */
  public void prevDay() {
	selectedDay--;
	if (selectedDay < 1) {
	  prevMonth();
	  selectedDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
	}
	update();
  }

  /**
   * Checks if the month has changed as a result of user interaction.
   *
   * @return true if the month changed, false if the month didn't change
   */
  public boolean isMonthChanged() {
	return monthChanged;
  }

  /**
   * Resets monthChanged to false.
   */
  public void resetMonthChanged() {
	monthChanged = false;
  }

  /**
   * Creates an event on the currently selected date.
   *
   * @param title     the title of the event
   * @param startTime the start time of the event
   * @param endTime   the end time of the event
   */
  public void createEvent(String title, String startTime, String endTime) {
	//Date format
	String date = (cal.get(Calendar.MONTH) + 1) + "/" + selectedDay + "/" + cal.get(Calendar.YEAR);
	Event e = new Event(title, date, startTime, endTime);
	ArrayList<Event> eventArray = new ArrayList<>();
	if (hasEvent(e.date)) {
	  eventArray = map.get(date);
	}
	eventArray.add(e);
	map.put(date, eventArray);
  }

  /**
   * Checks if specified date has any events scheduled.
   *
   * @param date the selected date in format MM/DD/YYYY
   * @return if the date has an event
   */
  public Boolean hasEvent(String date) {
	return map.containsKey(date);
  }

  /**
   * Checks if a new event has a time conflict with an existing event.
   *
   * @param timeStart the start time of the new event
   * @param timeEnd   the end time of the new event
   * @return whether there is a conflict in time
   */
  public Boolean hasEventConflict(String timeStart, String timeEnd) {
	String date = (getCurrentMonth() + 1) + "/" + selectedDay + "/" + getCurrentYear();
	//If there is no event scheduled on the day, return false
	if (!hasEvent(date)) {
	  return false;
	}

	ArrayList<Event> eventArray = map.get(date);
	Collections.sort(eventArray, timeComparator());

	int timeStartMins = convertHourToMin(timeStart), timeEndMins = convertHourToMin(timeEnd);
	for (Event e : eventArray) {
	  int eventStartTime = convertHourToMin(e.startTime), eventEndTime = convertHourToMin(e.endTime);
	  if (timeStartMins >= eventStartTime && timeStartMins < eventEndTime) {
		return true;
	  } else if (timeStartMins <= eventStartTime && timeEndMins > eventStartTime) {
		return true;
	  }
	}
	return false;
  }

  /**
   * Gets a string of all events on a particular date.
   *
   * @param date the date to get events for
   * @return a string of all events on specified date
   */
  public String getEvents(String date) {
	ArrayList<Event> eventArray = map.get(date);
	Collections.sort(eventArray, timeComparator());
	String events = "";
	for (Event e : eventArray) {
	  events += e.toString() + "\n";
	}
	return events;
  }

  /**
   * Saves all events to "events.ser".
   */
  public void saveEvents() {
	if (map.isEmpty()) {
	  return;
	}

	try {
	  FileWriter fw = new FileWriter("event.txt");
	  BufferedWriter bw = new BufferedWriter(fw);

	  for (String s : map.keySet()) {
		for (Event e : map.get(s)) {
		  String detail = s + " " + e.startTime + " to " + e.endTime + " " + e.title;
		  bw.write(detail);
		  bw.newLine();
		}
	  }
	  bw.close();
	} catch (IOException e) {
	  System.out.println(e.getMessage());
	}
  }

  /**
   * Loads all events from "events.ser".
   */
  @SuppressWarnings("unchecked")
  private void loadEvents() {
	String fileName = "event.txt";

	String input;

	try {
	  FileReader fr = new FileReader(fileName);
	  BufferedReader br = new BufferedReader(fr);

	  while ((input = br.readLine()) != null) {
		parseEventInput(input);
	  }

	  br.close();
	} catch (FileNotFoundException e) {
	  System.out.println("No such file found, because it is the first run.");
	} catch (IOException e) {
	  System.out.println(e.getMessage());
	}
  }

  /**
   * Converts 24:00 time to minutes
   *
   * @param time the time in 24 hour format
   * @return the time converted to minutes
   */
  private int convertHourToMin(String time) {
	int hours = Integer.valueOf(time.substring(0, 2));
	return hours * 60 + Integer.valueOf(time.substring(3));
  }

  private void parseEventInput(String input){
	String[] detail = input.split(" ");

	String dateString = detail[0];
	String startTime = detail[1];
	String endTime = detail[3];

	String eName = "";
	for (int i = 4; i < detail.length; i++) {
	  eName = eName + detail[i] + " ";
	}

	Event e = new Event(eName, dateString, startTime, endTime);
	if (map.containsKey(dateString)) {
	  map.get(dateString).add(e);
	} else {
	  ArrayList<Event> a = new ArrayList<>();
	  a.add(e);
	  map.put(dateString, a);
	}
  }

  /**
   * Comparator for comparing by time in format XX:XX.
   *
   * @return The comparator.
   */
  private static Comparator<Event> timeComparator() {
	return (arg0, arg1) -> {
	  if (arg0.startTime.substring(0, 2).equals(arg1.startTime.substring(0, 2))) {
		return Integer.parseInt(arg0.startTime.substring(3, 5)) - Integer.parseInt(arg1.startTime.substring(3, 5));
	  }
	  return Integer.parseInt(arg0.startTime.substring(0, 2)) - Integer.parseInt(arg1.startTime.substring(0, 2));
	};
  }

  /**
   * Event object containing event title, date, and time.
   */
  private static class Event implements Serializable{

	private static final long serialVersionUID = -6030371583841330976L;
	/**
	 * The title of the event.
	 */
	private String title;
	/**
	 * The date of the event.
	 */
	private String date;
	/**
	 * The start time of the event.
	 */
	private String startTime;
	/**
	 * The end time of the event.
	 */
	private String endTime;

	/**
	 * Construct a event object
	 * @param title     the title of the event
	 * @param date      the date of the event
	 * @param startTime the start time of the event
	 * @param endTime   the end time of the event
	 */
	private Event(String title, String date, String startTime, String endTime) {
	  this.title = title;
	  this.date = date;
	  this.startTime = startTime;
	  this.endTime = endTime;
	}

	/**
	 * Gets the event entry as a string.
	 * @return the event entry in format "XX:XX - XX:XX: title".
	 */
	public String toString() {
	  if (endTime.equals("")) {
		return startTime + ": " + title;
	  }
	  return startTime + " - " + endTime + ": " + title;
	}
  }
}
