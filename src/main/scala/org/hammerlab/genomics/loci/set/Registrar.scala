package org.hammerlab.genomics.loci.set

import com.esotericsoftware.kryo.Kryo
import org.apache.spark.serializer.KryoRegistrator

class Registrar extends KryoRegistrator {
  override def registerClasses(kryo: Kryo): Unit = {
    kryo.register(classOf[LociSet], new Serializer)
    kryo.register(classOf[Array[LociSet]])
    kryo.register(classOf[Contig], new ContigSerializer)
    kryo.register(classOf[Array[Contig]])
  }
}