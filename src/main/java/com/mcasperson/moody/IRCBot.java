package com.mcasperson.moody;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.jibble.pircbot.PircBot;

public class IRCBot extends PircBot {

    private final int MAX_MESSAGES = 10000;
    private final List<Message> messages = new ArrayList<Message>();
    private final MessageRecieved listener;

    public List<Message> getMessages() {
        return messages;
    }

    public IRCBot(final String server, final List<String> channels, final MessageRecieved listener) {
        this.listener = listener;
        //this.setVerbose(true);

        try {
            this.setName(Constants.IRC_NAME);
            this.connect(server);

            for (final String channel : channels) {
                this.joinChannel(channel);
                Thread.sleep(Constants.JOIN_WAIT_TIME);
            }

        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onMessage(final String channel, final String sender, final String login, final String hostname,
            final String message) {
        messages.add(new Message(sender, message, channel, Calendar.getInstance()));
        while (messages.size() > MAX_MESSAGES)
            messages.remove(0);

        if (listener != null)
            listener.onMessageRecieved(channel, sender, login, hostname, message);
    }

}
