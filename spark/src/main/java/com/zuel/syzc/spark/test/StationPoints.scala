package com.zuel.syzc.spark.test

case class StationPoints(longitude: Double, latitude: Double, laci: String) extends Serializable {
  override def toString: String = "StationPoints" + longitude + "," + latitude + "," + laci
}
