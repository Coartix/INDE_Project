import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.sql.functions._

object FromTextFile {
  def doAnalysis(df: DataFrame): Unit = {
    // Analyse data
    
    // first analysis
    // compute the average score of each citizen
    //val avg_score_by_citizen = ...

    //avg_score_by_citizen.show(false)

    // and sort every location by the average score of its citizens
    //val avg_score_by_location = ...

    //avg_score_by_location.show(false)

    // second analysis
    // show words used by citizen with score < 0.25 and the number of times they used it
    //val words_below_threshold = ...

    //words_below_threshold.show(false)

    // third analysis
    // show for each citizen their score evolution from the first timestamp to the last
    //val score_evolution = ...

    //score_evolution.show(false)

    // fourth analysis
    
  }


  def main(args:Array[String]):Unit= {

    val spark: SparkSession = SparkSession.builder()
      .master("local[1]")
      .appName("readerExample")    
      .config("spark.hadoop.fs.s3a.aws.credentials.provider", "org.apache.hadoop.fs.s3a.SimpleAWSCredentialsProvider")
      .config("spark.hadoop.fs.s3a.access.key", sys.env("AWS_ACCESS_KEY_ID"))
      .config("spark.hadoop.fs.s3a.secret.key", sys.env("AWS_SECRET_ACCESS_KEY"))
      .getOrCreate()

    //read multiple files
    val df = spark.read.parquet(
      "s3a://coartixbucket/"
      )
    df.show(false)

    // analyse data
    doAnalysis(df)

    // close spark session
    spark.stop()
  }
}
