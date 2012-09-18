import java.util.Comparator;

public class MessageTimeComparator implements Comparator<Message> {

    public int compare(final Message arg0, final Message arg1) {
        if (arg0 == null && arg1 == null)
            return 0;
        if (arg0 == null)
            return 1;
        if (arg1 == null)
            return -1;
        return arg0.getTime().compareTo(arg1.getTime());
    }

}
