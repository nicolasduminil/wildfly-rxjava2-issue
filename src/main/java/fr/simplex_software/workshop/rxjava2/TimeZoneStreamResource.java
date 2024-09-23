package fr.simplex_software.workshop.rxjava2;

import io.reactivex.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jboss.resteasy.annotations.*;

import java.time.*;
import java.time.format.*;
import java.util.*;

@Path("tz")
public class TimeZoneStreamResource
{
  private static final String FMT = "d MMM uuuu, HH:mm:ss";
  private static final List<String> locations = new ArrayList<>(List.of(TimeZone.getAvailableIDs()));

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Stream
  public Flowable<String> getTimeZoneStream()
  {
    return Flowable.create(flowableEmitter ->
    {
      locations.forEach(location -> flowableEmitter.onNext(location));
      flowableEmitter.onComplete();
    }, BackpressureStrategy.BUFFER);
  }
}
