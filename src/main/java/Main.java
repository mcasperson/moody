import java.awt.Color;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import viralheat.Sentiment;
import viralheat.ViralHeatREST;

public class Main implements MessageRecieved {

    private final ViralHeatREST client;
    private final IRCBot bot;
    private final Map<String, SentimentCheckDetails> channels = new HashMap<String, SentimentCheckDetails>();
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

        bot = new IRCBot(Constants.IRC_SERVER, channels, this);

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

            channels.put(channel, new SentimentCheckDetails(label));
        }

        // Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public void onMessageRecieved(final String channel, final String sender, final String login, final String hostname,
            final String message) {

        /* Update the sentiment every couple of minutes */
        if (channels.containsKey(channel)
                && (Calendar.getInstance().getTimeInMillis() - channels.get(channel).getLastCheck().getTimeInMillis() >= Constants.FREQUENCY)) {

            final Map<String, Integer> sentimentCount = new HashMap<String, Integer>();

            int count = 0;
            
            /* sort the messages by time received */
            Collections.sort(bot.getMessages(), new MessageTimeComparator());
            /* Now make the latest ones (i.e. those with the largest times) be listed first */
            Collections.reverse(bot.getMessages());
            
            /* Check the sentiment of as many messages as possible */
            for (final Message messageDetails : bot.getMessages()) {
                if (messageDetails.getChannel().equals(channel)) {
                    try {
                        final int end = messageDetails.getMessage().length() > 360 ? 360 : messageDetails.getMessage().length();
                        final String trimmedMessage = messageDetails.getMessage().substring(0, end);
                        final Sentiment sentiment = client.getSentiment(trimmedMessage, apiKey);

                        if (!sentimentCount.containsKey(sentiment.getMood())) {
                            sentimentCount.put(sentiment.getMood(), 1);
                        } else {
                            sentimentCount.put(sentiment.getMood(), sentimentCount.get(sentiment.getMood() + 1));
                        }
                    } catch (final Exception ex) {
                        ex.printStackTrace();
                    }
                }

                ++count;

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
            if (maxMood.equals("negative"))
                channels.get(channel).getLabel().setForeground(Color.RED);
            else if (maxMood.equals("positive"))
                channels.get(channel).getLabel().setForeground(Color.GREEN);
            else
                channels.get(channel).getLabel().setForeground(Color.YELLOW);
        }
    }
}
