import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.sql.functions._
//import org.apache.spark.sql.functions.{avg, col, desc, explode}
import org.apache.spark.sql.expressions.Window
//import org.apache.spark.sql.functions.{first, last}

object FromTextFile {
  def doAnalysis(df: DataFrame): Unit = {
    // Analyse data

    // First analysis
    // plot the average score by citizen
    println("First analysis")
    println("average score by citizen")
    val avg_score_by_citizen = df.groupBy("citizen").agg(avg("score").as("avg_score"))
    avg_score_by_citizen.show(false)

    // and plot the average score by location
    println("average score by location")
    val avg_score_by_location = avg_score_by_citizen.join(df, "citizen")
      .groupBy("location").agg(avg("avg_score").as("avg_location_score"))
      .orderBy(desc("avg_location_score"))
    avg_score_by_location.show(false)

    // Second analysis
    // plot the words used in places with low harmony score
    println("Second analysis")
    println("words used in places with low harmony score")
    val words_below_threshold = df.filter(col("score") > 25)
      .select(col("citizen"), explode(col("words")).as("word"))
      .groupBy("word").count()
      .orderBy(desc("count"))

    words_below_threshold.show(false)

    // Third analysis
    // plot the evolution of the score for each citizen
    println("Third analysis")
    println("evolution of the score for each citizen")
    val first_report = df.withColumn("rn", row_number().over(Window.partitionBy("citizen").orderBy("timestamp")))
      .where(col("rn") === 1).drop("rn").select(col("citizen"), col("score").as("first_score"))

    val last_report = df.withColumn("rn", row_number().over(Window.partitionBy("citizen").orderBy(col("timestamp").desc)))
      .where(col("rn") === 1).drop("rn").select(col("citizen"), col("score").as("last_score"))

    val score_evolution = first_report.join(last_report, "citizen")
      .withColumn("score_diff", col("last_score") - col("first_score"))
  
    score_evolution.show(false)

    // Fourth analysis
    // plot for each citizen all the drones that have been in the same location
    println("Fourth analysis")
    println("drones that have been in the same location than each citizen")
    val drones_by_citizen = df.select(col("citizen"), col("drone_id"), col("location"))
      .groupBy("citizen").agg(collect_set("drone_id").as("drones"))

    drones_by_citizen.show(false)
  }


  def main(args:Array[String]):Unit= {

    val spark: SparkSession = SparkSession.builder()
      .master("local[1]")
      .appName("readerExample")    
      .config("spark.hadoop.fs.s3a.aws.credentials.provider", "org.apache.hadoop.fs.s3a.SimpleAWSCredentialsProvider")
      .config("spark.hadoop.fs.s3a.access.key", sys.env("AWS_ACCESS_KEY_ID"))
      .config("spark.hadoop.fs.s3a.secret.key", sys.env("AWS_SECRET_ACCESS_KEY"))
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    //read multiple files
    val df = spark.read.parquet(
      "s3a://coartixbucket/"
      )
    //df.show(false)
    
    val explodedDF = df
      .select(
        col("id").as("drone_id"),
        col("location"),
        col("timestamp"),
        col("words"),
        explode(arrays_zip(col("citizens"), col("score"))).as("values")
      )
      .select(
        col("drone_id"),
        col("location"),
        col("timestamp"),
        col("words"),
        col("values.citizens").as("citizen"),
        col("values.score").as("score")
      )

    println("data from parquet")
    explodedDF.show(false)


    // analyse data
    doAnalysis(explodedDF)

    // close spark session
    spark.stop()
  }
}
