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
import io.vertx.ext.web.handler.CSRFHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.ClusteredSessionStore;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;

/**
 * Vert.x Chained Routes And Static Handlers
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2021-02-08 9:36
 * @QQ号码 444141300
 * @Email service@dwlai.com
 * @官网 http://www.fwtai.com
*/
public final class VertxSession extends AbstractVerticle{

  @Override
  public void start(final Promise<Void> start){
    final Router router = Router.router(vertx);
    final SessionStore session1 = LocalSessionStore.create(vertx);//ok,当然也可以使用下面的方式创建!!!
    final SessionStore session2 = ClusteredSessionStore.create(vertx);//ok
    //???
    router.route().handler(LoggerHandler.create());
    //Session
    router.route().handler(SessionHandler.create(session1));// BodyHandler.create(),支持文件上传!!!
    router.route().handler(CorsHandler.create("127.0.0.1"));
    router.route().handler(CSRFHandler.create(vertx,"RjF9vTHCS2yr0zX3D50CKRiarMX+0qOpHAfcu24gWZ9bL39s48euPQniE2RhGx"));//自定义参数,高版本有2个参数,低版本只有1个参数
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
    router.get("/api/v1.0/eventBus").handler(this::corsCSRF);// http://127.0.0.1:803/api/v1.0/eventBus

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
  protected void corsCSRF(final RoutingContext context){
    ToolClient.getResponse(context).end("Session|Cors|CSRF示例代码");
  }
}