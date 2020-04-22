package com.zuel.syzc.spark.test

import org.apache.spark
import org.apache.spark.SparkConf
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.ml.evaluation.ClusteringEvaluator
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.junit
import org.junit.Test

class SparkSqlTest {


  def readJsonFile(path: String): (SparkSession, DataFrame) = {
    val sparkConf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("test")
    val spark: SparkSession = SparkSession.builder.config(sparkConf).getOrCreate
    val df: DataFrame = spark.read
      .option("multiLine", value = true).option("mode", "PERMISSIVE") // 加此行设置读取多行json文件
      .json(path)
    (spark, df)
  }

  @TestClass
  def sqlStyle(): Unit = {
    val (spark, df) = readJsonFile("test_data/test_json.json")
    // 对DataFrame创建一个临时表,临时表是Session范围内的，Session退出后，表就失效了。
    // 如果想应用范围内有效，可以使用全局表。注意使用全局表时需要全路径访问，如：global_temp.people
    df.createOrReplaceTempView("people")
    val sqlDF: DataFrame = spark.sql("SELECT * FROM people")
    sqlDF.show()
    // 对于DataFrame创建一个全局表
    df.createGlobalTempView("people")
    spark.sql("SELECT * FROM global_temp.people").show()
    spark.newSession().sql("SELECT * FROM global_temp.people").show()
  }

  @TestClass
  def dslStyle(): Unit = {
    val (spark, df) = readJsonFile("test_data/test_json.json")
    import spark.implicits._
    // 查看DataFrame的Schema信息
    df.printSchema()
    // 只查看”name”列数据
    df.select("name").show()
    // 查看”name”列数据以及”age+1”数据
    df.select($"name", $"age" + 1).show()
    // 查看”age”大于”21”的数据
    df.filter($"age" > 21).show()
    // 按照”age”分组，查看数据条数
    df.groupBy("age").count().show()

  }

  @junit.Test
  def toDataSet(): Unit = {
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("analysis")
    val spark = SparkSession.builder.config(sparkConf).getOrCreate
    import spark.implicits._
    spark.read.format("csv").option("header", "true").load("../in/服创大赛-基站经纬度数据.csv").createOrReplaceTempView("longitude")
    val dataFrame:Dataset[Row] = spark.sql("select * from longitude")
    dataFrame.show()

    val dataset: Dataset[StationPoints] = dataFrame.map {
      row => StationPoints(row.getString(0).toDouble, row.getString(1).toDouble, row.getString(2))
    }
    dataset.show()
    dataset.collect().foreach(x => println(x.toString))
    val dataset2: Dataset[StationPoints] = dataFrame.as[StationPoints]
    dataset2.foreach(x => println(x.toString))
    //val dataset = dataFrame.map(doc => StationPoints())
    //dataset.collect().foreach(println)

  }

  @junit.Test
  def testKMeans(): Unit = {
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("analysis")
    val spark = SparkSession.builder.config(sparkConf).getOrCreate
    // Loads data.
    val dataset = spark.read.format("csv").load("../in/服创大赛-基站经纬度数据.csv")

    // Trains a k-means model.
    val kmeans = new KMeans().setK(2).setSeed(1L)
    val model = kmeans.fit(dataset)

    // Make predictions
    val predictions = model.transform(dataset)

    // Evaluate clustering by computing Silhouette score
    val evaluator = new ClusteringEvaluator()

    val silhouette = evaluator.evaluate(predictions)
    println(s"Silhouette with squared euclidean distance = $silhouette")

    // Shows the result.
    println("Cluster Centers: ")
    model.clusterCenters.foreach(println)

  }

}
