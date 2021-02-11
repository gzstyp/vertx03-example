package com.fwtai.example;

import com.fwtai.tool.ToolClient;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Vert.x  Hello Router+EventBus
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2021-02-08 9:36
 * @QQ号码 444141300
 * @Email service@dwlai.com
 * @官网 http://www.fwtai.com
*/
public final class VertxRouter extends AbstractVerticle{

  @Override
  public void start(){
    final DeploymentOptions options = new DeploymentOptions();
    options.setWorker(true);
    options.setInstances(8);
    //vertx.deployVerticle(new VertxEventBus());//ok,部署调度启动
    vertx.deployVerticle("com.fwtai.example.VertxEventBus",options);//部署调度启动
    final Router router = Router.router(vertx);
    router.get("/index").blockingHandler(context -> {
      ToolClient.getResponse(context).end("Vertx Router,欢迎访问");
    });
    router.get("/").blockingHandler(this::index);// http://192.168.3.108:501/
    router.get("/api/v1.0/hello").blockingHandler(this::hello);// http://192.168.3.108:501/api/v1.0/hello?name=typ&age=36
    router.get("/api/v1.0/restful/:name").blockingHandler(this::restful);// http://192.168.3.108:501/api/v1.0/restful/typ
    router.get("/api/v1.0/eventBus").handler(this::eventBus);// http://192.168.3.108:501/api/v1.0/eventBus
    router.get("/api/v1.0/eventBusName/:name").handler(this::eventBusName);// http://192.168.3.108:501/api/v1.0/eventBusName/fwtai
    vertx.createHttpServer().requestHandler(router).listen(501);
  }

  protected void index(final RoutingContext context){
    ToolClient.getResponse(context).end("Vertx Router,欢迎访问");
  }

  protected void hello(final RoutingContext context){
    ToolClient.getResponse(context).end("Vertx Router!");
  }

  protected void restful(final RoutingContext context){
    final String name = context.pathParam("name");
    ToolClient.getResponse(context).end("Vertx Router,Welcome "+name);
  }

  protected void eventBus(final RoutingContext context){
    vertx.eventBus().request("hello.vertx.addr","",reply->{ //reply是回复|回答
      ToolClient.getResponse(context).end("EventBus,"+reply.result().body());
    });
  }

  protected void eventBusName(final RoutingContext context){
    final String name = context.pathParam("name");
    vertx.eventBus().request("hello.named.addr",name,reply->{//reply是回复|回答
      ToolClient.getResponse(context).end("EventBus,"+reply.result().body());
    });
  }
}