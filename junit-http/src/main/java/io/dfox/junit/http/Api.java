package io.dfox.junit.http;

import com.fasterxml.jackson.databind.JsonNode;
import io.dfox.junit.http.api.InvalidPathException;
import io.dfox.junit.http.api.MethodNotFoundException;
import io.dfox.junit.http.api.RunnerException;
import io.dfox.junit.http.api.Summary;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.error.ServerErrorHandler;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import static ratpack.jackson.Jackson.json;
import ratpack.path.PathBinding;

/**
 * The HTTP API.
 */
public class Api {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Api.class);
    private static final int INTERNAL_ERROR_STATUS = 500;

    private final Service app = new Service();
    
    /**
     * Get the path past the current binding.
     * 
     * @param context The context
     * @return The path
     */
    private String getPastBinding(final Context context) {
        PathBinding binding = context.getPathBinding();
        return binding.getPastBinding();
    }
    
    public Handler getTestData() {
        return context -> {
            String path = getPastBinding(context);
            Optional<JsonNode> data = app.getData(path);
            if (data.isPresent()) {
                context.render(data.get().toString());
            }
            else {
                context.notFound();
            }
        };
    }
    
    /**
     * @return A handler to run a test
     */
    public Handler runTest() {
        return context -> {
            String path = getPastBinding(context);
            Summary summary = app.runTest(path);
            context.render(json(summary));
        };
    }
    
    /**
     * @return A handler to run a fixture
     */
    public Handler runFixture() {
        return context -> {
            String path = getPastBinding(context);
            Summary summary = app.runFixture(path);
            context.render(json(summary));
        };
    }
    
    /**
     * @return A handler for server errors
     */
    public ServerErrorHandler handleServerErrors() {
        return (context, e) -> {
            if (e instanceof MethodNotFoundException || e instanceof InvalidPathException) {
                context.notFound();
            }
            else if (e instanceof RunnerException) {
                context.getResponse().status(INTERNAL_ERROR_STATUS);
                LOGGER.error("A server error occurred servicing resource: " + context.getRequest().getUri(), e);
            }
        };
    }
}
