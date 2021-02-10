package com.fwtai.example;

import com.fwtai.tool.ToolClient;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * Vert.x Chained Routes And Static Handlers
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2021-02-08 9:36
 * @QQ号码 444141300
 * @Email service@dwlai.com
 * @官网 http://www.fwtai.com
*/
public final class VertxStatic extends AbstractVerticle{

  @Override
  public void start(final Promise<Void> start){
    vertx.deployVerticle(new ServiceClusterVertx());//ok,部署调度启动
    final Router router = Router.router(vertx);

    //前置请求处理
    /*router.route().handler(context->{
      final String accessToken = context.request().getHeader("accessToken");
      if(accessToken == null){
        ToolClient.getResponse(context).end("无权限操作!");
      }
      if("myToken".contains(accessToken)){
        context.next();
      }else{
        ToolClient.getResponse(context).end("无权限操作!");
      }
    });*/
    router.get("/api/v1.0/eventBus").handler(this::eventBus);// http://127.0.0.1:803/api/v1.0/eventBus
    router.get("/api/v1.0/eventBusName/:name").handler(this::eventBusName);// http://192.168.3.108:803/api/v1.0/eventBusName/fwtai

    final ConfigStoreOptions config = new ConfigStoreOptions()
      .setType("file")
      .setFormat("json")
      .setConfig(new JsonObject().put("path","config.json"));//当然也可以再创建一个
    final ConfigRetrieverOptions opts = new ConfigRetrieverOptions()
      .addStore(config);//当然可以根据上面再创建多个可以添加多个
    final ConfigRetriever cfgRetrieve = ConfigRetriever.create(vertx,opts);

    router.route().handler(StaticHandler.create("web"));//指定root根目录,默认访问路径: http://192.168.3.108:803/

    //方式1,参数类型:void getConfig(Handler<AsyncResult<JsonObject>> completionHandler);//都是函数接口类型,ok
    /*cfgRetrieve.getConfig(asyncResult ->{
      this.configHandle(start,router,asyncResult);
    });*/

    //方式2,参数类型:void getConfig(Handler<AsyncResult<JsonObject>> completionHandler);//都是函数接口类型
    final Handler<AsyncResult<JsonObject>> handler = asyncResult -> configHandle(start,router,asyncResult);
    cfgRetrieve.getConfig(handler);
  }

  protected void configHandle(final Promise<Void> start,final Router router,final AsyncResult<JsonObject> asyncResult){
    if(asyncResult.succeeded()){
      final JsonObject jsonObject = asyncResult.result();//请注意json文件格式数据,{"http":{"port":803}}
      final JsonObject http = jsonObject.getJsonObject("http");// {"port":803}
      final Integer httpPort = http.getInteger("port",801);
      vertx.createHttpServer().requestHandler(router).listen(httpPort);
      start.complete();
    }else{
      start.fail("应用启动失败");
    }
  }

  //方法的参数类型,blockingHandler(Handler<RoutingContext> requestHandler)
  protected void eventBus(final RoutingContext context){
    vertx.eventBus().request("hello.vertx.addr","",reply->{
      ToolClient.getResponse(context).end("EventBus,"+reply.result().body());
    });
  }

  //方法的参数类型,blockingHandler(Handler<RoutingContext> requestHandler)
  protected void eventBusName(final RoutingContext context){
    final String name = context.pathParam("name");
    vertx.eventBus().request("hello.named.addr",name,reply->{
      ToolClient.getResponse(context).end("EventBus,"+reply.result().body());
    });
  }
}