import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DayView extends JComponent {
    private LocalDate displayDate;
    private Graphics2D g2;
    private DateTimeFormatter dateFormatter;
    protected static final LocalTime START_TIME = LocalTime.of(0, 0);
    protected static final LocalTime END_TIME = LocalTime.of(23, 59);
    private ZoneId defaultZoneId = ZoneId.systemDefault();
    private HashMap<LocalDate, ArrayList<CalendarEvent>> EventMap = new HashMap<>();
    private Point click_start;
    private Point drag_start;
    private Point drag_end;
    private CalendarEvent prevDragEvent;
    private Boolean drag_on_event = false;
    private Boolean click_on_event = false;
    private CalendarEvent event_with_mouse_clicked = null;
    private CalendarEvent event_with_mouse_dragged = null;

    private JToggleButton tag_vacation;
    private JToggleButton tag_family;
    private JToggleButton tag_school;
    private JToggleButton tag_work;

    int pix_per_hr = 50;
    int HEADER_HEIGHT = 50;
    int height = pix_per_hr * 24 + HEADER_HEIGHT;
    int eventWidth = 600;
    int event_start_x = 50;
    // pixel per minute
    double timeScale = (double) (height - HEADER_HEIGHT) / (END_TIME.getHour() *60 + END_TIME.getMinute()) - (START_TIME.getHour() *60 + START_TIME.getMinute());
    private static final Color DEFAULT_COLOR = Color.PINK;

    public DayView() {
        // in order to show the scroll bar, must set the preferred size, not the fixed size
        this.setPreferredSize(new Dimension(800, height)); // 50px * 24 hr + 50px for title
        this.setMinimumSize(new Dimension(800, height));
        this.setVisible(true);
        displayDate = LocalDate.now();
        setupEventListeners();
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        g2 = (Graphics2D) g;
        // Set background to white
        g2.setColor(Color.white);
        g2.drawRect(0, 0, 800, 1250);  //this.getWidth(), this.getHeight()
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Set paint colour to black
        g2.setColor(Color.black);
        drawDayHeadings();
        drawGrid();
        drawTimes();
        drawEvents();
        drawCurrentTimeLine();
    }

    private void drawDayHeadings() {
        dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
        String text = dateFormatter.format(displayDate);
        g2.drawString(text, 500, 20);
    }

    private double timeToPixel(LocalTime time) {
        double re = ((time.getHour() *60 + time.getMinute() - START_TIME.getHour()*60 - START_TIME.getMinute()) * timeScale) + HEADER_HEIGHT;
        return re;
    }

    private LocalTime pixelToTime(double y) {
        double number_min = ((y - HEADER_HEIGHT) / timeScale) + START_TIME.getHour()*60 + START_TIME.getMinute();
        return LocalTime.of((int) number_min/60, (int) number_min % 60, 0);
    }

    private void drawGrid() {
        // Draw horizontal grid lines
        int y = 0;
        g2.setColor(Color.gray);
        for (LocalTime time = START_TIME; time.getHour() < END_TIME.getHour(); time = time.plusHours(1)) {
            y = HEADER_HEIGHT + time.getHour()*pix_per_hr;
            g2.draw(new Line2D.Double(0, y, 0 + 2000, y)); // dayWidth = 800
        }
        y = y + HEADER_HEIGHT;
        g2.draw(new Line2D.Double(0, y, 0 + 2000, y)); // dayWidth = 800
        // Reset the graphics context's colour
        g2.setColor(Color.red);
    }

    private void drawTimes() {
        int y = 0;
        int FONT_LETTER_PIXEL_WIDTH = 2;
        LocalTime time = START_TIME;
        Font origFont = g2.getFont();
        float origFontSize = origFont.getSize();
        g2.setFont(new Font("Imprint MT Shadow", Font.BOLD, 12));
        for (time = START_TIME; time.getHour() < END_TIME.getHour(); time = time.plusHours(1)) {
            y = (int) timeToPixel(time);

            g2.drawString(time.toString(), (FONT_LETTER_PIXEL_WIDTH * time.toString().length()) - 5, y);
        }
        g2.setFont(origFont);
        y = y + HEADER_HEIGHT;
        g2.drawString(time.toString(), (FONT_LETTER_PIXEL_WIDTH * time.toString().length()) - 5, y);
    }

    private void drawCurrentTimeLine() {
        final double y = timeToPixel(LocalTime.now());

        final Color origColor = g2.getColor();
        final Stroke origStroke = g2.getStroke();

        g2.setColor(new Color(255, 127, 110));
        g2.setStroke(new BasicStroke(2));
        g2.draw(new Line2D.Double(0, y, 2000, y));

        g2.setColor(origColor);
        g2.setStroke(origStroke);
    }

    private void drawEvents() {
        double y0;
        if ((EventMap.containsKey(displayDate)) && EventMap.get(displayDate).size() > 0)  {
            for (CalendarEvent event : EventMap.get(displayDate)) {
                y0 = timeToPixel(event.getStart());
                double eventHeight = (timeToPixel(event.getEnd()) - timeToPixel(event.getStart()));
                RoundRectangle2D rect = new RoundRectangle2D.Double(event_start_x, y0, eventWidth, eventHeight, 20, 20);

                Color origColor = g2.getColor();
                g2.setColor(event.getColor());

                g2.fill(rect);
                g2.setColor(origColor);

                // Draw time header
                Font origFont = g2.getFont();
                final float fontSize = origFont.getSize() - 1.6F;
                Font newFont = origFont.deriveFont(Font.BOLD, fontSize);
                g2.setFont(newFont);
                g2.drawString(event.getStart() + " - " + event.getEnd(), (int) event_start_x + 5, (int) y0 + 11);
                g2.setFont(origFont.deriveFont(fontSize));
                g2.drawString(event.getText(), (int) event_start_x + 5, (int) y0 + 23);
                g2.setFont(origFont);
            }
        }
    }

    public void setCurrentDate(LocalDate newDate) {
        displayDate = newDate;
    }

    public LocalDate getCurrentDate() {
        return displayDate;
    }

    public void addEvent(LocalDate eventDate, CalendarEvent ce) {
        if (!(EventMap.containsKey(eventDate))) {
            ArrayList<CalendarEvent> dayEventList = new ArrayList<CalendarEvent>();
            EventMap.put(eventDate, dayEventList);
        }
        EventMap.get(eventDate).add(ce);

    }

    // listener for clicking on calendar
    private void setupEventListeners() {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent mc) {
                if(mc.getClickCount() == 2){
                    click_start = mc.getPoint();
                    click_on_event = (Boolean)mouseOnEvent(click_start).get(0);
                    if (click_on_event) {
                        event_with_mouse_clicked = (CalendarEvent)mouseOnEvent(click_start).get(1);
                        prevDragEvent = event_with_mouse_clicked;
                    }
                    checkCalendarEventClick(mc.getPoint());
                }
                click_on_event = false;
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                drag_start = e.getPoint();
                // is the cursor any event? this is checked every time the mouse is pressed
                drag_on_event = (Boolean)mouseOnEvent(drag_start).get(0);
                if (drag_on_event) {
                    event_with_mouse_dragged = (CalendarEvent)mouseOnEvent(drag_start).get(1);
                    prevDragEvent = event_with_mouse_dragged;
                }
                repaint();
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                drag_end = e.getPoint();
                int dy = drag_end.y - drag_start.y;
                checkCalendarEventDrag(drag_start.y, dy);
                repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                drag_start = null;
                drag_on_event = false;
                event_with_mouse_dragged = null;
                prevDragEvent = null;
                repaint();
            }
        });
    }

    private ArrayList<Object> mouseOnEvent(Point p) {
        double x0, x1, y0, y1;

        if (EventMap.containsKey(displayDate)) {
            for (CalendarEvent event : EventMap.get(displayDate)) {
                x0 = event_start_x;
                y0 = timeToPixel(event.getStart());
                x1 = event_start_x + eventWidth;
                y1 = timeToPixel(event.getEnd());
                if (p.getX() >= x0 && p.getX() <= x1 && p.getY() >= y0 && p.getY() <= y1) {
                    ArrayList<Object> results = new ArrayList <Object>();
                    results.add(true);
                    results.add(event);
                    return results;
                }
            }
        }
        ArrayList<Object> results = new ArrayList <Object>();
        results.add(false);
        return results;
    }

    private void checkCalendarEventClick(Point p) {
        ArrayList modify_info = null;

        if (click_on_event) {
            modify_info = modifyEventWithDialogBox(event_with_mouse_clicked);
            if (modify_info != null) {
                this.addEvent((LocalDate) modify_info.get(0), (CalendarEvent)modify_info.get(1));
                EventMap.get(displayDate).remove(event_with_mouse_clicked);
                this.repaint();
            }
        }
        else{ createEventWithClick(p.getY());}
    }

    private void checkCalendarEventDrag(int start_y, int dy) {
        CalendarEvent draggedEvent = null;

        if (drag_on_event) {
            draggedEvent = modifyEventWithDrag(event_with_mouse_dragged, dy);
            this.addEvent(displayDate, draggedEvent);
            EventMap.get(displayDate).remove(prevDragEvent);
            prevDragEvent = draggedEvent;
            this.repaint();
        }
        else{
            Color prev_color;
            if (prevDragEvent != null) {prev_color = prevDragEvent.getColor();}
            else {prev_color = generateRandomColor();}

            // delete prev event
            if (EventMap.containsKey(displayDate) && EventMap.get(displayDate).size()>0 && prevDragEvent != null) {
                EventMap.get(displayDate).remove(prevDragEvent);
            }
            prevDragEvent = createEventWithDrag(start_y, start_y+dy, prev_color);
        }
    }


    public ArrayList createEventWithDialogBox() {
        JPanel dialogPanel = new JPanel(new GridLayout(0, 1));
        dialogPanel.add(new JLabel("Name of event:"));
        JTextField eventTitle = new JTextField("New Event");
        dialogPanel.add(eventTitle);

        dialogPanel.add(new JLabel("Date"));
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        JFormattedTextField dateTextField = new JFormattedTextField(dateFormat);
        dateTextField.setName("Today");
        dateTextField.setColumns(10);
        dateTextField.setEditable(true);
        JLabel todayLabel = new JLabel("Date:");
        todayLabel.setLabelFor(dateTextField);

        // convert localdate to date
        Date displayDate_asDate = Date.from(displayDate.atStartOfDay(defaultZoneId).toInstant());
        dateTextField.setValue(displayDate_asDate);

        dialogPanel.add(dateTextField);

        // create spinners for start time
        JSpinner spinner1 = createSpinner("H", LocalTime.now().getHour());
        JSpinner spinner2 = createSpinner("m", LocalTime.now().getMinute());
        JPanel spinnerPanel1 = new JPanel(new GridLayout(1, 0));
        spinnerPanel1.add(spinner1);
        spinnerPanel1.add(new JLabel(":", SwingConstants.CENTER));
        spinnerPanel1.add(spinner2);

        // create spinners for end time
        JSpinner spinner3 = createSpinner("H", LocalTime.now().getHour());
        JSpinner spinner4 = createSpinner("m", LocalTime.now().getMinute());
        JPanel spinnerPanel2 = new JPanel(new GridLayout(1, 0));
        spinnerPanel2.add(spinner3);
        spinnerPanel2.add(new JLabel(":", SwingConstants.CENTER));
        spinnerPanel2.add(spinner4);

        dialogPanel.add(new JLabel("Start"));
        dialogPanel.add(spinnerPanel1);
        dialogPanel.add(new JLabel("End"));
        dialogPanel.add(spinnerPanel2);

        // add the 4 tags checkboxes
        JPanel tagPanel = new JPanel(new GridLayout(1, 0));
        tag_vacation = new JToggleButton("Vacation");
        tag_family = new JToggleButton("Family");
        tag_school = new JToggleButton("School");
        tag_work = new JToggleButton("Work");

        tag_vacation.setBackground(Color.decode("#e1f5fe"));
        tag_vacation.setFont(new Font("SansSerif", Font.ITALIC, 12));
        tag_family.setBackground(Color.decode("#ffebee"));
        tag_family.setFont(new Font("SansSerif", Font.ITALIC, 12));
        tag_school.setBackground(Color.decode("#e8f5e9"));
        tag_school.setFont(new Font("SansSerif", Font.ITALIC, 12));
        tag_work.setBackground(Color.decode("#fff3e0"));
        tag_work.setFont(new Font("SansSerif", Font.ITALIC, 12));

        tagPanel.add(tag_vacation);
        tagPanel.add(tag_family);
        tagPanel.add(tag_school);
        tagPanel.add(tag_work);

        dialogPanel.add(new JLabel("Choose your tags:"));
        dialogPanel.add(tagPanel);

        // add color chooser
        Color inputColor = JColorChooser.showDialog(dialogPanel, "Please choose an event color", DEFAULT_COLOR);

        int result = JOptionPane.showConfirmDialog(null, dialogPanel, "Appointment",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);


        ArrayList successful_create_event = new ArrayList();
        // if OK is pressed
        if (result == JOptionPane.OK_OPTION) {
            String title = eventTitle.getText();

            String eventDate_string = dateTextField.getText();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(((SimpleDateFormat) dateFormat).toPattern());
            LocalDate eventDate = LocalDate.parse(eventDate_string, dateTimeFormatter);

            // convert the four util.dates from four spinners to four local dates
            Calendar calendar_start_h = Calendar.getInstance();
            calendar_start_h.setTime((Date)spinner1.getValue());
            int start_hours = calendar_start_h.get(Calendar.HOUR_OF_DAY);

            Calendar calendar_start_m = Calendar.getInstance();
            calendar_start_m.setTime((Date)spinner2.getValue());
            int start_minutes = calendar_start_m.get(Calendar.MINUTE);

            Calendar calendar_end_h = Calendar.getInstance();
            calendar_end_h.setTime((Date)spinner3.getValue());
            int end_hours = calendar_end_h.get(Calendar.HOUR_OF_DAY);

            Calendar calendar_end_m = Calendar.getInstance();
            calendar_end_m.setTime((Date)spinner4.getValue());
            int end_minutes = calendar_end_m.get(Calendar.MINUTE);

            LocalTime start_time = LocalTime.of(start_hours, start_minutes, 0, 0);
            LocalTime end_time = LocalTime.of(end_hours, end_minutes, 0, 0);

            String start_hour = new SimpleDateFormat("HH").format(spinner1.getValue());
            String start_min = new SimpleDateFormat("mm").format(spinner2.getValue());
            String end_hour = new SimpleDateFormat("HH").format(spinner3.getValue());
            String end_min = new SimpleDateFormat("mm").format(spinner4.getValue());

            ArrayList success_info = new ArrayList();
            success_info.add(title);
            success_info.add(eventDate_string);
            success_info.add(eventDate);
            success_info.add(start_time);
            success_info.add(end_time);
            success_info.add(start_hour);
            success_info.add(start_min);
            success_info.add(end_hour);
            success_info.add(end_min);
            success_info.add(inputColor);

            successful_create_event.add(true);
            successful_create_event.add(success_info);
            return successful_create_event;
        } else {
            successful_create_event.add(false);
            successful_create_event.add(new ArrayList());
            return successful_create_event;
        }
    }


    public ArrayList modifyEventWithDialogBox(CalendarEvent clicked_event) {
        JPanel dialogPanel = new JPanel(new GridLayout(0, 1));
        dialogPanel.add(new JLabel("Name of event:"));
        JTextField eventTitle = new JTextField(clicked_event.getText());
        dialogPanel.add(eventTitle);

        dialogPanel.add(new JLabel("Date"));
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        JFormattedTextField dateTextField = new JFormattedTextField(dateFormat);
        dateTextField.setName("Today");
        dateTextField.setColumns(10);
        dateTextField.setEditable(true);
        JLabel todayLabel = new JLabel("Date:");
        todayLabel.setLabelFor(dateTextField);

        // convert localdate to date
        Date displayDate_asDate = Date.from(clicked_event.getDate().atStartOfDay(defaultZoneId).toInstant());
        dateTextField.setValue(displayDate_asDate);
        dialogPanel.add(dateTextField);

        // create spinners for start time
        JSpinner spinner1 = createSpinner("H", clicked_event.getStart().getHour());
        JSpinner spinner2 = createSpinner("m", clicked_event.getStart().getMinute());
        JPanel spinnerPanel1 = new JPanel(new GridLayout(1, 0));
        spinnerPanel1.add(spinner1);
        spinnerPanel1.add(new JLabel(":", SwingConstants.CENTER));
        spinnerPanel1.add(spinner2);


        // create spinners for end time
        JSpinner spinner3 = createSpinner("H", clicked_event.getEnd().getHour());
        JSpinner spinner4 = createSpinner("m", clicked_event.getEnd().getMinute());
        JPanel spinnerPanel2 = new JPanel(new GridLayout(1, 0));
        spinnerPanel2.add(spinner3);
        spinnerPanel2.add(new JLabel(":", SwingConstants.CENTER));
        spinnerPanel2.add(spinner4);


        dialogPanel.add(new JLabel("Start"));
        dialogPanel.add(spinnerPanel1);
        dialogPanel.add(new JLabel("End"));
        dialogPanel.add(spinnerPanel2);

        // add the 4 tags checkboxes
        JPanel tagPanel = new JPanel(new GridLayout(1, 0));
        tag_vacation = new JToggleButton("Vacation");
        tag_family = new JToggleButton("Family");
        tag_school = new JToggleButton("School");
        tag_work = new JToggleButton("Work");

        tag_vacation.setBackground(Color.decode("#e1f5fe"));
        tag_vacation.setFont(new Font("SansSerif", Font.ITALIC, 12));
        tag_family.setBackground(Color.decode("#ffebee"));
        tag_family.setFont(new Font("SansSerif", Font.ITALIC, 12));
        tag_school.setBackground(Color.decode("#e8f5e9"));
        tag_school.setFont(new Font("SansSerif", Font.ITALIC, 12));
        tag_work.setBackground(Color.decode("#fff3e0"));
        tag_work.setFont(new Font("SansSerif", Font.ITALIC, 12));

        tagPanel.add(tag_vacation);
        tagPanel.add(tag_family);
        tagPanel.add(tag_school);
        tagPanel.add(tag_work);

        dialogPanel.add(new JLabel("Choose your tags:"));
        dialogPanel.add(tagPanel);

        // add color chooser
        Color inputColor = JColorChooser.showDialog(dialogPanel, "Please choose an event color", DEFAULT_COLOR);

        int result = JOptionPane.showConfirmDialog(null, dialogPanel, "Appointment",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        ArrayList successful_create_event = new ArrayList();
        // if OK is pressed
        if (result == JOptionPane.OK_OPTION) {

            // convert the four util.dates from four spinners to four local dates
            Calendar calendar_start_h = Calendar.getInstance();
            calendar_start_h.setTime((Date)spinner1.getValue());
            int start_hours = calendar_start_h.get(Calendar.HOUR_OF_DAY);

            Calendar calendar_start_m = Calendar.getInstance();
            calendar_start_m.setTime((Date)spinner2.getValue());
            int start_minutes = calendar_start_m.get(Calendar.MINUTE);

            Calendar calendar_end_h = Calendar.getInstance();
            calendar_end_h.setTime((Date)spinner3.getValue());
            int end_hours = calendar_end_h.get(Calendar.HOUR_OF_DAY);

            Calendar calendar_end_m = Calendar.getInstance();
            calendar_end_m.setTime((Date)spinner4.getValue());
            int end_minutes = calendar_end_m.get(Calendar.MINUTE);

            LocalTime start_time = LocalTime.of(start_hours, start_minutes, 0, 0);
            LocalTime end_time = LocalTime.of(end_hours, end_minutes, 0, 0);

            CalendarEvent newEvent = new CalendarEvent(clicked_event);

            newEvent.setStart(start_time);
            newEvent.setEnd(end_time);

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(((SimpleDateFormat) dateFormat).toPattern());
            LocalDate newDate = LocalDate.parse(dateTextField.getText(), dateTimeFormatter);
            newEvent.setDate(newDate);
            newEvent.setText(eventTitle.getText());
            newEvent.setColor(inputColor);

            // collect modify info
            ArrayList modify_info = new ArrayList();
            modify_info.add(newDate);
            modify_info.add(newEvent);
            return modify_info;

        }
        return null;
    }

    public CalendarEvent modifyEventWithDrag(CalendarEvent clicked_event, int dy) {
        CalendarEvent draggedEvent = new CalendarEvent(clicked_event);
        draggedEvent.setStart(clicked_event.getStart().plusMinutes((long) (dy/timeScale)));
        draggedEvent.setEnd(clicked_event.getEnd().plusMinutes((long) (dy/timeScale)));
        return draggedEvent;
    }

    public void createEventWithClick(Double y_start) {
        LocalTime start_time = pixelToTime(y_start);
        if (!(EventMap.containsKey(displayDate))) {
            ArrayList<CalendarEvent> dayEventList = new ArrayList<CalendarEvent>();
            EventMap.put(displayDate, dayEventList);
        }
        CalendarEvent dummy_event = new CalendarEvent(displayDate, start_time, start_time.plusHours(1), "New Event", generateRandomColor()); // Color.red
        this.addEvent(displayDate, dummy_event);
        this.repaint();
    }

    public CalendarEvent createEventWithDrag(int start_y, int end_y, Color prev_color) {
        if (!(EventMap.containsKey(displayDate))) {
            ArrayList<CalendarEvent> dayEventList = new ArrayList<CalendarEvent>();
            EventMap.put(displayDate, dayEventList);
        }
        CalendarEvent updated_drag_event = new CalendarEvent(displayDate, pixelToTime(start_y), pixelToTime(end_y), "New Event", prev_color); // Color.red
        this.addEvent(displayDate, updated_drag_event);
        this.repaint();

        return updated_drag_event;
    }

    /*
    A method to create spinners for start/end time
    */
    public JSpinner createSpinner(String time_unit, Integer val) {
        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(model);
        Calendar cal = Calendar.getInstance();
        if (time_unit == "H") {
            cal.set(Calendar.HOUR_OF_DAY, val);
            Date date_for_H = cal.getTime();
            spinner.setValue(date_for_H);
            JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner,"HH");
            spinner.setEditor(editor);}
        else {
            cal.set(Calendar.MINUTE, val);
            Date date_for_M = cal.getTime();
            spinner.setValue(date_for_M);
            JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner,"mm");
            spinner.setEditor(editor);}

        return spinner;
    }

    public Color generateRandomColor() {
        Random rand = new Random();
        // Java 'Color' class takes 3 floats, from 0 to 1.
        float r = (float) (rand.nextFloat() * 0.5); // avoid the color being same as the text color
        float g = rand.nextFloat();
        float b = rand.nextFloat();
        float a = (float) 0.7;
        Color randomColor = new Color(r, g, b, a);

        return randomColor;
    }
}
