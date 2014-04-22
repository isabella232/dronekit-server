package com.geeksville.dapi

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import com.geeksville.util.TCPListener
import akka.actor.Props
import com.geeksville.apiproxy.APIConstants
import com.geeksville.akka.TCPListenerActor
import java.io.File
import com.geeksville.akka.MockAkka

object Global {
  def setConfig() {
    val configOverride = new File(System.getProperty("user.home") + "/nestor.conf")
    if (configOverride.exists)
      System.setProperty("config.file", configOverride.toString)
    else
      println(s"No config override file found.  You should probably create $configOverride")

    val cfg = ConfigFactory.load()
    MockAkka.configOverride = Some(cfg)
    //println(cfg.getString("dapi.threescale.serviceId"))
  }
}