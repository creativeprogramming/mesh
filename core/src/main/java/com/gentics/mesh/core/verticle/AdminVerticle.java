package com.gentics.mesh.core.verticle;

import static io.vertx.core.http.HttpMethod.GET;

import org.jacpfx.vertx.spring.SpringVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gentics.mesh.core.AbstractCoreApiVerticle;
import com.gentics.mesh.etc.MeshSpringConfiguration;
import com.gentics.mesh.git.GitPullChecker;

/**
 * The admin verticle provides core administration rest endpoints.
 * 
 * @author johannes2
 *
 */
@Component
@Scope("singleton")
@SpringVerticle
public class AdminVerticle extends AbstractCoreApiVerticle {

	private static final Logger log = LoggerFactory.getLogger(AdminVerticle.class);
	public static final String GIT_PULL_CHECKER_INTERVAL_KEY = "gitPullCheckerInterval";
	public static final String GIT_PULL_CHECKER_KEY = "gitPullChecker";

	public static final boolean DEFAULT_GIT_CHECKER = false;
	public static final long DEFAULT_GIT_CHECKER_INTERVAL = 60 * 5 * 100; // 5 Min

	@Autowired
	private MeshSpringConfiguration springConfiguration;

	GitPullChecker gitChecker;

	public AdminVerticle() {
		super("admin");
	}

	@Override
	public void registerEndPoints() throws Exception {
		if (config().getBoolean(GIT_PULL_CHECKER_KEY, DEFAULT_GIT_CHECKER)) {
			gitChecker = new GitPullChecker(config().getLong(GIT_PULL_CHECKER_INTERVAL_KEY, DEFAULT_GIT_CHECKER_INTERVAL));
		}

		addStatusHandler();

		// TODO secure handlers below
//		addBackupHandler();
//		addNeo4VertxRestartHandler();

		// addVerticleHandler();
		// addServiceHandler();

	}

	@Override
	public void stop() throws Exception {
		super.stop();
		if (gitChecker != null) {
			gitChecker.close();
		}
	}

	private void addStatusHandler() {
		route("/status").method(GET).handler(rc -> {
			rc.response().setStatusCode(200);
			rc.response().end("OK");
		});

	}

//	private void addServiceHandler() {
//		route("/deployService/:mavenCoordinates").method(GET).handler(rc -> {
//			// TODO impl me
//				rc.response().end("Deploy " + rc.request().params().get("mavenCoordinates"));
//			});
//
//		route("/undeployService/:mavenCoordinates").method(GET).handler(rc -> {
//			// TODO impl me
//				rc.response().end("Undeploy " + rc.request().params().get("mavenCoordinates"));
//			});
//
//	}
//
//	private void addVerticleHandler() {
//		route("/deployVerticle/:clazz").method(GET).handler(rc -> {
//			String clazz = rc.request().params().get("clazz");
//			try {
//				// TODO create merged jsonconfig (see mesh init)
//				JsonObject config = new JsonObject();
//				String id = deployAndWait(vertx, config, clazz);
//				rc.response().end("Deployed " + clazz + " id: " + id);
//			} catch (Exception e) {
//				rc.fail(e);
//			}
//		});
//
//		route("/undeployVerticle/:clazz").method(GET).handler(rc -> {
//			// TODO impl me
//				rc.response().end("Undeploy " + rc.request().params().get("clazz"));
//			});
//	}

}
