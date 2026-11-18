package org.zerock.controller;

import static org.junit.Assert.*;
import java.util.Map;
import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpSessionEvent;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.ui.ExtendedModelMap;
import org.zerock.listener.SessionListener;

public class AdminControllerTests {
    @Test public void pageAndStatusUseCurrentSessionCountAndRealMemoryUsage() {
        MockServletContext context = new MockServletContext();
        SessionListener listener = new SessionListener();
        listener.contextInitialized(new ServletContextEvent(context));
        MockHttpSession session = new MockHttpSession(context);
        listener.sessionCreated(new HttpSessionEvent(session));
        MockHttpServletRequest request = new MockHttpServletRequest(context);
        request.setSession(session);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AdminController controller = new AdminController();
        ExtendedModelMap model = new ExtendedModelMap();
        assertEquals("admin", controller.admin(request, response, model));
        assertEquals(1, model.get("activeUserCnt"));
        Map<String, Object> status = controller.status(request, response);
        assertEquals("no-store", response.getHeader("Cache-Control"));
        assertEquals(1, status.get("activeUserCnt"));
        double usage = (Double) status.get("memoryUsage");
        assertTrue(usage >= 0 && usage <= 100);
        assertEquals(usage >= 90 ? "BUSY" : "NORMAL", status.get("serverStatus"));
        assertEquals(3, status.size());
    }
}
