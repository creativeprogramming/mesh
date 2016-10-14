package com.gentics.mesh.context.impl;

import static com.gentics.mesh.http.HttpConstants.APPLICATION_JSON_UTF8;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.gentics.mesh.auth.MeshAuthProvider;
import com.gentics.mesh.context.AbstractInternalActionContext;
import com.gentics.mesh.core.data.MeshAuthUser;
import com.gentics.mesh.core.data.Project;
import com.gentics.mesh.core.rest.error.GenericRestException;
import com.gentics.mesh.dagger.MeshInternal;
import com.gentics.mesh.etc.RouterStorage;
import com.gentics.mesh.util.ETag;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;

/**
 * Vertx specific routing context based action context implementation.
 */
public class InternalRoutingActionContextImpl extends AbstractInternalActionContext {

	private static final Logger log = LoggerFactory.getLogger(InternalRoutingActionContextImpl.class);

	private RoutingContext rc;

	private MeshAuthUser user;

	private Project project;

	private Map<String, Object> data = new ConcurrentHashMap<>();

	public static final String LOCALE_MAP_DATA_KEY = "locale";

	/**
	 * Create a new routing context based vertx action context.
	 * 
	 * @param rc
	 */
	public InternalRoutingActionContextImpl(RoutingContext rc) {
		this.rc = rc;
		if (rc.data() != null) {
			this.data.putAll(rc.data());
		}
	}

	@Override
	public String getParameter(String parameterName) {
		return rc.request().getParam(parameterName);
	}

	@Override
	public MultiMap getParameters() {
		return rc.request().params();
	}

	@Override
	public void setParameter(String name, String value) {
		rc.request().params().set(name, value);
	}

	@Override
	public void send(String body, HttpResponseStatus status) {
		rc.response().putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_UTF8);
		rc.response().putHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		rc.response().setStatusCode(status.code()).end(body);
	}

	@Override
	public void send(HttpResponseStatus status) {
		rc.response().setStatusCode(status.code()).end();
	}

	@Override
	public void setEtag(String entityTag, boolean isWeak) {
		rc.response().putHeader(HttpHeaders.ETAG, ETag.prepareHeader(entityTag, isWeak));
	}

	@Override
	public void setLocation(String basePath) {
		String protocol = "http://";
		if (rc.request().isSSL()) {
			protocol = "https://";
		}
		String hostAndPort = rc.request().host();
		rc.response().putHeader(HttpHeaders.LOCATION, protocol + hostAndPort + basePath);
	}

	@Override
	public boolean matches(String etag, boolean isWeak) {
		String headerValue = rc.request().getHeader(HttpHeaders.IF_NONE_MATCH);
		if (headerValue == null) {
			return false;
		}
		return headerValue.equals(ETag.prepareHeader(etag, isWeak));
	}

	@Override
	public Map<String, Object> data() {
		return data;
	}

	@Override
	public String query() {
		return rc.request().query();
	}

	@Override
	public void fail(Throwable cause) {
		rc.fail(cause);
	}

	@Override
	public String getBodyAsString() {
		return rc.getBodyAsString();
	}

	@Override
	public Locale getLocale() {
		return (Locale) data().computeIfAbsent(LOCALE_MAP_DATA_KEY, map -> {
			String header = rc.request().headers().get("Accept-Language");
			return getLocale(header);
		});
	}

	@Override
	public Set<FileUpload> getFileUploads() {
		return rc.fileUploads();
	}

	@Override
	public MultiMap requestHeaders() {
		return rc.request().headers();
	}

	@Override
	public void logout() {
		Session session = rc.session();
		if (session != null) {
			session.destroy();
		}
		rc.addCookie(Cookie.cookie(MeshAuthProvider.TOKEN_COOKIE_KEY, "deleted").setMaxAge(0));
		rc.clearUser();
	}

	@Override
	public void addCookie(Cookie cookie) {
		rc.addCookie(cookie);
	}

	@Override
	public MeshAuthUser getUser() {
		if (user == null && rc.user() != null) {
			if (rc.user() instanceof MeshAuthUser) {
				user = (MeshAuthUser) rc.user();
			} else {
				log.error("Could not load user from routing context.");
				// TODO i18n
				throw new GenericRestException(INTERNAL_SERVER_ERROR, "Could not load request user");
			}
		}
		return user;
	}

	@Override
	public void setUser(MeshAuthUser user) {
		rc.setUser(user);
	}

	@Override
	public Project getProject() {
		if (project == null) {
			String projectName = get(RouterStorage.PROJECT_CONTEXT_KEY);
			project = MeshInternal.get().boot().meshRoot().getProjectRoot().findByName(projectName);
		}
		return project;
	}

}
