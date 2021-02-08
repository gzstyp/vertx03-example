package com.fwtai.example;

import io.vertx.core.AbstractVerticle;

/**
 * Vert.x  Hello HTTP
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2021-02-08 9:36
 * @QQ号码 444141300
 * @Email service@dwlai.com
 * @官网 http://www.fwtai.com
*/
public final class VertxHttp extends AbstractVerticle{

  @Override
  public void start(){
    vertx.createHttpServer().requestHandler(request->{
      final String path = request.path();
      if(path.startsWith("/api/v1.0/user/login")){
        request.response().end("Vert.x Hello HTTP");
      }
    }).listen(601);
  }
}