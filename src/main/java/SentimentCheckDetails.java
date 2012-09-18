import java.util.Calendar;

import javax.swing.JLabel;

public class SentimentCheckDetails {
    private final JLabel label;
    private Calendar lastCheck;
    
    public SentimentCheckDetails(final JLabel label)
    {
        lastCheck = Calendar.getInstance();
        this.label = label;
    }

    public JLabel getLabel() {
        return label;
    }

    public Calendar getLastCheck() {
        return lastCheck;
    }

    public void setLastCheck(Calendar lastCheck) {
        this.lastCheck = lastCheck;
    }
}
