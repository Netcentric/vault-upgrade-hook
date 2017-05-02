/*
 * (C) Copyright 2016 Netcentric AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package biz.netcentric.vlt.upgrade.handler.groovy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.request.RequestParameterMap;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.request.RequestProgressTracker;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

@SuppressWarnings({ "deprecation", "rawtypes", "unchecked" })
public class FakeRequest implements SlingHttpServletRequest {
    private final String method;
    private final String path;
    private final Map<String, Object> attributes;
    private final Map<String, String[]> parameters;
    private final HttpSession session;
    private final ResourceResolver resourceResolver;

    private static final class FakeHttpSession implements HttpSession {
	@Override
	public long getCreationTime() {
	    throw new UnsupportedOperationException();
	}

	@Override
	public String getId() {
	    throw new UnsupportedOperationException();
	}

	@Override
	public long getLastAccessedTime() {
	    throw new UnsupportedOperationException();
	}

	@Override
	public ServletContext getServletContext() {
	    throw new UnsupportedOperationException();
	}

	@Override
	public void setMaxInactiveInterval(int i) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public int getMaxInactiveInterval() {
	    throw new UnsupportedOperationException();
	}

	@Override
	public HttpSessionContext getSessionContext() {
	    throw new UnsupportedOperationException();
	}

	@Override
	public Object getAttribute(String s) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public Object getValue(String s) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public Enumeration getAttributeNames() {
	    throw new UnsupportedOperationException();
	}

	@Override
	public String[] getValueNames() {
	    throw new UnsupportedOperationException();
	}

	@Override
	public void setAttribute(String s, Object o) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public void putValue(String s, Object o) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public void removeAttribute(String s) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public void removeValue(String s) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public void invalidate() {
	    throw new UnsupportedOperationException();
	}

	@Override
	public boolean isNew() {
	    throw new UnsupportedOperationException();
	}
    }

    public FakeRequest(ResourceResolver resourceResolver, String method, String path, Map<String, Object> params) {
	this.resourceResolver = resourceResolver;
	this.method = method;
	this.path = path;
	attributes = new HashMap<String, Object>();
	parameters = new HashMap<String, String[]>();
	session = new FakeHttpSession();

	for (String key : params.keySet()) {
	    Object value = params.get(key);

	    // internally, Sling seems to expect all parameter values to be
	    // String[]
	    if (params.get(key) instanceof String[]) {
		parameters.put(key, (String[]) value);
	    } else {
		parameters.put(key, new String[] { value.toString() });
	    }
	}
    }

    @Override
    public String getAuthType() {
	return null;
    }

    @Override
    public String getContextPath() {
	return "";
    }

    @Override
    public Cookie[] getCookies() {
	return new Cookie[0];
    }

    @Override
    public long getDateHeader(String name) {
	return -1;
    }

    @Override
    public String getHeader(String name) {
	return null;
    }

    @Override
    public Enumeration getHeaderNames() {
	return null;
    }

    @Override
    public Enumeration getHeaders(String name) {
	return null;
    }

    @Override
    public int getIntHeader(String name) {
	return -1;
    }

    @Override
    public String getMethod() {
	return method;
    }

    @Override
    public String getPathInfo() {
	return null;
    }

    @Override
    public String getPathTranslated() {
	return null;
    }

    @Override
    public String getQueryString() {
	return null;
    }

    @Override
    public String getRemoteUser() {
	return null;
    }

    @Override
    public String getRequestURI() {
	return path;
    }

    @Override
    public StringBuffer getRequestURL() {
	return new StringBuffer("http://localhost:4502" + path);
    }

    @Override
    public String getRequestedSessionId() {
	return null;
    }

    @Override
    public String getServletPath() {
	return path;
    }

    @Override
    public HttpSession getSession() {
	return session;
    }

    @Override
    public HttpSession getSession(boolean create) {
	return session;
    }

    @Override
    public Principal getUserPrincipal() {
	return null;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
	return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
	return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
	return false;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
	return false;
    }

    @Override
    public boolean isUserInRole(String role) {
	return false;
    }

    @Override
    public Object getAttribute(String name) {
	return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
	return Collections.enumeration(attributes.keySet());
    }

    @Override
    public String getCharacterEncoding() {
	return "utf-8";
    }

    @Override
    public int getContentLength() {
	return 0;
    }

    @Override
    public String getContentType() {
	return null;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
	return null;
    }

    @Override
    public String getLocalAddr() {
	return null;
    }

    @Override
    public String getLocalName() {
	return null;
    }

    @Override
    public int getLocalPort() {
	return 0;
    }

    @Override
    public Locale getLocale() {
	return null;
    }

    @Override
    public Enumeration getLocales() {
	return null;
    }

    @Override
    public String getParameter(String name) {
	try {
	    final Object value = parameters.get(name);

	    if (value instanceof String[]) {
		return ((String[]) value)[0];
	    }

	    return (String) value;
	} catch (ClassCastException e) {
	    return null;
	}
    }

    @Override
    public Map<String, String[]> getParameterMap() {
	return parameters;
    }

    @Override
    public Enumeration<String> getParameterNames() {
	return Collections.enumeration(parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
	throw new UnsupportedOperationException();
    }

    @Override
    public String getProtocol() {
	return "HTTP/1.1";
    }

    @Override
    public BufferedReader getReader() throws IOException {
	return null;
    }

    @Override
    public String getRealPath(String path) {
	return null;
    }

    @Override
    public String getRemoteAddr() {
	return null;
    }

    @Override
    public String getRemoteHost() {
	return null;
    }

    @Override
    public int getRemotePort() {
	return 0;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
	return null;
    }

    @Override
    public String getScheme() {
	return "http";
    }

    @Override
    public String getServerName() {
	return null;
    }

    @Override
    public int getServerPort() {
	return 0;
    }

    @Override
    public boolean isSecure() {
	return false;
    }

    @Override
    public void removeAttribute(String name) {
	attributes.remove(name);
    }

    @Override
    public void setAttribute(String name, Object o) {
	attributes.put(name, o);
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
    }

    @Override
    public Resource getResource() {
	return null;
    }

    @Override
    public ResourceResolver getResourceResolver() {
	return resourceResolver;
    }

    @Override
    public RequestPathInfo getRequestPathInfo() {
	return null;
    }

    @Override
    public RequestParameter getRequestParameter(final String s) {
	final String value = getParameter(s);
	if (value != null) {
	    return new RequestParameter() {
		@Override
		public String getName() {
		    return s;
		}

		@Override
		public boolean isFormField() {
		    return false;
		}

		@Override
		public String getContentType() {
		    return null;
		}

		@Override
		public long getSize() {
		    return value.length();
		}

		@Override
		public byte[] get() {
		    return value.getBytes();
		}

		@Override
		public InputStream getInputStream() throws IOException {
		    return null;
		}

		@Override
		public String getFileName() {
		    return null;
		}

		@Override
		public String getString() {
		    return value;
		}

		@Override
		public String getString(String s) throws UnsupportedEncodingException {
		    return value;
		}
	    };
	}
	return null;
    }

    @Override
    public RequestParameter[] getRequestParameters(String s) {
	return new RequestParameter[0];
    }

    @Override
    public RequestParameterMap getRequestParameterMap() {
	return null;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s, RequestDispatcherOptions requestDispatcherOptions) {
	return null;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(Resource resource,
	    RequestDispatcherOptions requestDispatcherOptions) {
	return null;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(Resource resource) {
	return null;
    }

    @Override
    public Cookie getCookie(String s) {
	return null;
    }

    @Override
    public String getResponseContentType() {
	return null;
    }

    @Override
    public Enumeration<String> getResponseContentTypes() {
	return null;
    }

    @Override
    public ResourceBundle getResourceBundle(Locale locale) {
	return null;
    }

    @Override
    public ResourceBundle getResourceBundle(String s, Locale locale) {
	return null;
    }

    @Override
    public RequestProgressTracker getRequestProgressTracker() {
	return null;
    }

    @Override
    public <AdapterType> AdapterType adaptTo(Class<AdapterType> aClass) {
	if (ResourceResolver.class.equals(aClass)) {
	    return (AdapterType) getResourceResolver();
	}
	return null;
    }

    @Override
    public List<RequestParameter> getRequestParameterList() {
	return null;
    }
}
