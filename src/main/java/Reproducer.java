import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;

import static io.vertx.core.http.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static io.vertx.core.http.HttpHeaders.ORIGIN;
import static io.vertx.core.http.HttpMethod.GET;

public class Reproducer {

    private final static int PORT = 9999;
    private final static String HOST = "localhost";
    private final static HttpServerOptions serverOpts = new HttpServerOptions().setPort(PORT).setHost(HOST);
    private final static HttpClientOptions clientOpts = new HttpClientOptions().setDefaultPort(PORT).setDefaultHost(HOST);
    private final static String ROUTE = "/api/1/stuff";

    public static void main2(String... args) { // KO
        final Vertx vertx = Vertx.vertx();
        final Router router = Router.router(vertx);
        router.route("/api/1/*").handler(CorsHandler.create("*").allowedHeader(ACCESS_CONTROL_ALLOW_ORIGIN.toString()).allowedMethod(GET));
        router.get(ROUTE).handler(ctx -> ctx.response().end("test"));
        launchServerAndTest(vertx, router);
    }

    public static void main(String... args) { // OK
        final Vertx vertx = Vertx.vertx();
        final Router router = Router.router(vertx);
        router.routeWithRegex("/api/1/.*").handler(CorsHandler.create("*").allowedHeader(ACCESS_CONTROL_ALLOW_ORIGIN.toString()).allowedMethod(GET));
        router.get(ROUTE).handler(ctx -> ctx.response().end("test"));
        launchServerAndTest(vertx, router);
    }

    private static void launchServerAndTest(Vertx vertx, Router router) {
        vertx.createHttpServer(serverOpts).requestHandler(router::accept).listen(res -> {
            if (res.failed()) res.cause().printStackTrace();
            client(vertx).get(ROUTE, resp -> {
                System.out.println(resp.statusCode());
                System.out.println(resp.getHeader(ACCESS_CONTROL_ALLOW_ORIGIN));
                resp.bodyHandler(buff -> System.out.println(buff.toString()));
            }).putHeader(ORIGIN, "http://localhost").end();
        });

    }

    private static HttpClient client(Vertx vertx) {
        return vertx.createHttpClient(clientOpts);
    }

}
