import java.util.Calendar;

public class Message {
    private final String from;
    private final String channel;
    private final String message;
    private final Calendar time;
    
    public Calendar getTime() {
        return time;
    }

    public String getMessage() {
        return message;
    }

    public String getFrom() {
        return from;
    }

    public Message(final String from, final String message, final String channel, final Calendar time)
    {
        this.from = from;
        this.message = message;
        this.time = time;
        this.channel = channel;
    }

    public String getChannel() {
        return channel;
    }
}
