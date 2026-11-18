package org.zerock.security;

import static org.junit.Assert.*;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.Filter;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.mock.web.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.zerock.mapper.MemberMapper;

public class MenuReviewSecurityTests {
    private GenericWebApplicationContext context;
    private Filter filter;

    @Before public void setUp() {
        context = new GenericWebApplicationContext(new MockServletContext());
        AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
        context.getBeanFactory().registerSingleton("dataSource", Proxy.newProxyInstance(
            DataSource.class.getClassLoader(), new Class<?>[]{DataSource.class},
            (proxy, method, args) -> { throw new AssertionError("No database access expected"); }));
        context.getBeanFactory().registerSingleton("memberMapper", Proxy.newProxyInstance(
            MemberMapper.class.getClassLoader(), new Class<?>[]{MemberMapper.class},
            (proxy, method, args) -> { throw new AssertionError("No member query expected"); }));
        new XmlBeanDefinitionReader(context).loadBeanDefinitions("file:src/main/webapp/WEB-INF/spring/security-context.xml");
        context.refresh();
        filter = context.getBean("springSecurityFilterChain", Filter.class);
    }
    @After public void close() { if (context != null) context.close(); }

    private MockHttpServletRequest request(String path, boolean loggedIn, boolean csrf) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setServletPath(path);
        request.setSession(new MockHttpSession());
        if (loggedIn) request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
            new SecurityContextImpl(new UsernamePasswordAuthenticationToken("alice", "unused", AuthorityUtils.createAuthorityList("ROLE_USER"))));
        if (csrf) {
            HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
            CsrfToken token = repository.generateToken(request);
            repository.saveToken(token, request, new MockHttpServletResponse());
            request.setParameter(token.getParameterName(), token.getToken());
        }
        return request;
    }
    @Test public void anonymousCannotReachAnyWriteEndpoint() throws Exception {
        for (String path : new String[]{"/menu/1/reviews", "/menu/1/reviews/1/modify", "/menu/1/reviews/1/remove"}) {
            AtomicBoolean reached = new AtomicBoolean(); MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request(path, false, true), response, (req, res) -> reached.set(true));
            assertFalse(reached.get()); assertEquals(302, response.getStatus());
            assertTrue(response.getRedirectedUrl().endsWith("/customLogin"));
        }
    }
    @Test public void authenticatedWriteRequiresCsrf() throws Exception {
        for (String path : new String[]{"/menu/1/reviews", "/menu/1/reviews/1/modify", "/menu/1/reviews/1/remove"}) {
            AtomicBoolean reached = new AtomicBoolean(); MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request(path, true, false), response, (req, res) -> reached.set(true));
            assertFalse(reached.get()); assertEquals(302, response.getStatus());
            assertEquals("/accessError", response.getRedirectedUrl());
            filter.doFilter(request(path, true, true), new MockHttpServletResponse(), (req, res) -> reached.set(true));
            assertTrue(reached.get());
        }
    }

    @Test public void adminRoutesRejectAnonymousAndRegularMembers() throws Exception {
        for (String path : new String[]{"/admin", "/admin/", "/admin/settings"}) {
            for (boolean loggedIn : new boolean[]{false, true}) {
                MockHttpServletRequest request = request(path, loggedIn, false);
                request.setMethod("GET");
                AtomicBoolean reached = new AtomicBoolean();
                MockHttpServletResponse response = new MockHttpServletResponse();
                filter.doFilter(request, response, (req, res) -> reached.set(true));
                assertFalse("Unexpected access: " + path, reached.get());
                assertEquals(302, response.getStatus());
                assertTrue(response.getRedirectedUrl().endsWith(loggedIn ? "/accessError" : "/customLogin"));
            }
        }
    }

    @Test public void adminRoutesAllowAdminAndStillRequireCsrfForWrites() throws Exception {
        for (String path : new String[]{"/admin", "/admin/", "/admin/settings"}) {
            for (String method : new String[]{"GET", "POST"}) {
                for (boolean csrf : new boolean[]{false, true}) {
                    MockHttpServletRequest request = request(path, true, csrf);
                    request.setMethod(method);
                    request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                        new SecurityContextImpl(new UsernamePasswordAuthenticationToken("admin0", "unused", AuthorityUtils.createAuthorityList("ROLE_ADMIN"))));
                    AtomicBoolean reached = new AtomicBoolean();
                    filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> reached.set(true));
                    assertEquals(method.equals("GET") || csrf, reached.get());
                }
            }
        }
    }
}
