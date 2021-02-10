package com.fwtai.example;

import com.fwtai.tool.ToolClient;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CSRFHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.ClusteredSessionStore;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import org.flywaydb.core.api.FlywayException;

/**
 * Vert.x 异步协调|异步处理|多语言编程实现
 * doConfig();
 *  compose(this::storeConfig);
 *  compose(this::doDatabaseMigrations);
 *  compose(this::configRouter);
 *  compose(this::startHttpServer);
 *  compose(this::deployOtherVerticles);
 *  setHandler(start::handle);
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2021-02-08 9:36
 * @QQ号码 444141300
 * @Email service@dwlai.com
 * @官网 http://www.fwtai.com
*/
public final class VertxAsyn extends AbstractVerticle{

  final JsonObject loadedConfig = new JsonObject();

  @Override
  public void start(final Promise<Void> start){
    //步骤1
    doConfig()
      //步骤2
      .compose(this::storeConfig)
      //步骤3
      .compose(this::doDatabaseMigrations)
      //步骤4
      .compose(this::configRouter)
      //步骤5
      .compose(this::startHttpServer)
      //步骤6,部署其他的
      .compose(this::deployOtherVerticles)
      //步骤7,启动
      .setHandler(start::handle);//最后调用的是本身的start()方法

    //vertx.deployVerticle("Api.groovy");//ok,todo 请勿删除
    //vertx.deployVerticle("Api.js");//ok,todo 请勿删除
  }

  //方法的参数类型,blockingHandler(Handler<RoutingContext> requestHandler)
  protected void login(final RoutingContext context){
    ToolClient.getResponse(context).end("欢迎登录,使用java实现");
  }

  //步骤1
  protected Future<JsonObject> doConfig(){
    final ConfigStoreOptions config = new ConfigStoreOptions().setType("file").setFormat("json").setConfig(new JsonObject().put("path","config.json"));//当然也可以再创建一个
    final ConfigRetrieverOptions opts = new ConfigRetrieverOptions().addStore(config);//当然可以根据上面再创建多个可以添加多个
    final ConfigRetriever cfgRetrieve = ConfigRetriever.create(vertx,opts);
    return Future.future(cfgRetrieve::getConfig);//完整写法: return Future.<JsonObject> future(promise->cfgRetrieve.getConfig(promise));
  }

  //步骤2,初始化配置,是否有报错???
  protected Future<Void> storeConfig(final JsonObject config){
    loadedConfig.mergeIn(config);
    return Promise.<Void>succeededPromise().future();
  }

  //步骤3,配置数据库
  protected Future<Void> doDatabaseMigrations(final Void unused){
    final JsonObject jsonObject = loadedConfig.getJsonObject("db");
    final String url = jsonObject.getString("url","jdbc:mysql://192.168.3.66:3306/security_backend?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowMultiQueries=true&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true");
    final String user = jsonObject.getString("userName","root");
    final String password = jsonObject.getString("password","rootFwtai");
    /*final Flyway flyway = Flyway.configure().dataSource(url,user,password).load();
    final Promise<Void> promise = Promise.promise();
    try {
      flyway.migrate();
      promise.complete();
    } catch (final FlywayException fe) {
      promise.fail(fe);
    }
    return promise.future();*/

    try {
      //flyway.migrate();
      return Promise.<Void> succeededPromise().future();
    } catch (final FlywayException fe) {
      fe.printStackTrace();
      return Promise.<Void> failedPromise(fe).future();
    }
  }

  //步骤4
  protected Future<Router> configRouter(final Void unused){
    final Router router = Router.router(vertx);
    final SessionStore session1 = LocalSessionStore.create(vertx);//ok,当然也可以使用下面的方式创建!!!
    final SessionStore session2 = ClusteredSessionStore.create(vertx);//ok
    //Session
    router.route().handler(SessionHandler.create(session1));// BodyHandler.create(),支持文件上传!!!
    router.route().handler(CorsHandler.create("127.0.0.1"));
    router.route().handler(CSRFHandler.create("RjF9vTHCS2yr0zX3D50CKRiarMX+0qOpHAfcu24gWZ9bL39s48euPQniE2RhGx"));//自定义参数,高版本有2个参数,低版本只有1个参数
    router.get("/api/v1.0/login").handler(this::login);// http://127.0.0.1:803/api/v1.0/login
    router.route().handler(StaticHandler.create("web"));//指定root根目录,默认访问路径: http://192.168.3.108:803/
    return Promise.succeededPromise(router).future();
  }

  //步骤5,有参数,怎么传递?是否报错???
  protected Future<HttpServer> startHttpServer(final Router router){
    final JsonObject http = loadedConfig.getJsonObject("http");// {"port":803}
    final Integer httpPort = http.getInteger("port",801);
    final HttpServer server = vertx.createHttpServer().requestHandler(router);
    return Future.<HttpServer>future(promise -> server.listen(httpPort,promise));
  }

  //步骤6
  protected Future<Void> deployOtherVerticles(final HttpServer server){
    vertx.deployVerticle(new UserVerticle());
    final Future<String> helloGroovy = Future.future(promise -> vertx.deployVerticle("Hello.groovy",promise));
    final Future<String> helloJavascript = Future.future(promise -> vertx.deployVerticle("Hello.js",promise));

    final Future<String> apiGroovy = Future.future(promise -> vertx.deployVerticle("Api.groovy",promise));
    final Future<String> apiJavascript = Future.future(promise -> vertx.deployVerticle("Api.js",promise));
    return CompositeFuture.all(helloGroovy,helloJavascript,apiGroovy,apiJavascript).mapEmpty();
  }
}