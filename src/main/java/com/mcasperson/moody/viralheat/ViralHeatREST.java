package com.mcasperson.moody.viralheat;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

/**
 * See https://www.viralheat.com/developer/sentiment_api
 * @author Matthew Casperson
 */
@Path("/api/sentiment/")
public interface ViralHeatREST {
    @Path("review.json")
    @GET
    Sentiment getSentiment(@QueryParam(value = "text") String text, @QueryParam(value = "api_key") String api_key);
}
