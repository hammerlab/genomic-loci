package org.hammerlab.genomics.kryo

import com.esotericsoftware.kryo.Kryo
import org.apache.spark.serializer.KryoRegistrator
import org.hammerlab.genomics.loci.set.{Contig, ContigSerializer, LociSet, Serializer}
import org.hammerlab.genomics.reference.Position

class Registrar extends KryoRegistrator {
  override def registerClasses(kryo: Kryo): Unit = {
    // LociSet is serialized when broadcast in InputConfig.filterRDD. Serde'ing a LociSet delegates to an Array of
    // Contigs.
    kryo.register(classOf[LociSet], new Serializer)
    kryo.register(classOf[Array[LociSet]])
    kryo.register(classOf[Contig], new ContigSerializer)
    kryo.register(classOf[Array[Contig]])

    Position.registerKryo(kryo)
  }
}
