import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

object KafkaProducerDrone {
  def main(args: Array[String]): Unit = {
    // Print hello
    println("Hello, world!")

    // Create Kafka producer
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put(
      "key.serializer",
      "org.apache.kafka.common.serialization.StringSerializer"
    )
    props.put(
      "value.serializer",
      "org.apache.kafka.common.serialization.StringSerializer"
    )

    val producer = new KafkaProducer[String, String](props)

    val position = "latitude: 37.7749, longitude: -122.4194"
    val number = 42

    val record = new ProducerRecord[String, String](
      "drone-message",
      position,
      number.toString
    )
    producer.send(record)

    producer.close()
  }
}
