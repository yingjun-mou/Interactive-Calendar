import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;


public class CalendarEvent {

    private static final Color DEFAULT_COLOR = Color.PINK;

    private LocalDate date;
    private LocalTime start;
    private LocalTime end;
    private String text;
    private Color color;

    public CalendarEvent(LocalDate date, LocalTime start, LocalTime end, String text, Color color) {
        this.date = date;
        this.start = start;
        this.end = end;
        this.text = text;
//        this.color = DEFAULT_COLOR;
        this.color = color;
    }

    public CalendarEvent(CalendarEvent ce) {
        this.date = ce.getDate();
        this.start = ce.getStart();
        this.end = ce.getEnd();
        this.text = ce.getText();
//        this.color = DEFAULT_COLOR;
        this.color = ce.getColor();
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStart() {
        return start;
    }

    public void setStart(LocalTime start) {
        this.start = start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public void setEnd(LocalTime end) {
        this.end = end;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String toString() {
        return getDate() + " " + getStart() + "-" + getEnd() + ". " + getText();
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color input_color) {
         color = input_color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CalendarEvent that = (CalendarEvent) o;

        if (!date.equals(that.date)) return false;
        if (!start.equals(that.start)) return false;
        return end.equals(that.end);

    }

    @Override
    public int hashCode() {
        int result = date.hashCode();
        result = 31 * result + start.hashCode();
        result = 31 * result + end.hashCode();
        return result;
    }

}