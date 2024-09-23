# Wildfly RxJava2 Issue

This is a reproducer of an issue that I'm facing while trying to get working a 
simple RESTeasy service deployed in Wildfly 33.0 bootable.

The service exposes a simple REST API on the `/tz` endpoint which returns a 
`Flowable<String>`. The build process creates, via the `jib-maven-plugin` a
Docker image containing the Wildfly 33.0 bootable server with the REST API
deployed.

An integration test is provided which, on the behalf of the `testcontainers`,
starts the RESTeasy service and executes a simple test aagainst it, by invoking 
its unique endpoint.

## Expected behaviour: 

The test should execute successfully

## Actual behaviour:

The following exception is raised:

    jakarta.ws.rs.ProcessingException: RESTEASY008200: JSON Binding deserialization error: jakarta.json.bind.JsonbException: Cannot create instance of a class: class org.jboss.resteasy.plugins.providers.sse.SseEventInputImpl, No default constructor found.
        at org.jboss.resteasy.plugins.providers.jsonb.JsonBindingProvider.readFrom(JsonBindingProvider.java:78)
        at org.jboss.resteasy.core.interception.jaxrs.AbstractReaderInterceptorContext.readFrom(AbstractReaderInterceptorContext.java:99)
        at org.jboss.resteasy.core.interception.jaxrs.AbstractReaderInterceptorContext.proceed(AbstractReaderInterceptorContext.java:81)
        at org.jboss.resteasy.client.jaxrs.internal.ClientResponse.readFrom(ClientResponse.java:192)
        at org.jboss.resteasy.specimpl.BuiltResponse.readEntity(BuiltResponse.java:75)
        at org.jboss.resteasy.specimpl.AbstractBuiltResponse.readEntity(AbstractBuiltResponse.java:232)
        at org.jboss.resteasy.plugins.providers.sse.client.SseEventSourceImpl$EventHandler.run(SseEventSourceImpl.java:328)
        at org.jboss.resteasy.plugins.providers.sse.client.SseEventSourceScheduler$1.run(SseEventSourceScheduler.java:80)
        at org.jboss.resteasy.concurrent.ContextualExecutors.lambda$runnable$2(ContextualExecutors.java:312)
        at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:572)
        at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:317)
        at java.base/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:304)
        at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144)
        at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642)
        at java.base/java.lang.Thread.run(Thread.java:1583)
    Caused by: jakarta.json.bind.JsonbException: Cannot create instance of a class: class org.jboss.resteasy.plugins.providers.sse.SseEventInputImpl, No default constructor found.
      at org.eclipse.yasson.internal.deserializer.DefaultObjectInstanceCreator.<init>(DefaultObjectInstanceCreator.java:44)
      at org.eclipse.yasson.internal.deserializer.DeserializationModelCreator.createObjectDeserializer(DeserializationModelCreator.java:251)
      at org.eclipse.yasson.internal.deserializer.DeserializationModelCreator.deserializerChainInternal(DeserializationModelCreator.java:193)
      at org.eclipse.yasson.internal.deserializer.DeserializationModelCreator.deserializerChain(DeserializationModelCreator.java:135)
      at org.eclipse.yasson.internal.deserializer.DeserializationModelCreator.deserializerChain(DeserializationModelCreator.java:123)
      at org.eclipse.yasson.internal.DeserializationContextImpl.deserializeItem(DeserializationContextImpl.java:137)
      at org.eclipse.yasson.internal.DeserializationContextImpl.deserialize(DeserializationContextImpl.java:127)
      at org.eclipse.yasson.internal.JsonBinding.deserialize(JsonBinding.java:55)
      at org.eclipse.yasson.internal.JsonBinding.fromJson(JsonBinding.java:95)
      at org.jboss.resteasy.plugins.providers.jsonb.ManagedJsonb.fromJson(ManagedJsonb.java:73)
      at org.jboss.resteasy.plugins.providers.jsonb.JsonBindingProvider.readFrom(JsonBindingProvider.java:71)

## Test and deploy

In order to deploy and test proceed as follows:

    $ git clone https://github.com/nicolasduminil/quarkus-test-resource-lifecycle-manager-issue.git
    $ cd quarkus-test-resource-lifecycle-manager-issue
    $ mvn package
    $ mvn failsafe:integration-test