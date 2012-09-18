import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import viralheat.Sentiment;
import viralheat.ViralHeatREST;

public class Main implements ActionListener {

    private final ViralHeatREST client;
    private final IRCBot bot;
    private final Map<String, JLabel> channels = new HashMap<String, JLabel>();
    private final String apiKey;

    public static void main(final String[] args) {

        if (args.length < 2)
            return;

        final StringBuilder apiKey = new StringBuilder();
        final List<String> argsList = new ArrayList<String>();

        for (final String arg : args) {
            if (apiKey.length() == 0)
                apiKey.append(arg);
            else
                argsList.add(arg);
        }

        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Main(apiKey.toString(), argsList);
            }
        });
    }

    public Main(final String apiKey, final List<String> channels) {
        RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
        client = ProxyFactory.create(ViralHeatREST.class, Constants.URL);

        bot = new IRCBot(Constants.IRC_SERVER, channels, null);

        this.apiKey = apiKey;

        createAndShowGUI();
    }

    private void createAndShowGUI() {
        // Create and set up the window.
        final JFrame frame = new JFrame("Moody");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        for (final String channel : bot.getChannels()) {
            final JLabel label = new JLabel(channel);
            frame.getContentPane().add(label);

            channels.put(channel, label);
        }

        // Display the window.
        frame.pack();
        frame.setVisible(true);

        /* Setup a timer to update the UI */
        new Timer(Constants.FREQUENCY, this);
    }

    public void actionPerformed(ActionEvent arg0) {
        /* sort the messages by time received */
        Collections.sort(bot.getMessages(), new MessageTimeComparator());
        /* Now make the latest ones (i.e. those with the largest times) be listed first */
        Collections.reverse(bot.getMessages());

        for (final String channel : bot.getChannels()) {
            if (channels.containsKey(channel)) {
                /* Used to keep a track of the moods of the processed messages */
                final Map<String, Integer> sentimentCount = new HashMap<String, Integer>();

                /* to keep a track of how many messages are processed */
                int count = 0;

                /* Check the sentiment of as many messages as possible */
                for (final Message messageDetails : bot.getMessages()) {

                    /* only check those messages that were sent during the last frequency period */
                    final boolean messageInFrequencyWindow = Calendar.getInstance().getTimeInMillis()
                            - messageDetails.getTime().getTimeInMillis() <= Constants.FREQUENCY;

                    /*
                     * because the list is sorted, once we find out outside the frequency period, all the remaining ones will be
                     * too
                     */
                    if (!messageInFrequencyWindow)
                        break;

                    if (messageDetails.getChannel().equals(channel)) {

                        /* have we already processed this message? */
                        final String mood = messageDetails.getMood();

                        /* if not, call the API */
                        if (mood == null) {
                            try {
                                /* keep a track of how many calls we are making */
                                ++count;

                                /* Make sure we abide by the message limits */
                                final int end = messageDetails.getMessage().length() > Constants.VIRAL_HEAT_MAX_MESSAGE_LENGTH ? Constants.VIRAL_HEAT_MAX_MESSAGE_LENGTH
                                        : messageDetails.getMessage().length();
                                final String trimmedMessage = messageDetails.getMessage().substring(0, end);

                                /* Use RESTEasy client to call Viral Heat */
                                final Sentiment sentiment = client.getSentiment(trimmedMessage, apiKey);

                                /* Save the mood */
                                messageDetails.setMood(sentiment.getMood());

                            } catch (final Exception ex) {
                                ex.printStackTrace();
                            }
                        }

                        if (messageDetails.getMood() != null) {
                            /* Note the mood of the message */
                            if (!sentimentCount.containsKey(messageDetails.getMood())) {
                                sentimentCount.put(messageDetails.getMood(), 1);
                            } else {
                                sentimentCount.put(messageDetails.getMood(), sentimentCount.get(messageDetails.getMood() + 1));
                            }
                        }
                    }

                    /* Don't blow the API limit */
                    if (count >= Constants.MAX_CHECKS_PER_UPDATE / bot.getChannels().length)
                        break;
                }

                /* Find the mood that was most prevalent */
                int maxValue = -1;
                String maxMood = null;
                for (final String mood : sentimentCount.keySet()) {
                    if (maxValue == -1 || sentimentCount.get(mood) > maxValue) {
                        maxValue = sentimentCount.get(mood);
                        maxMood = mood;
                    }
                }

                /* Update the UI */
                if (maxMood.equals(Constants.NEGATIVE_MOOD))
                    channels.get(channel).setForeground(Color.RED);
                else if (maxMood.equals(Constants.POSITIVE_MOOD))
                    channels.get(channel).setForeground(Color.GREEN);
                else
                    channels.get(channel).setForeground(Color.YELLOW);
            }

        }
    }
}
