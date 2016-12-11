package org.hammerlab.genomics.loci

import org.hammerlab.genomics.loci.kryo.Registrar
import org.hammerlab.spark.test.suite.{ KryoSerializerSuite, SparkSerializerSuite }

class LociSerializerSuite
  extends KryoSerializerSuite(classOf[Registrar], referenceTracking = true)
  with SparkSerializerSuite
