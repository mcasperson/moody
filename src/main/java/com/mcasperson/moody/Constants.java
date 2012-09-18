package com.mcasperson.moody;

public class Constants {
    public static final String URL = "http://www.viralheat.com";
    public static final String IRC_NAME = "moody";
    public static final int FREQUENCY = 1000 * 60 * 5;
    public static final int MAX_CHECKS = 5000;
    public static final int WORKING_HOURS_PER_DAY = 8;
    public static final int MAX_CHECKS_PER_UPDATE = FREQUENCY / MAX_CHECKS / WORKING_HOURS_PER_DAY / 60 / 60 / 1000;  
    public static final int VIRAL_HEAT_MAX_MESSAGE_LENGTH = 360;
    public static final String POSITIVE_MOOD = "positive";
    public static final String NEGATIVE_MOOD = "negative";
    public static final int JOIN_WAIT_TIME = 1000;
}   
