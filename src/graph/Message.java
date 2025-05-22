package graph;

import java.util.Date;

public class Message {
    public final byte[] data;
    public final String asText;
    public final double asDouble;
    public final Date date;

    // string constructor, used for all constructors
    public Message(String text) {
        this.asText = text;
        this.data = text.getBytes();
        double parsedValue;
        try {
            parsedValue = Double.parseDouble(text);
        } catch (NumberFormatException e) {
            parsedValue = Double.NaN;
        }
        this.asDouble = parsedValue;
        this.date = new Date();
    }

    // double constructor
    public Message(double value) {
        this(String.valueOf(value));
    }

    // bytes constructor
    public Message(byte[] bytes) {
        this(new String(bytes));
    }
}