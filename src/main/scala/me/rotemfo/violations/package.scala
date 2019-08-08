package me.rotemfo

import java.time.{Duration => JavaDuration}

import io.circe.{Decoder, Encoder, HCursor, Json}

import scala.concurrent.duration.{FiniteDuration, NANOSECONDS}
import scala.language.implicitConversions

/**
  * project: akka-sse
  * package: me.rotemfo.reactiveflows
  * file:    reactiveflows
  * created: 2019-07-29
  * author:  rotem
  */
package object violations {

  implicit def javaDurationToScala(duration: JavaDuration): FiniteDuration =
    FiniteDuration(duration.toNanos, NANOSECONDS)

  implicit val violationsEncoder: Encoder[Violation] = (violation: Violation) => {
    var fields: Seq[(String, Json)] = Seq(
      ("id", Json.fromInt(violation.getId)),
      ("name", Json.fromString(violation.getName)),
      ("severity", Json.fromInt(violation.getSeverity))
    )
    if (violation.getServiceid != null)
      fields ++= Seq(("serviceId", Json.fromLong(violation.getServiceid)))
    if (violation.getUnitid != null)
      fields ++= Seq(("unitId", Json.fromString(violation.getUnitid)))
    if (violation.getViolationtypeid != null)
      fields ++= Seq(("violationTypeId", Json.fromInt(violation.getViolationtypeid)))
    if (violation.getViolationscopeid != null)
      fields ++= Seq(("violationScopeId", Json.fromString(violation.getViolationscopeid)))
    Json.fromFields(fields)
  }

  implicit val violationsDecoder: Decoder[Violation] = (c: HCursor) => {
    for {
      id <- c.downField("id").as[Int]
      name <- c.downField("name").as[String]
      severity <- c.downField("severity").as[Int]
      serviceId <- c.downField("serviceid").as[Option[Long]]
      unitId <- c.downField("unitid").as[Option[String]]
      violationTypeId <- c.downField("violationtypeid").as[Option[Int]]
      violationScopeId <- c.downField("violationscopeid").as[Option[String]]
    } yield {
      Violation.newBuilder()
        .setId(id)
        .setName(name)
        .setSeverity(severity)
        .setServiceid(if (serviceId.isDefined) serviceId.get else null)
        .setUnitid(if (unitId.isDefined) unitId.get else null)
        .setViolationtypeid(if (violationTypeId.isDefined) violationTypeId.get else null)
        .setViolationscopeid(if (violationScopeId.isDefined) violationScopeId.get else null)
        .build()
    }
  }
}
