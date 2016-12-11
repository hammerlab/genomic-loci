package org.hammerlab.genomics.loci.kryo

import com.esotericsoftware.kryo.Kryo
import org.apache.spark.serializer.KryoRegistrator

class Registrar extends KryoRegistrator {
  override def registerClasses(kryo: Kryo): Unit = {
    {
      import org.hammerlab.genomics.loci.set.{ Contig, ContigSerializer, LociSet, Serializer }
      kryo.register(classOf[LociSet], new Serializer)
      kryo.register(classOf[Array[LociSet]])
      kryo.register(classOf[Contig], new ContigSerializer)
      kryo.register(classOf[Array[Contig]])
    }
    {
      import org.hammerlab.genomics.loci.map.{Contig, ContigSerializer, LociMap, Serializer}
      kryo.register(classOf[LociMap[_]], new Serializer)
      kryo.register(classOf[Array[LociMap[_]]])
      kryo.register(classOf[Contig[_]], new ContigSerializer)
      kryo.register(classOf[Array[Contig[_]]])
    }
  }
}
