package com.zuel.syzc.spark.test

object ScalaTest {
  def main(args: Array[String]): Unit = {
    val rddTest: RddTest = new RddTest
    //    rddTest.rddCreate()
    //    rddTest.mapFunction()
    rddTest.functionMapPartitions()
  }
}
