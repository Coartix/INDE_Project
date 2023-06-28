import org.apache.spark.sql.{SparkSession, Encoders}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import java.sql.Timestamp
import play.api.libs.json._
import play.api.libs.functional.syntax._

import java.time.{Instant}

// Define the Report case class outside of main function, same as your original code
  case class Report(id: Int, location: List[Double], citizens: List[String], score: List[Int], words: List[String], timestamp: Instant)

  object Report {
    implicit val reportFormat: Format[Report] = (
      (JsPath \ "id").format[Int] and
      (JsPath \ "location").format[List[Double]] and
      (JsPath \ "citizens").format[List[String]] and
      (JsPath \ "score").format[List[Int]] and
      (JsPath \ "words").format[List[String]] and
      (JsPath \ "timestamp").format[Instant]
    )(Report.apply, unlift(Report.unapply))
  }

object KafkaSparkConsumerStore {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder
    .master("local[2]")
    .appName("KafkaSparkConsumerStore")
    .getOrCreate()
    import spark.implicits._

    // Create DataFrame representing the stream of input lines from kafka
    val df = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "drone-message")
      .option("startingOffsets", "earliest") // Start reading from the earliest message
      .load()

    // Cast the value in the dataframe to string
    val stringDF = df.selectExpr("CAST(value AS STRING)").as[String]

    // Define the schema using the case class
    val schema = Encoders.product[Report].schema

    // Deserialize the JSON to a DataFrame
    // val reportDF = spark.read.schema(schema).json(stringDF)
    val reportDF = stringDF.select(from_json($"value", schema).as("data")).select("data.*")
    // reportDF.show(false)

    // Write the DataFrame to S3 in parquet format
    reportDF.writeStream
      .outputMode("append")
      .format("parquet")
      .option("path", "s3a://coartixbucket/")
      .option("checkpointLocation", "/tmp/checkpoints/")
      .trigger(Trigger.ProcessingTime("10 seconds"))
      .start()
      .awaitTermination()
  }
}
