import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.sql.functions._

object FromTextFile {
  def doAnalysis(df: DataFrame): Unit = {
    // Analyse data
    
    // first analysis
    // compute the average score of each citizen
    val avg_score = df
      .groupBy("citizen")
      .agg(avg("score"))
      .withColumnRenamed("avg(score)", "avg_score")
      .sort(desc("avg_score"))

    avg_score.show(false)

    // and sort every location by the average score of its citizens
    val avg_score_by_location = df
      .groupBy("location")
      .agg(avg("score"))
      .withColumnRenamed("avg(score)", "avg_score")
      .sort(desc("avg_score"))

    avg_score_by_location.show(false)

    // second analysis
    // show words used by citizen with score < 0.25 and the number of times they used it
    val words_below_threshold = df
    .filter(col("score") < 25)
    .groupBy("word")
    .agg(count("word").as("times_used"))
    .sort(desc("times_used"))

    words_below_threshold.show(false)

    // third analysis
    // 
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
    val df = spark.read.json(
      "s3a://coartixbucket/"
      )
    df.show(false)
    
    val exploded_citizens = df.select(col("id"), col("location"), posexplode(col("citizens"))).withColumnRenamed("col", "citizen")
    val exploded_scores = df.select(col("id"), posexplode(col("score"))).withColumnRenamed("col", "score")
    val exploded_words = df.select(col("id"), posexplode(col("words"))).withColumnRenamed("col", "word")

    val joined_df = exploded_citizens
      .join(exploded_scores, Seq("id", "pos"))
      .join(exploded_words, Seq("id", "pos"))
      .drop("pos")

    joined_df.show(false)

    // analyse data
    doAnalysis(joined_df)

    // close spark session
    spark.stop()
  }
}
