package com.fwtai.example;

import com.fwtai.tool.ToolClient;
import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Vert.x Infinispan Cluster Manager_分布式_集群,单实例启动多个则构成集群模式,如下:
 * 启动命令 java -jar fat.jar -cluster -Djava.net.preferIPv4Stack=true -Dhttp.port=8090
 * 启动命令 java -jar fat.jar -cluster -Djava.net.preferIPv4Stack=true -Dhttp.port=8070
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2021-02-08 9:36
 * @QQ号码 444141300
 * @Email service@dwlai.com
 * @官网 http://www.fwtai.com
*/
public final class VertxCluster extends AbstractVerticle{

  @Override
  public void start(){
    vertx.deployVerticle(new ServiceClusterVertx());//ok,部署调度启动
    final Router router = Router.router(vertx);
    router.get("/index").blockingHandler(context -> {
      ToolClient.getResponse(context).end("Vertx Router,欢迎访问");
    });
    router.get("/").blockingHandler(this::index);// http://192.168.3.108:501/
    router.get("/api/v1.0/hello").blockingHandler(this::hello);// http://192.168.3.108:501/api/v1.0/hello?name=typ&age=36
    router.get("/api/v1.0/restful/:name").blockingHandler(this::restful);// http://192.168.3.108:501/api/v1.0/restful/typ
    router.get("/api/v1.0/eventBus").handler(this::eventBus);// http://192.168.3.108:501/api/v1.0/eventBus
    router.get("/api/v1.0/eventBusName/:name").handler(this::eventBusName);// http://192.168.3.108:501/api/v1.0/eventBusName/fwtai
    int httpPort = 501;
    try {
      httpPort = Integer.parseInt(System.getProperty("http.port"),501);
    } catch (final NumberFormatException e) {
    }
    vertx.createHttpServer().requestHandler(router).listen(httpPort);
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
    vertx.eventBus().request("hello.vertx.addr","",reply->{
      ToolClient.getResponse(context).end("EventBus,"+reply.result().body());
    });
  }

  protected void eventBusName(final RoutingContext context){
    final String name = context.pathParam("name");
    vertx.eventBus().request("hello.named.addr",name,reply->{
      ToolClient.getResponse(context).end("EventBus,"+reply.result().body());
    });
  }
}