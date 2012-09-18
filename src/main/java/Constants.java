
public class Constants {
    public static final String URL = "http://www.viralheat.com";
    public static final String IRC_NAME = "moody";
    public static final String IRC_SERVER = "irc.bne.redhat.com";
    public static final int FREQUENCY = 1000 * 60 * 5;
    public static final int MAX_CHECKS = 5000;
    public static final int WORKING_HOURS_PER_DAY = 8;
    public static final int MAX_CHECKS_PER_UPDATE = FREQUENCY / MAX_CHECKS / WORKING_HOURS_PER_DAY / 60 / 60 / 1000;  
}   
