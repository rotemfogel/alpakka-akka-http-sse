package me.rotemfo.violations.serde

import java.util

import javax.xml.bind.DatatypeConverter
import me.rotemfo.violations.Violation
import me.rotemfo.violations.serde.ViolationDeserializer.schema
import org.apache.avro.io.DecoderFactory
import org.apache.avro.specific.SpecificDatumReader
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Deserializer
import org.slf4j.{Logger, LoggerFactory}

/**
  * project: akka-sse
  * package: me.rotemfo.violations
  * file:    ViolationDeserializer
  * created: 2019-08-01
  * author:  rotem
  */
class ViolationDeserializer extends Deserializer[Violation] {
  private final val logger: Logger = LoggerFactory.getLogger(getClass)

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {}

  override def deserialize(topic: String, data: Array[Byte]): Violation = {
    if (data == null)
      null
    else {
      try {
        logger.debug("serialized data='{}'", DatatypeConverter.printHexBinary(data))
        val datumReader = new SpecificDatumReader[Violation](schema)
        val decoder = DecoderFactory.get.binaryDecoder(data, null)
        val result = datumReader.read(null, decoder)
        logger.debug("result='{}'", result)
        result
      } catch {
        case ex: Exception =>
          ex.printStackTrace()
          throw new SerializationException(s"Can't deserialize data '${data.mkString("")}' from topic '$topic'", ex)
      }
    }
  }

  override def close(): Unit = {}
}

object ViolationDeserializer {
  val schema = new Violation().getSchema
}