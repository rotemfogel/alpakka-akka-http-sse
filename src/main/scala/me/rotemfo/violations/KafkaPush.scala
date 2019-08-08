package me.rotemfo.violations

import java.time.Duration

import me.rotemfo.violations.serde.ViolationSerializer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

import scala.util.Random

/**
  * project: akka-sse
  * package: me.rotemfo.violations
  * file:    KafkaPush
  * created: 2019-08-01
  * author:  rotem
  */
object KafkaPush extends App {
  private final val VIOLATION: String = "violation#"
  private final val random: Random = new Random()
  val producer = new KafkaProducer[String, Violation](new java.util.HashMap[String, AnyRef]() {
    {
      put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
      put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
      put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[ViolationSerializer])
    }
  })
  val serviceId = System.currentTimeMillis()
  try {
    while (true) {
      for (i <- 1 to 100) {
        val v = Violation.newBuilder()
          .setId(i)
          .setName(s"$VIOLATION$i")
          .setSeverity(random.nextInt(3) + 1)
          .setServiceid(serviceId)
          .build()
        producer.send(new ProducerRecord[String, Violation]("violations", serviceId.toString, v))
        println(s"pushed violation #$i")
        Thread.sleep(5000)
      }
      Thread.sleep(5000)
    }
  } finally {
    producer.flush()
    producer.close(Duration.ofMillis(1000))
  }
}
