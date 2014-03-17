package com.geeksville.dapi.test

import akka.actor.Actor
import akka.actor.ActorLogging
import com.geeksville.apiproxy.GCSHooksImpl
import com.geeksville.util.Using._
import java.io.BufferedInputStream
import akka.actor.Props
import com.geeksville.mavlink.TlogStreamReceiver
import com.geeksville.mavlink.MavlinkEventBus
import com.geeksville.apiproxy.LiveUploader

case object RunTest

/**
 * An integration test that calls into the server as if it was a GCS/vehicle client
 */
class SimGCSClient extends Actor with ActorLogging {
  def receive = {
    case RunTest =>
      log.error("Running test")
      fullTest()
  }

  private def quickTest() {
    using(new GCSHooksImpl()) { webapi =>
      webapi.loginUser("test-bob@3drobotics.com", "sekrit");
      webapi.flush();

      val interfaceNum = 0;
      val sysId = 1;
      webapi.setVehicleId("550e8400-e29b-41d4-a716-446655440000",
        interfaceNum, sysId, false);

      // webapi.filterMavlink(interfaceNum, payload);

      log.info("Test successful")
    }
  }

  /**
   * Creates an fake vehicle which actually calls up and sends real TLOG data/accepts commands
   *
   * FIXME: Add support for accepting commands
   * FIXME: Don't use the old MavlinkEventBus global
   */
  private def fullTest() {
    log.info("Starting full test vehicle")
    val s = new BufferedInputStream(getClass.getResourceAsStream("test.tlog"), 8192)

    val tlog = context.actorOf(Props(TlogStreamReceiver.open(s)), "tlogsim")

    // Anything coming from the controller app, forward it to the serial port
    val groundControlId = 253 // FIXME
    MavlinkEventBus.subscribe(tlog, groundControlId)

    LiveUploader.create(context, isLive = false)
  }
}