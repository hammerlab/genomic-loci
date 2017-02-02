package org.hammerlab.genomics.loci.map

import com.esotericsoftware.kryo.Kryo
import org.apache.spark.serializer.KryoRegistrator
import org.hammerlab.genomics.reference

class Registrar extends KryoRegistrator {
  override def registerClasses(kryo: Kryo): Unit = {
    new reference.Registrar().registerClasses(kryo)

    kryo.register(classOf[LociMap[_]], new Serializer)
    kryo.register(classOf[Array[LociMap[_]]])
    kryo.register(classOf[Contig[_]], new ContigSerializer)
    kryo.register(classOf[Array[Contig[_]]])
  }
}
