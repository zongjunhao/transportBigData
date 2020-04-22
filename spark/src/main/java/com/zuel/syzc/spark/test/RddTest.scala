package com.zuel.syzc.spark.test

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.{HashPartitioner, SparkConf, SparkContext}
import org.junit.Test

class RddTest {
  val sc: SparkContext = getSparkContext

  private def getSparkContext: SparkContext = {
    // 两种方式创建SparkContext
    val conf: SparkConf = new SparkConf().setMaster("local").setAppName("My App")
    val sc = new SparkContext(conf)
    //  val sc1 = new SparkContext("master","appName")
    //  val spark: SparkSession = SparkSession.builder.config(conf).getOrCreate
    //  return sc
    //  还可以不加return关键字
    sc
  }

  @Test
  def rddCreate(): Unit = {
    val rdd: RDD[Int] = sc.parallelize(Array(1, 2, 3, 4, 5, 6, 7, 8))
    val iterator = rdd.toLocalIterator
    while (iterator.hasNext) {
      println(iterator.next())
    }
    val rdd2: RDD[Int] = sc.makeRDD(Array(1, 2, 3, 4, 5, 6, 7, 8))
    val iterator2 = rdd2.toLocalIterator
    while (iterator2.hasNext) {
      println(iterator2.next())
    }
  }


  // ------------------------------------------------value类型-----------------------------------------------------------
  @Test
  def functionMap(): Unit = {
    val source: RDD[Int] = sc.parallelize(1 to 10)
    printRdd(source, " ")
    val mapAdd: RDD[Int] = source.map(_ * 2)
    printRdd(mapAdd, " ")
  }

  /**
   * 二者区别
   * map(): 每次处理一条数据
   * mapPartition()：每次处理一个分区的数据，这个分区的数据处理完后，原RDD中分区的数据才能释放，可能导致OOM。
   * 开发指导：当内存空间较大的时候建议使用mapPartition()，以提高处理效率。
   */
  @Test
  def functionMapPartitions(): Unit = {
    val rdd: RDD[Int] = sc.parallelize(Array(1, 2, 3, 4))
    val result: RDD[Int] = rdd.mapPartitions(x => x.map(_ * 2))
    printRdd(result)
  }

  @Test
  def functionMapPartitionsWithIndex(): Unit = {
    val rdd: RDD[Int] = sc.parallelize(Array(1, 2, 3, 4))
    //使每个元素跟所在分区形成一个元组组成一个新的RDD
    val indexRdd = rdd.mapPartitionsWithIndex((index, items) => items.map((index, _)))
    printRdd(indexRdd)
  }

  @Test
  def functionFlatMap(): Unit = {
    val sourceFlat = sc.parallelize(1 to 5)
    for (x <- sourceFlat)
      print(x + " ")
    println()
    // 根据原RDD创建新RDD（1->1,2->1,2……5->1,2,3,4,5）
    val flatMap = sourceFlat.flatMap(1 to _)
    for (x <- flatMap)
      print(x + " ")
  }


  @Test
  // 将每一个分区形成一个数组，形成新的RDD类型时RDD[Array[T]]
  def functionGlom(): Unit = {
    val rdd = sc.parallelize(1 to 16, 4)
    for (x <- rdd)
      print(x + " ")
    println()
    val result = rdd.glom()
    glomPrint(result)
  }

  /**
   * 分组，按照传入函数的返回值进行分组。将相同的key对应的值放入一个迭代器。
   */
  @Test
  def functionGroupBy(): Unit = {
    val rdd = sc.parallelize(1 to 4)
    val group = rdd.groupBy(_ % 2)
    printRdd(group)
  }

  /**
   * 过滤。返回一个新的RDD，该RDD由经过func函数计算后返回值为true的输入元素组成。
   */
  @Test
  def functionFilter(): Unit = {
    val sourceFilter = sc.parallelize(Array("xiaoming", "xiaojiang", "xiaohe", "dazhi"))
    val filter = sourceFilter.filter(_.contains("xiao"))
    printRdd(filter)
  }

  /**
   * 以指定的随机种子随机抽样出数量为fraction的数据，withReplacement表示是抽出的数据是否放回
   * true为有放回的抽样，false为无放回的抽样，seed用于指定随机数生成器种子。
   */
  @Test
  def functionSample(): Unit = {
    val rdd = sc.parallelize(1 to 10)
    val sample1 = rdd.sample(withReplacement = true, 0.4, 2)
    printRdd(sample1)
    val sample2 = rdd.sample(withReplacement = false, 0.2, 3)
    printRdd(sample2)
  }

  /**
   * 对源RDD进行去重后返回一个新的RDD。默认情况下，只有8个并行任务来操作，但是可以传入一个可选的numTasks参数改变它。
   */
  @Test
  def functionDistinct(): Unit = {
    val distinctRdd = sc.parallelize(List(1, 2, 1, 5, 2, 9, 6, 1))
    val unionRDD1 = distinctRdd.distinct()
    printRdd(unionRDD1)
    val unionRDD2 = distinctRdd.distinct(2)
    printRdd(unionRDD2)
  }

  /**
   * 缩减分区数，用于大数据集过滤后，提高小数据集的执行效率。
   */
  @Test
  def functionCoalesce(): Unit = {
    val rdd = sc.parallelize(1 to 16, 4)
    println(rdd.partitions.length)
    val result1 = rdd.glom()
    glomPrint(result1)
    val coalesceRDD = rdd.coalesce(3)
    println(coalesceRDD.partitions.length)
    val result2 = coalesceRDD.glom()
    glomPrint(result2)
  }

  /**
   * 根据分区数，重新通过网络随机洗牌所有数据。
   *
   * 1. coalesce重新分区，可以选择是否进行shuffle过程。由参数shuffle: Boolean = false/true决定。
   * 2. repartition实际上是调用的coalesce，默认是进行shuffle的。源码如下：
   * def repartition(numPartitions: Int)(implicit ord: Ordering[T] = null): RDD[T] = withScope {
   * coalesce(numPartitions, shuffle = true)
   * }
   */
  @Test
  def functionRepartition(): Unit = {
    val rdd = sc.parallelize(1 to 16, 4)
    println(rdd.partitions.length)
    val reRdd = rdd.repartition(2)
    println(reRdd.partitions.length)
    val result2 = reRdd.glom()
    glomPrint(result2)
  }

  /**
   * 使用func先对数据进行处理，按照处理后的数据比较结果排序，默认为正序。
   */
  @Test
  def functionSortBy(): Unit = {
    val rdd = sc.parallelize(List(2, 1, 3, 4))
    // 按照自身大小排序
    val result1: RDD[Int] = rdd.sortBy(x => x)
    printRdd(result1)
    val result2: RDD[Int] = rdd.sortBy(x => x % 3)
    printRdd(result2)
  }

  /**
   * 管道，针对每个分区，都执行一个shell脚本，返回输出的RDD。
   */
  @Test
  def functionPipe(): Unit = {
    val sparkConf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("analysis")
    val spark: SparkSession = SparkSession.builder.config(sparkConf).getOrCreate
    spark.read.format("csv").option("header", "true").load("in/服创大赛-基站经纬度数据.csv").createOrReplaceTempView("longitude")
    //val dataset: DataFrame = spark.sql("select * from longitude")

  }

  // -----------------------------------------------双value类型交互-------------------------------------------------------
  /**
   * 对源RDD和参数RDD求并集后返回一个新的RDD
   */
  @Test
  def functionUnion(): Unit = {
    val rdd1 = sc.parallelize(1 to 5)
    val rdd2 = sc.parallelize(5 to 10)
    val rdd3 = rdd1.union(rdd2)
    printRdd(rdd3, " ")
  }

  /**
   * 计算差的一种函数，去除两个RDD中相同的元素，不同的RDD将保留下来
   */
  @Test
  def functionSubtract(): Unit = {
    val rdd1 = sc.parallelize(3 to 8)
    val rdd2 = sc.parallelize(1 to 5)
    val rdd3 = rdd1.subtract(rdd2)
    printRdd(rdd3, " ")
  }

  /**
   * 对源RDD和参数RDD求交集后返回一个新的RDD
   */
  @Test
  def functionIntersection(): Unit = {
    val rdd1 = sc.parallelize(1 to 7)
    val rdd2 = sc.parallelize(5 to 10)
    val rdd3 = rdd1.intersection(rdd2)
    printRdd(rdd3, "")
  }

  /**
   * 笛卡尔积（尽量避免使用）
   */
  @Test
  def functionCartesian(): Unit = {
    val rdd1 = sc.parallelize(1 to 3)
    val rdd2 = sc.parallelize(2 to 5)
    val rdd3 = rdd1.cartesian(rdd2)
    printRdd(rdd3)
  }

  /**
   * 将两个RDD组合成Key/Value形式的RDD
   * 这里默认两个RDD的partition数量以及元素数量都相同，否则会抛出异常。
   */
  @Test
  def functionZip(): Unit = {
    val rdd1 = sc.parallelize(Array(1, 2, 3), 3)
    val rdd2 = sc.parallelize(Array("a", "b", "c"), 3)
    val rdd3 = rdd1.zip(rdd2)
    printRdd(rdd3, " ")
    val rdd4 = rdd2.zip(rdd1)
    printRdd(rdd4, " ")
    val rdd5 = sc.parallelize(Array("a", "b", "c"), 2)
    printRdd(rdd5, " ")
    val rdd6 = rdd1.zip(rdd5)
    printRdd(rdd6)
  }

  // -------------------------------------------Key-Value类型------------------------------------------------------------
  /**
   * 对pairRDD进行分区操作，如果原有的partitionRDD和现有的partitionRDD是一致的话就不进行分区， 否则会生成ShuffleRDD，即会产生shuffle过程。
   */
  @Test
  def functionPartitionBy(): Unit = {
    val rdd: RDD[(Int, String)] = sc.parallelize(Array((1, "aaa"), (2, "bbb"), (3, "ccc"), (4, "ddd")), 4)
    println(rdd.partitions.length)
    printRdd(rdd, " ")
    glomPrint(rdd.glom())
    val rdd2 = rdd.partitionBy(new HashPartitioner(2))
    println(rdd2.partitions.length)
    printRdd(rdd2, end = " ")
    glomPrint(rdd2.glom())
  }

  /**
   * groupByKey也是对每个key进行操作，但只生成一个sequence。
   */
  @Test
  def functionGroupByKey(): Unit = {
    val words: Array[String] = Array("one", "two", "two", "three", "three", "three")
    // 将相同key对应值聚合到一个sequence中
    val wordPairsRDD: RDD[(String, Int)] = sc.parallelize(words).map(word => (word, 1))
    val group: RDD[(String, Iterable[Int])] = wordPairsRDD.groupByKey()
    printRdd(group, " ")
    // 计算相同key对应值的相加结果
    val result: RDD[(String, Int)] = group.map(t => (t._1, t._2.sum))
    printRdd(result)
  }

  /**
   * 在一个(K,V)的RDD上调用，返回一个(K,V)的RDD，使用指定的reduce函数，
   * 将相同key的值聚合到一起，reduce任务的个数可以通过第二个可选的参数来设置。
   *
   * reduceByKey和groupByKey的区别
   * 1. reduceByKey：按照key进行聚合，在shuffle之前有combine（预聚合）操作，返回结果是RDD[k,v].
   * 2. groupByKey：按照key进行分组，直接进行shuffle。
   * 3. 开发指导：reduceByKey比groupByKey，建议使用。但是需要注意是否会影响业务逻辑。
   */
  @Test
  def functionReduceByKey(): Unit = {
    val rdd = sc.parallelize(List(("female", 1), ("male", 5), ("female", 5), ("male", 2)))
    // 计算相同key对应值的相加结果
    val reduce: RDD[(String, Int)] = rdd.reduceByKey((x, y) => x + y)
    printRdd(reduce)
  }

  /**
   * 参数：(zeroValue:U,[partitioner: Partitioner]) (seqOp: (U, V) => U,combOp: (U, U) => U)
   *
   * 1. 作用：
   * 在kv对的RDD中，按key将value进行分组合并，
   * 合并时，将每个value和初始值作为seq函数的参数，进行计算，
   * 返回的结果作为一个新的kv对，然后再将结果按照key进行合并，
   * 最后将每个分组的value传递给combine函数进行计算（先将前两个value进行计算，将返回结果和下一个value传给combine函数，以此类推），
   * 将key与计算结果作为一个新的kv对输出。
   *
   * 2. 参数描述：
   * （1）zeroValue：给每一个分区中的每一个key一个初始值；
   * （2）seqOp：函数用于在每一个分区中用初始值逐步迭代value；
   * （3）combOp：函数用于合并每个分区中的结果。
   *
   * 3. 需求：
   * 创建一个pairRDD，取出每个分区相同key对应值的最大值，然后相加
   */
  @Test
  def functionAggregateByKey(): Unit = {
    val rdd: RDD[(String, Int)] = sc.parallelize(List(("a", 3), ("a", 2), ("c", 4), ("b", 3), ("c", 6), ("c", 8)), 2)
    val agg: RDD[(String, Int)] = rdd.aggregateByKey(0)(math.max, _ + _)
    printRdd(agg)
    glomPrint(agg.glom())
  }

  /**
   * 参数：(zeroValue: V)(func: (V, V) => V): RDD[(K, V)]
   * 1.	作用：aggregateByKey的简化操作，seqOp和combOp相同
   * 2.	需求：创建一个pairRDD，计算相同key对应值的相加结果
   */
  @Test
  def functionFoldByKey(): Unit = {
    val rdd: RDD[(Int, Int)] = sc.parallelize(List((1, 3), (1, 2), (1, 4), (2, 3), (3, 6), (3, 8)), 3)
    val agg = rdd.foldByKey(0)(_ + _)
    printRdd(agg)
  }

  /**
   * 参数：(createCombiner: V => C,  mergeValue: (C, V) => C,  mergeCombiners: (C, C) => C)
   *
   * 1.	作用：
   * 对相同K，把V合并成一个集合。
   *
   * 2.	参数描述：
   * （1）createCombiner:
   * combineByKey()会遍历分区中的所有元素，因此每个元素的键要么还没有遇到过，要么就和之前的某个元素的键相同。
   * 如果这是一个新的元素,combineByKey()会使用一个叫作createCombiner()的函数来创建那个键对应的累加器的初始值
   * （2）mergeValue:
   * 如果这是一个在处理当前分区之前已经遇到的键，它会使用mergeValue()方法将该键的累加器对应的当前值与这个新的值进行合并
   * （3）mergeCombiners:
   * 由于每个分区都是独立处理的，因此对于同一个键可以有多个累加器。
   * 如果有两个或者更多的分区都有对应同一个键的累加器，就需要使用用户提供的 mergeCombiners()方法将各个分区的结果进行合并。
   * 3.	需求：
   * 创建一个pairRDD，根据key计算每种key的均值。（先计算每个key出现的次数以及可以对应值的总和，再相除得到结果）
   */
  @Test
  def functionCombineByKey(): Unit = {
    val input = sc.parallelize(Array(("a", 88), ("b", 95), ("a", 91), ("b", 93), ("a", 95), ("b", 98)), 2)
    val combine: RDD[(String, (Int, Int))] = input.combineByKey((_, 1),
      (acc: (Int, Int), v) => (acc._1 + v, acc._2 + 1),
      (acc1: (Int, Int), acc2: (Int, Int)) => (acc1._1 + acc2._1, acc1._2 + acc2._2))
    printRdd(combine, " ")
    val result: RDD[(String, Double)] = combine.map { case (key, value) => (key, value._1 / value._2.toDouble) }
    printRdd(result)
  }

  /**
   * 1. 作用：在一个(K,V)的RDD上调用，K必须实现Ordered接口，返回一个按照key进行排序的(K,V)的RDD
   * 2. 需求：创建一个pairRDD，按照key的正序和倒序进行排序
   */
  @Test
  def functionSortByKey(): Unit = {
    val rdd = sc.parallelize(Array((3, "aa"), (6, "cc"), (2, "bb"), (1, "dd")))
    // 按照key的正序
    val result1: RDD[(Int, String)] = rdd.sortByKey(ascending = true)
    // 按照key的倒序
    val result2: RDD[(Int, String)] = rdd.sortByKey(ascending = false)
    printRdd(result1, end = " ")
    printRdd(result2, end = " ")
  }

  /**
   * 1. 针对于(K,V)形式的类型只对V进行操作
   * 2. 需求：创建一个pairRDD，并将value添加字符串"|||"
   */
  @Test
  def functionMapValues(): Unit = {
    val rdd = sc.parallelize(Array((1, "a"), (1, "d"), (2, "b"), (3, "c")))
    printRdd(rdd.mapValues(_ + "|||"))
  }

  /**
   * 1. 作用：在类型为(K,V)和(K,W)的RDD上调用，返回一个相同key对应的所有元素对在一起的(K,(V,W))的RDD
   * 2. 需求：创建两个pairRDD，并将key相同的数据聚合到一个元组。
   */
  @Test
  def functionJoin(): Unit = {
    val rdd = sc.parallelize(Array((1, "a"), (2, "b"), (3, "c")))
    val rdd1 = sc.parallelize(Array((1, 4), (2, 5), (3, 6)))
    printRdd(rdd.join(rdd1))
  }

  /**
   * 1. 作用：在类型为(K,V)和(K,W)的RDD上调用，返回一个(K,(Iterable<V>,Iterable<W>))类型的RDD
   * 2. 需求：创建两个pairRDD，并将key相同的数据聚合到一个迭代器。
   */
  @Test
  def functionCoGroup(): Unit = {
    val rdd = sc.parallelize(Array((1, "a"), (2, "b"), (3, "c"), (3, "c")))
    val rdd1 = sc.parallelize(Array((1, 4), (2, 5), (3, 6)))
    val result: RDD[(Int, (Iterable[String], Iterable[Int]))] = rdd.cogroup(rdd1)
    printRdd(result)
  }

  /**
   * 数据结构：时间戳，省份，城市，用户，广告，中间字段使用空格分割。
   * 样本如下
   * 1516609143867 6 7 64 16
   * 1516609143869 9 4 75 18
   * 1516609143869 1 7 87 12
   * 需求：统计出每一个省份广告被点击次数的TOP3
   */
  @Test
  def practice(): Unit = {
    //1.初始化spark配置信息并建立与spark的连接
    //val sparkConf = new SparkConf().setMaster("local[*]").setAppName("Practice")
    //val sc = new SparkContext(sparkConf)

    //2.读取数据生成RDD：TS，Province，City，User，AD
    val line: RDD[String] = sc.textFile("test_data/agent.log")
    printRdd(line, " ")

    //3.按照最小粒度聚合：((Province,AD),1)
    val provinceAdToOne: RDD[((String, String), Int)] = line.map { x =>
      val fields: Array[String] = x.split(" ")
      ((fields(1), fields(4)), 1)
    }
    printRdd(provinceAdToOne, " ")

    //4.计算每个省中每个广告被点击的总数：((Province,AD),sum)
    val provinceAdToSum: RDD[((String, String), Int)] = provinceAdToOne.reduceByKey(_ + _)
    printRdd(provinceAdToSum, " ")

    //5.将省份作为key，广告加点击数为value：(Province,(AD,sum))
    val provinceToAdSum = provinceAdToSum.map(x => (x._1._1, (x._1._2, x._2)))
    printRdd(provinceToAdSum, " ")

    //6.将同一个省份的所有广告进行聚合(Province,List((AD1,sum1),(AD2,sum2)...))
    val provinceGroup = provinceToAdSum.groupByKey()
    printRdd(provinceGroup, " ")

    //7.对同一个省份所有广告的集合进行排序并取前3条，排序规则为广告点击总数
    val provinceAdTop3 = provinceGroup.mapValues { x =>
      x.toList.sortWith((x, y) => x._2 > y._2).take(3)
    }
    printRdd(provinceAdTop3, " ")

    //8.将数据拉取到Driver端并打印
    provinceAdTop3.collect().foreach(println)

    //9.关闭与spark的连接
    sc.stop()
  }

  // ----------------------------------------------Action---------------------------------------------------------------
  /**
   * 1. 作用：通过func函数聚集RDD中的所有元素，先聚合分区内数据，再聚合分区间数据。
   * 2. 需求：创建一个RDD，将所有元素聚合得到结果。
   */
  @Test
  def functionReduce(): Unit = {
    val rdd1 = sc.makeRDD(1 to 10, 2)
    println(rdd1.reduce(_ + _))
    val rdd2 = sc.makeRDD(Array(("a", 1), ("a", 3), ("c", 3), ("d", 5)))
    println(rdd2.reduce((x, y) => (x._1 + y._1, x._2 + y._2)))
  }

  /**
   * 1. 参数：(zeroValue: U)(seqOp: (U, T) ⇒ U, combOp: (U, U) ⇒ U)
   * 2. 作用：
   * aggregate函数将每个分区里面的元素通过seqOp和初始值进行聚合，然后用combine函数将每个分区的结果和初始值(zeroValue)进行combine操作。
   * 这个函数最终返回的类型不需要和RDD中元素类型一致。
   * 3. 需求：创建一个RDD，将所有元素相加得到结果
   */
  @Test
  def functionAggregate(): Unit = {
    val rdd1 = sc.makeRDD(1 to 10, 2)
    println(rdd1.aggregate(0)(_ + _, _ + _))

  }

  // ------------------------------------------------工具----------------------------------------------------------------
  def printRdd[T](simpleRdd: RDD[T], end: String = "\n"): Unit = {
    for (x <- simpleRdd)
      print(x + end)
    println()
  }

  def glomPrint[T](rdd: RDD[Array[T]]): Unit = {
    for (x <- rdd) {
      for (y <- x) {
        print(y + " ")
      }
      println()
    }
  }
}
