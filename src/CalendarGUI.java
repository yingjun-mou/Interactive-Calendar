import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;


public class CalendarGUI extends JFrame {
    private JPanel mainPanel;
    private JLabel statusBar;
    private JLabel contentText;
    private JPanel controlPanel;
    private JPanel contentPanel;
    private JButton buttonToday;
    private JButton buttonAppointment;
    private JButton buttonPrev;
    private JButton buttonNext;
    private JMenuBar mb;
    private JMenu menu1;
    private JMenu menu2;
    private JRadioButtonMenuItem m2a, m2b;
    private LocalDate displayDate;
    private DateTimeFormatter dateFormatter;
    private boolean isDayview;
    private static final Color DEFAULT_COLOR = Color.PINK;

    //   ********** Below IS FOR HW2 *************
    private DayView DAYVIEW;
    //   ********** Above IS FOR HW2 *************

    /*
    Constructor of the Calendar GUI
    */
    public CalendarGUI(String title) {
        super(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainPanel = new JPanel();
        this.setContentPane(mainPanel);
        this.pack();

        //   ********** THIS IS FOR HW1 *************
        // JTextArea textArea = new JTextArea(5, 30);
        //   ********** Above IS FOR HW1 *************


        //   ********** THIS IS FOR HW2 *************
        DAYVIEW = new DayView();
        DAYVIEW.setCurrentDate(displayDate);
        //   ********** Above IS FOR HW2 *************


        BorderLayout layout = new BorderLayout();
        contentPanel = new JPanel();
        controlPanel = new JPanel();
        contentText = new JLabel("Welcome");
        contentText.setFont(new Font("Verdana", Font.PLAIN, 36));
        statusBar = new JLabel("status bar");


        mainPanel.setLayout(layout);
        mainPanel.add(contentPanel,BorderLayout.CENTER);
        mainPanel.add(controlPanel,BorderLayout.WEST);
        mainPanel.add(statusBar,BorderLayout.SOUTH);

        // PART 1. TOP MENU
        createMenu();
        // PART 2. LEFT CONTROL PANEL
        createConotrol();
        // PART 3. BOTTOM STATUS BAR
        statusBar.setHorizontalAlignment(JLabel.CENTER);
        // PART 4. RIGHT CONTENT PANEL
        isDayview = true;
        // in order to center the lable in the panel, the panel must have a grid layout
        contentPanel.setLayout(new GridBagLayout());
        resetToday();

    }


    /*
    A method to overwrite the content text depending on the view mode and the date
    */
    public void updateCalendar() {
        if (isDayview) {
            dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
            contentText.setText("<html> Day View: <br>" + dateFormatter.format(displayDate));
        }
        else {
            dateFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
            contentText.setText("<html> Month View: <br>" + dateFormatter.format(displayDate));
        }
        // contentPanel.add(contentText);  ********** THIS IS FOR HW1 *************
    }


    /*
    Reset the date back to today
    */
    public void resetToday() {
        displayDate = LocalDate.now();
        DAYVIEW.setCurrentDate(displayDate);
        updateCalendar();
    }


    /*
    A method taking care of PART 1. TOP MENU
    */
    public void createMenu() {
        mb = new JMenuBar();
        menu1 = new JMenu("File");
        JMenuItem m1a = new JMenuItem("Exit");
        m1a.addActionListener(new exitApp());
        menu1.add(m1a);

        menu2 = new JMenu("View");
        m2a = new JRadioButtonMenuItem("Day View");
        m2b = new JRadioButtonMenuItem("Month View");
        m2a.setSelected(true);
        m2b.setSelected(false);
        ButtonGroup bg = new ButtonGroup();
        bg.add(m2a);
        bg.add(m2b);
        menu2.add(m2a);
        menu2.add(m2b);

        mb.add(menu1);
        mb.add(menu2);
        this.setJMenuBar(mb);

        m2a.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statusBar.setText("Day View Selected");
                isDayview = true;
                updateCalendar();
            }
        });

        m2b.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statusBar.setText("Month View Selected");
                isDayview = false;
                updateCalendar();
            }
        });
    }


    /*
    A method taking care of PART 2. LEFT CONTROL PANEL
    */
    public void createConotrol() {
        controlPanel.setLayout(new GridLayout(4, 1));
        buttonToday = new JButton("Today");
        buttonAppointment = new JButton("Appointment");
        buttonPrev = new JButton("Previous");
        buttonNext = new JButton("Next");

        controlPanel.add(buttonToday);
        controlPanel.add(buttonAppointment);
        controlPanel.add(buttonPrev);
        controlPanel.add(buttonNext);

        // PART 4. BUTTON LISTENER
        buttonAppointment.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                statusBar.setText("Appointment Button Selected");
                ArrayList new_event_result = DAYVIEW.createEventWithDialogBox();
                if ((Boolean)new_event_result.get(0)) {
                    ArrayList success_info = (ArrayList)new_event_result.get(1);
                    String title = (String) success_info.get(0);
                    String eventDate_string = (String) success_info.get(1);
                    LocalDate eventDate = (LocalDate) success_info.get(2);
                    LocalTime start_time = (LocalTime) success_info.get(3);
                    LocalTime end_time = (LocalTime) success_info.get(4);
                    String start_hour = (String) success_info.get(5);
                    String start_min = (String) success_info.get(6);
                    String end_hour = (String) success_info.get(7);
                    String end_min = (String) success_info.get(8);
                    Color input_color = (Color) success_info.get(9);

                    statusBar.setText("New event created: " + title + " " + eventDate_string + " " + start_hour + ":" + start_min + "-" + end_hour + ":" + end_min);
                    DAYVIEW.addEvent(eventDate, new CalendarEvent(eventDate, start_time, end_time, title, input_color));
                    DAYVIEW.repaint();
                }
                else {statusBar.setText("Create event dialog box cancelled");}
            }
        });


        buttonToday.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statusBar.setText("Today Button Selected");
                resetToday();
                updateCalendar();
                DAYVIEW.repaint();
            }
        });

        buttonPrev.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statusBar.setText("Previous Button Selected");
                if (isDayview) { displayDate = displayDate.minusDays(1);}
                else { displayDate = displayDate.minusMonths(1);}
                updateCalendar();
                DAYVIEW.setCurrentDate(displayDate);
                DAYVIEW.repaint();
            }
        });

        buttonNext.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statusBar.setText("Next Button Selected");
                if (isDayview) { displayDate = displayDate.plusDays(1);}
                else { displayDate = displayDate.plusMonths(1);}
                updateCalendar();
                DAYVIEW.setCurrentDate(displayDate);
                DAYVIEW.repaint();
            }
        });


        // JFrame listener for resizing
        contentPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {

                int button_min = Math.min(buttonPrev.getWidth(), buttonPrev.getHeight());
                int offset = buttonPrev.getInsets().left;

                ImageIcon todayIcon = new ImageIcon();
                try {
                    URL url = new URL(String.valueOf(CalendarGUI.class.getResource("/today.png").toURI()));
                    Image image = ImageIO.read(url);
                    todayIcon = new ImageIcon(image);
                    buttonToday.setIcon(todayIcon);
                    buttonToday.setHorizontalTextPosition(AbstractButton.LEADING);
                } catch (URISyntaxException | MalformedURLException f) {
                    f.printStackTrace();
                } catch (IOException f) {
                    f.printStackTrace();
                }
                ImageIcon appIcon = new ImageIcon();
                try {
                    URL url = new URL(String.valueOf(CalendarGUI.class.getResource("/appointment.png").toURI()));
                    Image image = ImageIO.read(url);
                    appIcon = new ImageIcon(image);
                    buttonAppointment.setIcon(appIcon);
                    buttonAppointment.setHorizontalTextPosition(AbstractButton.LEADING);
                } catch (URISyntaxException | MalformedURLException f) {
                    f.printStackTrace();
                } catch (IOException f) {
                    f.printStackTrace();
                }
                ImageIcon prevIcon = new ImageIcon();
                try {
                    URL url = new URL(String.valueOf(CalendarGUI.class.getResource("/prev.png").toURI()));
                    Image image = ImageIO.read(url);
                    prevIcon = new ImageIcon(image);
                    buttonPrev.setIcon(prevIcon);
                    buttonPrev.setHorizontalTextPosition(AbstractButton.LEADING);
                } catch (URISyntaxException | MalformedURLException f) {
                    f.printStackTrace();
                } catch (IOException f) {
                    f.printStackTrace();
                }
                ImageIcon nextIcon = new ImageIcon();
                try {
                    URL url = new URL(String.valueOf(CalendarGUI.class.getResource("/next.png").toURI()));
                    Image image = ImageIO.read(url);
                    nextIcon = new ImageIcon(image);
                    buttonNext.setIcon(nextIcon);
                    buttonNext.setHorizontalTextPosition(AbstractButton.LEADING);
                } catch (URISyntaxException | MalformedURLException f) {
                    f.printStackTrace();
                } catch (IOException f) {
                    f.printStackTrace();
                }

                buttonToday.setIcon(resizeIcon(todayIcon, (button_min - offset)/2, (button_min - offset)/2));
                buttonAppointment.setIcon(resizeIcon(appIcon, (button_min - offset)/2, (button_min - offset)/2));
                buttonPrev.setIcon(resizeIcon(prevIcon, (button_min - offset)/2, (button_min - offset)/2));
                buttonNext.setIcon(resizeIcon(nextIcon, (button_min - offset)/2, (button_min - offset)/2));
            }
        });
    }


    /*
    A method to resize the button icon with the frame
    */
    private static Icon resizeIcon(ImageIcon icon, int resizedWidth, int resizedHeight) {
        Image img = icon.getImage();
        Image resizedImage = img.getScaledInstance(resizedWidth, resizedHeight,  java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }


    /*
    A helper function to implement the exit from the menu
    */
    static class exitApp implements ActionListener {
        public void actionPerformed(ActionEvent e)
        {System.exit(0);}
    }


    /*
    ========================================
    Main driver function
    ========================================
    */
    public static void main(String[] args) {
        JFrame frame = new CalendarGUI("My Calendar GUI");

        //   ********** Below IS FOR HW2 *************
        ((CalendarGUI) frame).DAYVIEW = new DayView();
        JScrollPane contentSP = new JScrollPane(((CalendarGUI) frame).DAYVIEW, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED); // DAYVIEW
        // in order to show the scroll bar, set the fixed size, not the preferred size
        contentSP.setSize(new Dimension(800, 600));
        frame.getContentPane().add(contentSP);
        //   ********** Above IS FOR HW2 *************

        frame.setSize(800, 600);
        frame.setMinimumSize(new Dimension(600, 400));
        frame.setVisible(true);
    }

}
