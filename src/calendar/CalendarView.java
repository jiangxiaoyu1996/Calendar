package calendar;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;

/**
 * The calendar view of MVC Pattern
 */
public class CalendarView implements ChangeListener{

  /**
   * The calendar model.
   */
  private CalendarModel model;
  /**
   * Highlight date.
   */
  private int prevHighlight = 0;
  /**
   * The max days in a month.
   */
  private int maxDays;

  /**
   * The main frame of calendar.
   */
  private JFrame calendarFrame = new JFrame("Calendar");
  /**
   * The panel of month view.
   */
  private JPanel monthGridPanel = new JPanel();
  /**
   * The panel of day view.
   */
  private JPanel dayViewPanel = new JPanel();
  /**
   * The label of month.
   */
  private JLabel monthLabel = new JLabel();
  /**
   * The create event button.
   */
  private JButton create = new JButton("Create");
  /**
   * The next day button.
   */
  private JButton next = new JButton(">>");
  /**
   * The previous day button.
   */
  private JButton previous = new JButton("<<");
  /**
   * The events panel.
   */
  private JTextPane dayEventPane = new JTextPane();
  /**
   * The array list of JButton.
   */
  private ArrayList<JButton> dayBtns = new ArrayList<JButton>();

  /**
   * Constructs a view object.
   *
   * @param model the model to set
   */
  public CalendarView(CalendarModel model) {
	this.model = model;
	maxDays = model.getMaxDays();

	//The grid table of month.
	monthGridPanel.setLayout(new GridLayout(0, 7));
	dayButtonsSetUp();
	highlightDayButton();
	showDate(model.getSelectedDay());
	highlightSelectedDate(model.getSelectedDay() - 1);

	//The panel of events.
	dayEventPane.setPreferredSize(new Dimension(500, 250));
	dayEventPane.setEditable(false);

	//The month panel.
	JButton prevMonth = new JButton("<<");
	/*----------------------Controller-------------------------*/
	prevMonth.addActionListener(e -> {
	  model.prevMonth();
	  create.setEnabled(false);
	  next.setEnabled(false);
	  previous.setEnabled(false);
	  dayEventPane.setText("");
	});
	JButton nextMonth = new JButton(">>");
	nextMonth.addActionListener(e -> {
	  model.nextMonth();
	  create.setEnabled(false);
	  next.setEnabled(false);
	  previous.setEnabled(false);
	  dayEventPane.setText("");
	});
	/*---------------------------------------------------------*/

	JPanel monthPanel = new JPanel();
	monthPanel.setLayout(new BorderLayout());

	JPanel bPanel = new JPanel();
	bPanel.setLayout(new FlowLayout());
	bPanel.add(prevMonth);
	bPanel.add(nextMonth);

	monthLabel.setText(getMonthName(model.getCurrentMonth()) + " " + model.getCurrentYear());
	JPanel northP = new JPanel();
	northP.setLayout(new BorderLayout());
	northP.add(bPanel, BorderLayout.NORTH);
	northP.add(monthLabel, BorderLayout.CENTER);

	JLabel dayOfWeek = new JLabel("   Sun            Mon              Tue             Wed              Thu              Fri             Sat");

	monthPanel.add(northP, BorderLayout.NORTH);
	monthPanel.add(dayOfWeek, BorderLayout.CENTER);
	monthPanel.add(monthGridPanel, BorderLayout.SOUTH);

	//The day panel.
	dayViewPanel.setLayout(new BorderLayout());
	JScrollPane dayScrollPane = new JScrollPane(dayEventPane);
	JPanel buttonPanel = new JPanel();
	/*-------------------------Controller-----------------------*/
	next.addActionListener(e -> model.nextDay());
	previous.addActionListener(e -> model.prevDay());
	/*----------------------------------------------------------*/
	buttonPanel.add(previous);
	buttonPanel.add(next);
	dayViewPanel.add(buttonPanel, BorderLayout.NORTH);
	dayViewPanel.add(dayScrollPane, BorderLayout.CENTER);

	//The create and quit button panel.
	/*-------------------------Controller-----------------------*/
	create.addActionListener(e -> createEventDialog());

	JButton quit = new JButton("Quit");
	quit.addActionListener(e -> {
	  model.saveEvents();
	  System.exit(0);
	});
	/*----------------------------------------------------------*/
	JPanel CQPanel = new JPanel();
	CQPanel.add(create);
	CQPanel.add(quit);

	//The main calendar frame
	calendarFrame.setLayout(new BorderLayout());

	calendarFrame.add(monthPanel, BorderLayout.WEST);
	calendarFrame.add(dayViewPanel, BorderLayout.CENTER);
	calendarFrame.add(CQPanel, BorderLayout.NORTH);

	calendarFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	calendarFrame.pack();
	calendarFrame.setVisible(true);
  }

  /*--------------------View--------------------------*/
  /**
   * Change the change listeners state
   * @param e the change event
   */
  @Override
  public void stateChanged(ChangeEvent e) {
    //If the month is changed
	if (model.isMonthChanged()) {
	  maxDays = model.getMaxDays();
	  dayBtns.clear();
	  monthGridPanel.removeAll();
	  monthLabel.setText(getMonthName(model.getCurrentMonth()) + " " + model.getCurrentYear());

	  dayButtonsSetUp();
	  highlightDayButton();
	  prevHighlight = -1;
	  model.resetMonthChanged();
	  calendarFrame.pack();
	  calendarFrame.repaint();
	}
	//If the month is not changed
	else {
	  showDate(model.getSelectedDay());
	  highlightSelectedDate(model.getSelectedDay() - 1);
	}
  }
  /*-----------------------------------------------------*/

  /**
   * Creates an event on the selected date through user input.
   */
  private void createEventDialog() {
	final JDialog eventDialog = new JDialog();
	eventDialog.setTitle("Create event");
	eventDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
	eventDialog.setLayout(new GridBagLayout());
	final JTextField eventText = new JTextField(30);
	final JTextField timeStart = new JTextField(8);
	final JTextField timeEnd = new JTextField(8);

	JButton save = new JButton("Save");
	save.addActionListener(e -> {
	  if (eventText.getText().isEmpty()) {
		return;
	  }
	  if ((!eventText.getText().isEmpty() && (timeStart.getText().isEmpty() || timeEnd.getText().isEmpty())) || timeStart.getText().length() != 5 || timeEnd.getText().length() != 5 || !timeStart.getText().matches("([01]?[0-9]|2[0-3]):[0-5][0-9]") || !timeEnd.getText().matches("([01]?[0-9]|2[0-3]):[0-5][0-9]")) {
		JDialog timeErrorDialog = new JDialog();
		timeErrorDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		timeErrorDialog.setLayout(new GridLayout(2, 0));
		timeErrorDialog.add(new JLabel("Please follow the time format XX:XX."));
		JButton ok = new JButton("OK");
		ok.addActionListener(e1 -> timeErrorDialog.dispose());
		timeErrorDialog.add(ok);
		timeErrorDialog.pack();
		timeErrorDialog.setVisible(true);
	  } else if (!eventText.getText().equals("")) {
		if (model.hasEventConflict(timeStart.getText(), timeEnd.getText())) {
		  JDialog conflictDialog = new JDialog();
		  conflictDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		  conflictDialog.setLayout(new GridLayout(2, 0));
		  conflictDialog.add(new JLabel("Time conflict.Please reenter the time."));
		  JButton ok = new JButton("OK");
		  ok.addActionListener(e12 -> conflictDialog.dispose());
		  conflictDialog.add(ok);
		  conflictDialog.pack();
		  conflictDialog.setVisible(true);
		} else {
		  eventDialog.dispose();
		  model.createEvent(eventText.getText(), timeStart.getText(), timeEnd.getText());
		  showDate(model.getSelectedDay());
		  highlightDayButton();
		}
	  }
	});

	JLabel date = new JLabel();
	date.setText(model.getCurrentMonth() + 1 + "/" + model.getSelectedDay() + "/" + model.getCurrentYear());
	date.setBorder(BorderFactory.createEmptyBorder());

	//Layout
	GridBagConstraints c = new GridBagConstraints();
	//For date label
	c.insets = new Insets(2, 2, 2, 2);
	c.gridx = 0;
	c.gridy = 0;
	eventDialog.add(date, c);
	//For event label
	c.gridy = 1;
	c.weightx = 1.0;
	c.anchor = GridBagConstraints.LINE_START;
	eventDialog.add(new JLabel("Event"), c);
	//For event text field
	c.gridy = 2;
	eventDialog.add(eventText, c);
	//For time start label
	c.gridy = 3;
	c.weightx = 0.0;
	c.anchor = GridBagConstraints.LINE_START;
	eventDialog.add(new JLabel("Time Start (00:00)"), c);
	//For time end label
	c.anchor = GridBagConstraints.CENTER;
	eventDialog.add(new JLabel("Time End (24:00)"), c);
	//For time start text field
	c.gridy = 4;
	c.anchor = GridBagConstraints.LINE_START;
	eventDialog.add(timeStart, c);
	//For time end text field
	c.anchor = GridBagConstraints.CENTER;
	eventDialog.add(timeEnd, c);
	//For save button
	c.anchor = GridBagConstraints.LINE_END;
	eventDialog.add(save, c);

	eventDialog.pack();
	eventDialog.setVisible(true);
  }

  /**
   * Shows the selected date and events on that date.
   * @param d the selected date
   */
  private void showDate(final int d) {
	model.setSelectedDate(d);
	String dayOfWeek = getDayOfWeekName(model.getDayOfWeek(d));
	String date = (model.getCurrentMonth() + 1) + "/" + d + "/" + model.getCurrentYear();
	String events = "";
	if (model.hasEvent(date)) {
	  events += model.getEvents(date);
	}
	dayEventPane.setText("                                                " + dayOfWeek + " " + date + "\n" + events);
	dayEventPane.setCaretPosition(0);
  }

  /**
   * Get the day of week name by integer
   * @param i the integer to be parsed
   * @return the week day name
   */
  private String getDayOfWeekName(int i) {
	String dayOfWeekName = null;
	switch (i) {
	  case 1:
		dayOfWeekName = "Sunday";
		break;
	  case 2:
		dayOfWeekName = "Monday";
		break;
	  case 3:
		dayOfWeekName = "Tuesday";
		break;
	  case 4:
		dayOfWeekName = "Wednesday";
		break;
	  case 5:
		dayOfWeekName = "Thursday";
		break;
	  case 6:
		dayOfWeekName = "Friday";
		break;
	  case 7:
		dayOfWeekName = "Saturday";
		break;
	}
	return dayOfWeekName;
  }

  /**
   * Get the month name of integer
   * @param i the integer to be parsed
   * @return the month name
   */
  private String getMonthName(int i) {
	String monthName = null;
	switch (i) {
	  case 0:
		monthName = "January";
		break;
	  case 1:
		monthName = "February";
		break;
	  case 2:
		monthName = "March";
		break;
	  case 3:
		monthName = "April";
		break;
	  case 4:
		monthName = "May";
		break;
	  case 5:
		monthName = "June";
		break;
	  case 6:
		monthName = "July";
		break;
	  case 7:
		monthName = "August";
		break;
	  case 8:
		monthName = "September";
		break;
	  case 9:
		monthName = "October";
		break;
	  case 10:
		monthName = "November";
		break;
	  case 11:
		monthName = "December";
	}
	return monthName;
  }

  /**
   * Highlights the currently selected date.
   * @param d the currently selected date
   */
  private void highlightSelectedDate(int d) {
	Border border = new LineBorder(Color.RED, 2);
	dayBtns.get(d).setBorder(border);
	if (prevHighlight != -1) {
	  dayBtns.get(prevHighlight).setBorder(new JButton().getBorder());
	}
	prevHighlight = d;
  }

  /**
   * Highlights days containing events.
   */
  private void highlightDayButton() {
	for (int i = 1; i <= maxDays; i++) {
	  if (model.hasEvent((model.getCurrentMonth() + 1) + "/" + i + "/" + model.getCurrentYear())) {
		dayBtns.get(i - 1).setBackground(Color.gray);
	  }
	}
  }

  /**
   * Creates buttons representing days of the current month and adds them to an array list.
   */
  private void createDayButtons() {
	for (int i = 1; i <= maxDays; i++) {
	  final int d = i;
	  JButton day = new JButton(Integer.toString(d));
	  day.setBackground(Color.WHITE);

	  day.addActionListener(arg0 -> {
		showDate(d);
		highlightSelectedDate(d - 1);
		create.setEnabled(true);
		next.setEnabled(true);
		previous.setEnabled(true);
	  });
	  dayBtns.add(day);
	}
  }

  /**
   * Adds the buttons representing the days of the month to the panel.
   */
  private void addDayButtons() {
	for (JButton d : dayBtns) {
	  monthGridPanel.add(d);
	}
  }

  /**
   * Adds filler buttons before the start of the month to align calendar.
   */
  private void addBlankButtons() {
	for (int j = 1; j < model.getDayOfWeek(1); j++) {
	  JButton b = new JButton();
	  b.setEnabled(false);
	  monthGridPanel.add(b);
	}
  }

  /**
   * Set day button up.
   */
  private void dayButtonsSetUp() {
	createDayButtons();
	addBlankButtons();
	addDayButtons();
  }
}
