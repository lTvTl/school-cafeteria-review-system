package org.zerock.listener;

import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class SessionListener implements ServletContextListener, HttpSessionListener {
	@Override
	public void contextInitialized(ServletContextEvent event) {
		event.getServletContext().setAttribute("activeUserCnt", new AtomicInteger());
	}

	@Override
	public void sessionCreated(HttpSessionEvent event) {
		counter(event.getSession().getServletContext()).incrementAndGet();
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		counter(event.getSession().getServletContext()).updateAndGet(count -> Math.max(0, count - 1));
	}

	public static int getActiveUserCnt(ServletContext context) {
		AtomicInteger count = counter(context);
		return count == null ? 0 : count.get();
	}

	private static AtomicInteger counter(ServletContext context) {
		return (AtomicInteger) context.getAttribute("activeUserCnt");
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// Keep the counter available while the container destroys remaining sessions.
	}
}
