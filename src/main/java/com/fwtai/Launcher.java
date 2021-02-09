package com.fwtai;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class Launcher extends AbstractVerticle {

  @Override
  public void start(final Promise<Void> startPromise) throws Exception {
    vertx.createHttpServer().requestHandler(req -> {
      req.response()
        .putHeader("content-type", "text/html")
        .end("Hello from Vert.x!");
    }).listen(602, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }
}