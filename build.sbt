
organization := "org.hammerlab.genomics"
name := "loci"
version := "1.5.0-SNAPSHOT"

providedDeps ++= {
  Seq(
    libraries.value('spark),
    libraries.value('hadoop)
  )
}

libraryDependencies ++= Seq(
  libraries.value('args4j),
  libraries.value('args4s),
  libraries.value('bdg_formats),
  libraries.value('htsjdk),
  libraries.value('iterators),
  libraries.value('kryo),
  libraries.value('reference),
  libraries.value('string_utils)
)

testDeps ++= Seq(
  libraries.value('spark_tests),
  libraries.value('test_utils)
)

// Shade Guava due to use of RangeSet classes from 16.0.1 that don't exist in Spark/Hadoop's Guava 11.0.2.
shadedDeps += "com.google.guava" % "guava" % "16.0.1"

// Rename shaded Guava classes.
shadeRenames += "com.google.common.**" -> "org.hammerlab.guava.@1"
shadeRenames += "com.google.thirdparty.**" -> "org.hammerlab.guava.@1"

// Publish JAR that includes shaded Guava.
ParentPlugin.publishThinShadedJar
