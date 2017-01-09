# junit-http
A small HTTP servlet for executing JUnit tests from the web

## Why?
When doing functional testing via the UI, it's sometimes not possible to validate side effects that occur in backend systems. This little servlet allows the UI test to make a simple HTTP request to execute a test on the backend, which has full access to the systems where the side effects are performed. This is often referred to as "gray box testing". 

## How?
1. Make a WAR project
2. Add your test classes to it
3. Make junit-http a dependency
4. Deploy
5. Make an HTTP POST to http://example.com/deployedContext/com.example.tests.TestClassName/testMethod to run a single test, or http://example.com/deployedContext/com.example.tests.TestClassName to run all tests in the test class
6. Use the returned JSON to pass or fail your UI test

## Example Application

There is a stripped down example application included to demonstrate how to structure your tests and how to reuse your side-effecting components. The example uses a very simplified version of the hexagonal or ports and adaptors style architecture. In ports and adapters, you will generally use a separate module for each type of integration. For example, if using MySQL as the database for repositories, there will be a separate MySQL module that implements all of the repositories using that database. This is what the test module will have as a dependency.

There is an aspect of test by inference here, since the test will be reusing the integration module. However, this allows for maximum code reuse, and makes the tests less complex and brittle. Also should the integration change, the tests will not need to be updated. As long as the module is fully tested itself using standard testing, this should not be an issue.

In the included example, the ```junit-http-example-adapter``` module contains the repository implementation, and the ```junit-http-example-webapp``` module contains the application under test. ```junit-http-example-tests``` contains the test servlet. For simplicity there is no service layer or an interface for the repository.

To try out the example, run the ```junit-http-example-tests``` and ```junit-http-example-webapp``` webapps. This can be done using the ```jetty:run``` goal in each project.

Once both apps are running, try sending:

```GET http://localhost:8080/notes/save-note HTTP/1.1```

It should return a 404. Next, try sending:

```POST http://localhost:8081/tests/io.dfox.junit.http.example.ExampleTest/noteSaved HTTP/1.1``` 

It should return a 200 and something like the following response:

```json
{
  "results": [
    {
      "type": "failure",
      "grouping": "io.dfox.junit.http.example.ExampleTest",
      "name": "noteSaved",
      "error": {
        "name": "java.lang.AssertionError",
        "message": null
      },
      "trace": [
        "org.junit.Assert.fail(Assert.java:86)",
        "org.junit.Assert.assertTrue(Assert.java:41)",
        "org.junit.Assert.assertTrue(Assert.java:52)",
        "io.dfox.junit.http.example.ExampleTest.noteWritten(ExampleTest.java:38)",
        "sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)",
        "sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)",
        "sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)",
        "java.lang.reflect.Method.invoke(Method.java:497)",
        "org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)",
        "org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)",
        "org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)",
        "org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)",
        "org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:325)",
        "org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:78)",
        "io.dfox.junit.http.JunitHttpRunner.runTest(JunitHttpRunner.java:67)",
        "io.dfox.junit.http.JunitHttpRunner.runTests(JunitHttpRunner.java:78)",
        "io.dfox.junit.http.JUnitHttpServlet.doPost(JUnitHttpServlet.java:79)",
        "javax.servlet.http.HttpServlet.service(HttpServlet.java:707)",
        "javax.servlet.http.HttpServlet.service(HttpServlet.java:790)",
        "org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:830)",
        "org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:552)",
        "org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:143)",
        "org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:548)",
        "org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:1589)",
        "org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1213)",
        "org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:487)",
        "org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:1552)",
        "org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1126)",
        "org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:141)",
        "org.eclipse.jetty.server.handler.ContextHandlerCollection.handle(ContextHandlerCollection.java:213)",
        "org.eclipse.jetty.server.handler.HandlerCollection.handle(HandlerCollection.java:118)",
        "org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:132)",
        "org.eclipse.jetty.server.Server.handle(Server.java:550)",
        "org.eclipse.jetty.server.HttpChannel.handle(HttpChannel.java:321)",
        "org.eclipse.jetty.server.HttpConnection.onFillable(HttpConnection.java:254)",
        "org.eclipse.jetty.io.AbstractConnection$ReadCallback.succeeded(AbstractConnection.java:269)",
        "org.eclipse.jetty.io.FillInterest.fillable(FillInterest.java:97)",
        "org.eclipse.jetty.io.ChannelEndPoint$2.run(ChannelEndPoint.java:124)",
        "org.eclipse.jetty.util.thread.Invocable.invokePreferred(Invocable.java:102)",
        "org.eclipse.jetty.util.thread.strategy.ExecutingExecutionStrategy.invoke(ExecutingExecutionStrategy.java:58)",
        "org.eclipse.jetty.util.thread.strategy.ExecuteProduceConsume.produceConsume(ExecuteProduceConsume.java:201)",
        "org.eclipse.jetty.util.thread.strategy.ExecuteProduceConsume.run(ExecuteProduceConsume.java:133)",
        "org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:672)",
        "org.eclipse.jetty.util.thread.QueuedThreadPool$2.run(QueuedThreadPool.java:590)",
        "java.lang.Thread.run(Thread.java:745)"
      ]
    }
  ],
  "successful": false
}
```
Next, try sending the following:

```
POST /notes/save-note HTTP/1.1
Host: localhost:8080
Content-Type: text/plain
Content-Length: 15

This is my note
```

It should return a 200 OK. Now the note has been saved to the filesystem. Now when you run the test again:

```POST http://localhost:8081/tests/io.dfox.junit.http.example.ExampleTest/noteSaved HTTP/1.1``` 

You will get a 200 response with the following content:

```json
{
  "results": [
    {
      "type": "success",
      "grouping": "io.dfox.junit.http.example.ExampleTest",
      "name": "noteSaved"
    }
  ],
  "successful": true
}
```

## Test Data

In order to make writing tests on the client easier, you can also include test data, written in JSON, that can be accessed via the same servlet and also using the TestUtils.getTestData(String path) convenience method. This allows you to share data that you use in assertions and in tests so that they do not have to be specified in two places. In the example tests, there is one included you can access at the following URL:

```GET http://localhost:8081/data/notes.json``` 

The contents of which look like this:

```json
{
    "save": {
        "name": "save-note",
        "contents": "This is my saved note"
    },
    
    "load": {
        "name": "load-note",
        "contents": "This is my loaded note"
    },
    
    "fixture": {
        "name": "fixture-note",
        "contents": "This is my fixture note"
    }
}
```

You would use this in your UI test to send the appropriate note to the API for testing. Then, in your JUnit test:

```java
@Test
public void noteSaved() throws IOException {
    JsonNode notesFixture = getTestData("notes.json");
    JsonNode noteFixture = notesFixture.path("save");
    String name = noteFixture.path("name").asText();
    String expectedContents = noteFixture.path("contents").asText();
    
    Optional<InputStream> note = repository.getNote(name);
    assertTrue(note.isPresent());
    try(InputStream stream = note.get()){
        String contents = IOUtils.toString(stream);
        assertEquals(expectedContents, contents);
    }
}
```
## Fixtures

Sometimes, you will need to alter some state or do something on the server in-between actions on the UI to fully simulate a workflow. To do this, you can use fixtures. They are run the same way as tests, but instead of having a @Test annotation, they have a @Fixture annotation on the test class.

You can run the example one like this:

```POST http://localhost:8081/fixtures/io.dfox.junit.http.example.ExampleTest/createNote HTTP/1.1``` 

This will create a note on the server, so you can get it with:

```GET http://localhost:8080/notes/fixture-note HTTP/1.1```

## Frontends
There is currently one frontend for the [Nightwatch](http://nightwatchjs.org) testing framework at [https://github.com/cantinac/nightwatch-js-remote-assert](https://github.com/cantinac/nightwatch-js-remote-assert)

## The Future
We hope to have libaries for other common clients, such as iOS and Android testing frameworks, as well as other backends using the same REST API.


