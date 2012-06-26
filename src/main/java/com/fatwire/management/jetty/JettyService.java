package com.fatwire.management.jetty;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JettyService {
    private static final Logger log = LoggerFactory
            .getLogger(JettyService.class);

    private Server server;

    private Map<Controller, String> mappings = new HashMap<Controller, String>();

    public void init(int port) throws Exception {

        server = new Server(port);

        ServletContextHandler context = new ServletContextHandler(server, "/",
                false, false) {

            /* (non-Javadoc)
             * @see org.eclipse.jetty.server.handler.ContextHandler#doHandle(java.lang.String, org.eclipse.jetty.server.Request, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
             */
            @Override
            public void doHandle(String target, Request baseRequest,
                    HttpServletRequest request, HttpServletResponse response)
                    throws IOException, ServletException {
                long t = log.isDebugEnabled() ? System.nanoTime() : 0L;
                try {
                    log.debug("target: {}, baseRequest: {}", target, baseRequest.getDispatcherType().name());
                    super.doHandle(target, baseRequest, request, response);
                } finally {
                    long t2 = log.isDebugEnabled() ? System.nanoTime() : 0L;
                    log.debug("doHandle took {} us  for {}", (t2 - t) / 1000L,
                            request.getRequestURI());
                }
            }

        };
        for (Map.Entry<Controller, String> e : mappings.entrySet()) {
            DispatcherServlet s = new DispatcherServlet();
            s.setController(e.getKey());
            context.addServlet(new ServletHolder(s), e.getValue());

        }

        server.setHandler(context);
        server.start();

    }

    public void shutdown() throws Exception {
        /*
        ThreadInfo[] ti = java.lang.management.ManagementFactory
                .getThreadMXBean().dumpAllThreads(false, false);
        for (ThreadInfo t : ti) {
            System.out.println(t.toString());
            for (StackTraceElement e : t.getStackTrace()) {
                System.out.println(e.toString());
            }
        }
        */
        server.stop();

    }

    public void addController(Controller c, String mapping) {
        mappings.put(c, mapping);
    }

}
