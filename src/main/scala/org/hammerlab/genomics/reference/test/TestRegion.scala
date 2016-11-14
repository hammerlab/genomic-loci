package org.hammerlab.genomics.reference.test

import org.hammerlab.genomics.reference.{ContigName, Region}

case class TestRegion(contigName: ContigName, start: Long, end: Long) extends Region

object TestRegion {
  implicit def makeTestRegion(t: (ContigName, Int, Int)): TestRegion = TestRegion(t._1, t._2, t._3)
}
