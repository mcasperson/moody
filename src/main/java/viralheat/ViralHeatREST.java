package viralheat;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path("/api/sentiment/")
public interface ViralHeatREST {
    @Path("review.json")
    @GET
    Sentiment getSentiment(@QueryParam(value = "text") String text, @QueryParam(value = "api_key") String api_key);
}
