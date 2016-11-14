package org.hammerlab.genomics.loci

import org.hammerlab.genomics.kryo.Registrar
import org.hammerlab.spark.test.suite.KryoSerializerSuite

class Suite extends KryoSerializerSuite(classOf[Registrar])
