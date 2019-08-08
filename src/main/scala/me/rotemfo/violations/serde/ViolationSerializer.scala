package me.rotemfo.violations.serde

import java.io.{ByteArrayOutputStream, IOException}
import java.util

import javax.xml.bind.DatatypeConverter
import me.rotemfo.violations.Violation
import org.apache.avro.generic.GenericDatumWriter
import org.apache.avro.io.EncoderFactory
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Serializer
import org.slf4j.{Logger, LoggerFactory}

/**
  * project: akka-sse
  * package: me.rotemfo.violations
  * file:    ViolationSerializer
  * created: 2019-08-01
  * author:  rotem
  */
class ViolationSerializer extends Serializer[Violation] {
  private final val logger: Logger = LoggerFactory.getLogger(getClass)

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {}

  override def serialize(topic: String, data: Violation): Array[Byte] = {
    try {
      if (data != null) {
        logger.debug("data='{}'", data)

        val baos = new ByteArrayOutputStream()
        val binaryEncoder = EncoderFactory.get().binaryEncoder(baos, null)

        val datumWriter = new GenericDatumWriter[Violation](ViolationDeserializer.schema)
        datumWriter.write(data, binaryEncoder)

        binaryEncoder.flush()
        baos.close()

        val result = baos.toByteArray()
        logger.debug("serialized data='{}'", DatatypeConverter.printHexBinary(result))
        result
      }
      else null
    } catch {
      case io: IOException => throw new SerializationException(s"Can't serialize data='$data' for topic='$topic'", io)
    }
  }

  override def close(): Unit = {}
}
