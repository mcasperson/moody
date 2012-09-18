package com.mcasperson.moody;

public interface MessageRecieved {
    void onMessageRecieved(final String channel, final String sender, final String login, final String hostname, final String message);
}
