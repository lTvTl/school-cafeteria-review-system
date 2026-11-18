package org.zerock.listener;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpSessionEvent;
import org.junit.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;

public class SessionListenerTests {
    @Test public void countsLiveSessionsWithoutCountingIdChangesAsNewVisitors() {
        MockServletContext context = new MockServletContext();
        SessionListener listener = new SessionListener();
        listener.contextInitialized(new ServletContextEvent(context));
        MockHttpSession session = new MockHttpSession(context);
        HttpSessionEvent event = new HttpSessionEvent(session);
        listener.sessionCreated(event);
        session.changeSessionId();
        assertEquals(1, SessionListener.getActiveUserCnt(context));
        listener.sessionDestroyed(event);
        assertEquals(0, SessionListener.getActiveUserCnt(context));
        listener.sessionDestroyed(event);
        assertEquals(0, SessionListener.getActiveUserCnt(context));
    }

    @Test public void concurrentSessionsRemainAccurateAndContextsAreIsolated() throws Exception {
        MockServletContext context = new MockServletContext();
        MockServletContext other = new MockServletContext();
        SessionListener listener = new SessionListener();
        listener.contextInitialized(new ServletContextEvent(context));
        listener.contextInitialized(new ServletContextEvent(other));
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            List<Future<?>> work = new ArrayList<>();
            List<HttpSessionEvent> events = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                HttpSessionEvent event = new HttpSessionEvent(new MockHttpSession(context));
                events.add(event);
                work.add(pool.submit(() -> listener.sessionCreated(event)));
            }
            for (Future<?> future : work) future.get();
            assertEquals(100, SessionListener.getActiveUserCnt(context));
            assertEquals(0, SessionListener.getActiveUserCnt(other));
            work.clear();
            for (HttpSessionEvent event : events) work.add(pool.submit(() -> listener.sessionDestroyed(event)));
            for (Future<?> future : work) future.get();
            assertEquals(0, SessionListener.getActiveUserCnt(context));
        } finally {
            pool.shutdownNow();
        }
    }
}
