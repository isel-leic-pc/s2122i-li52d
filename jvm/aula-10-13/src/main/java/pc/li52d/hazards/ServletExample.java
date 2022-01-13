package pc.li52d.hazards;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static pc.li52d.threading.Utils.sleep;


public class ServletExample {

    /*
     * TCP port where to listen.
     * Standard port for HTTP is 80 but might be already in use
     */
    private static final int LISTEN_PORT = 8080;
    private static final Logger log = LoggerFactory.getLogger(ServletExample.class);

    public static void main(String[] args) throws Exception {

        System.setProperty("org.slf4j.simpleLogger.levelInBrackets", "true");

        log.info("Starting main...");

        String portDef = System.getenv("PORT");
        int port = portDef != null ? Integer.valueOf(portDef) : LISTEN_PORT;
        log.info("Listening on port {}", port);


        Server server = new Server(port);
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        handler.addServletWithMapping(new ServletHolder(new EchoServlet( )), "/*");

        server.start();
        log.info("Server started");
        server.join();

        log.info("main ends.");
    }

    static class EchoServlet extends HttpServlet {

        private String fieldRequestURI;

        @Override
        public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
            log.info("doGet request: URI='{}', method='{}'", request.getMethod(), request.getRequestURI());

            // private to the thread
            String localRequestURI = request.getRequestURI();

            // SHARED between all threads
            this.fieldRequestURI = request.getRequestURI(); // T1: 123 T2: abc

            sleep(100);

            String bodyString = String.format("Request processed on thread '%s', method='%s', URI='%s', URI = '%s'\n",
                Thread.currentThread().getName(),
                request.getMethod(),
                localRequestURI,
                fieldRequestURI
            );
            byte[] bodyBytes = bodyString.getBytes(StandardCharsets.UTF_8);

            response.addHeader("Content-Type", "text/plain, charset=utf-8");
            response.addHeader("Content-Length", Integer.toString(bodyBytes.length));
            response.getOutputStream().write(bodyBytes);
        }
    }

}

