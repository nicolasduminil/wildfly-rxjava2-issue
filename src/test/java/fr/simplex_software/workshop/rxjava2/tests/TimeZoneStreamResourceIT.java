package fr.simplex_software.workshop.rxjava2.tests;

import io.reactivex.*;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.sse.*;
import org.jboss.resteasy.rxjava2.*;
import org.junit.jupiter.api.*;
import org.slf4j.*;
import org.testcontainers.containers.*;
import org.testcontainers.containers.output.*;
import org.testcontainers.containers.wait.strategy.*;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.*;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
public class TimeZoneStreamResourceIT
{
  private static final Logger LOG = LoggerFactory.getLogger(TimeZoneStreamResourceIT.class);
  private static final String FMT = "d MMM uuuu, HH:mm:ss";
  private static URI timeUri;

  @Container
  private static final GenericContainer<?> wildfly =
    new GenericContainer<>("wildfly-bootable/wildfly-rxjava2-issue:local")
      .withExposedPorts(8080)
      .withNetwork(Network.newNetwork())
      .withNetworkAliases("wildfly-network-alias")
      .withLogConsumer(new Slf4jLogConsumer(LOG))
      .withEnv("TZ", "Europe/Paris")
      .waitingFor(Wait.forLogMessage(".*WFLYSRV0025.*", 1));

  static
  {
    wildfly.start();
  }

  @BeforeAll
  public static void beforeAll()
  {
    timeUri = UriBuilder.fromUri("http://" + wildfly.getHost())
      .port(wildfly.getMappedPort(8080)).path("tz").build();
  }

  @AfterAll
  public static void after()
  {
    timeUri = null;
  }

  @Test
  public void testTimeZoneStreamResource2() throws InterruptedException
  {
    CountDownLatch latch = new CountDownLatch(1);
    try (Client client = ClientBuilder.newClient().register(FlowableRxInvokerProvider.class))
    {
      FlowableRxInvoker invoker = client.target(timeUri).request().rx(FlowableRxInvoker.class);
      assertThat(invoker).isNotNull();
      @SuppressWarnings("unchecked")
      Flowable<String> flowable = (Flowable<String>) invoker.get(String.class);
      assertThat(flowable).isNotNull();
      Set<String> tzs = new TreeSet<>(); //FIXME [RESTEASY-2778] Intermittent flow / flux test failure
      flowable.subscribe(
        (String s) -> tzs.add(s),
        (Throwable t) -> LOG.error(t.getMessage(), t),
        () -> latch.countDown());
      latch.await(5, TimeUnit.SECONDS);
      assertThat(tzs).isNotEmpty();
      assertThat(tzs.size()).isGreaterThan(10);
      assertThat(latch.getCount()).isEqualTo(0L);
    }
  }
}
