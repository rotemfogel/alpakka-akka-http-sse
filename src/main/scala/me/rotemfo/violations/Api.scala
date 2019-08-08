package me.rotemfo.violations

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.{Directives, Route}
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import io.circe.syntax._
import me.rotemfo.violations.filter.ViolationsStreamFilters
import me.rotemfo.violations.serde.ViolationDeserializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * project: akka-sse
  * package:
  * file:    me.rotemfo.violations
  * created: 2019-07-29
  * author:  rotem
  */
final class Api(val address: String,
                val port: Int,
                val violationsFacadeTimeout: FiniteDuration,
                val eventBufferSize: Int,
                val eventHeartbeat: FiniteDuration)(implicit val system: ActorSystem) {

  implicit val mat: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system))

  import system.dispatcher

  private final val log: Logger = LoggerFactory.getLogger(getClass)

  val bindingFuture: Future[Http.ServerBinding] = Http(system)
    .bindAndHandle(route(), address, port)

  bindingFuture.onComplete(
    {
      case Failure(ex) =>
        log.error(ex.getMessage, ex)
        system.terminate()
      case Success(_) =>
        log.info("API Listening on {}", address)
    }
  )

  private final val consumerSettings =
    ConsumerSettings(system, new StringDeserializer, new ViolationDeserializer)
      .withBootstrapServers("localhost:9092")
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
      .withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000")


  private def subscribe(violationFilter: ViolationsStreamFilters) = {
    // copy config
    val config = consumerSettings.withProperty(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID.toString)
    Consumer
      .plainSource[String, Violation](config, Subscriptions.topics("violations"))
      .map(r => {
        val v = violationFilter.filter(r.value())
        if (v.isDefined) ServerSentEvent(v.get.asJson.noSpaces) else ServerSentEvent.heartbeat
      })
      .keepAlive(1.seconds, () => ServerSentEvent.heartbeat)
  }

  private def route(): Route = Route.seal {

    import Directives._
    import EventStreamMarshalling._

    parameters(
      (
        'serviceId.as[Long].?,
        'unitId.as[String].?,
        'violationTypeId.as[Int].?,
        'violationScopeId.as[String].?,
        'severity.as[Int].?
      )
    ).as(ViolationsStreamFilters) { filter => // filter =>
      path("stream") {
        get {
          complete(subscribe(filter))
        }
      }
    }
  }
}