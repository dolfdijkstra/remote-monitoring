package com.fatwire.management.jetty;

import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fatwire.management.JMXMetric;
import com.fatwire.management.dao.HostDao;
import com.fatwire.management.dao.JMXMetricDao;
import com.fatwire.management.domain.Host;

public class HostController implements Controller {
    private static final Logger log = LoggerFactory
            .getLogger(HostController.class);

    private HostDao hostDao;

    private JMXMetricDao metricDao;

    public ModelAndView handleRequest(HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() == 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        pathInfo = pathInfo.substring(1);
        log.debug(pathInfo);
        long t = System.nanoTime();
        Host host = discoverHost(pathInfo);
        long t1 = System.nanoTime();
        log.debug("discoverHost took " + (t1 - t) / 1000 + "us");
        if (host != null) {
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("host", host);
            long t2 = System.nanoTime();
            m.put("metrics", this.metricDao.queryMetrics(host));
            long t3 = System.nanoTime();
            log.debug("queryMetrics took " + (t3 - t2) / 1000 + "us");

            return new ModelAndView(m, hostView);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

    }

    View hostView = new View() {

        /* (non-Javadoc)
         * @see com.fatwire.management.jetty.View#render(java.util.Map, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
         */
        @SuppressWarnings("unchecked")
        @Override
        public void render(Map model, HttpServletRequest request,
                HttpServletResponse response) throws Exception {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/html");
            response.setHeader("Cache-Control", "must-revalidate");
            response.setCharacterEncoding("UTF-8");
            PrintWriter writer = response.getWriter();
            Host host = (Host) model.get("host");

            writer
                    .println("<html><head><title>JMX Beans for "
                            + host.getAddress().getHostName()
                            + "</title></head><body>");

            writer.println("<div><a href=\"/\">index</a>");
            for (Host h : hostDao.findHosts()) {
                writer.println(" | <a href=\""
                        + h.getAddress().getHostAddress() + "_"
                        + h.getJmxPort() + "\">" + h.getAddress().getHostName()
                        + "_" + h.getJmxPort() + "</a>");

            }
            writer.println("</div>");
            writer.println("<div>" + host.getAddress().getHostAddress());
            for (JMXMetric m : (Iterable<JMXMetric>) model.get("metrics")) {
                int i = 0;

                for (URL template : m.getGraphTemplateUrls()) {
                    String suffix = (i == 0) ? "" : "-" + Integer.toString(i);

                    String graphName = m.getObjName().getDomain() + "/"
                            + m.getDescription() + suffix + ".png";
                    /*
                    String graphName2 = m.getObjName().getDomain()
                            + "/"
                            + m.getObjName().toString().substring(
                                    m.getObjName().getDomain().length() + 1)
                            + "/" + template.getPath();
                    String digest = DigestUtils.md5Hex(graphName2);
                    */
                    writer
                            .write("<div><img class=\"rrd-image\" src=\"/"
                                    + StringEscape.forHTML(host.getAddress().getHostAddress())
                                    + "_"
                                    + host.getJmxPort()
                                    + "/"
                                    + StringEscape.forHTML(graphName)
                                    + "\" alt=\""
                                    + StringEscape.forHTML(m.getObjName()
                                            .toString())
                                    + "\" onclick=\"this.src=((this.src.indexOf('?') == -1 ? this.src : this.src.substring(0,this.src.indexOf('?'))) + '?' + (new Date()).getTime());return false;\"/></div>");
                }
            }
            writer.println("</div>");

            writer.println("</body></html>");

        }

    };

    private Host discoverHost(String host) {
        int t = host.lastIndexOf("_");
        return this.hostDao.findHost(host.substring(0, t), Integer
                .parseInt(host.substring(t + 1)));
    }

    public long getLastModified(HttpServletRequest req) {

        return -1;
    }

    public void setHostDao(HostDao hostDao) {
        this.hostDao = hostDao;

    }

    /**
     * @param metricDao the metricDao to set
     */
    public void setMetricDao(JMXMetricDao metricDao) {
        this.metricDao = metricDao;
    }

}
