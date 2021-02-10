package com.fwtai.example;

import io.vertx.core.AbstractVerticle;

/**
 * Vert.x  Hello EventBus
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2021-02-08 9:36
 * @QQ号码 444141300
 * @Email service@dwlai.com
 * @官网 http://www.fwtai.com
*/
public final class VertxEventBus extends AbstractVerticle{

  @Override
  public void start(){
    vertx.eventBus().consumer("hello.vertx.addr",message->{
      message.reply("hello vert.x world");//用于 Hello.js 调用或 com.fwtai.example.VertxRouter 的方法eventBus()调用
    });
    vertx.eventBus().consumer("hello.named.addr",message->{
      final String name = (String) message.body(); //用于 Hello.groovy 调用 或 com.fwtai.example.VertxRouter的方法 eventBusName() 调用
      message.reply(name);
    });
  }
}