
organization := "org.hammerlab.genomics"
name := "loci"
version := "2.0.0-SNAPSHOT"

addSparkDeps

deps ++= Seq(
  libs.value('args4j),
  libs.value('args4s),
  libs.value('htsjdk),
  libs.value('iterators).copy(revision = "1.3.0-SNAPSHOT"),
  libs.value('paths).copy(revision = "1.1.1-SNAPSHOT"),
  libs.value('scalautils),
  libs.value('string_utils)
)

compileAndTestDeps += libs.value('reference)

// Shade Guava due to use of RangeSet classes from 16.0.1 that don't exist in Spark/Hadoop's Guava 11.0.2.
shadedDeps += "com.google.guava" % "guava" % "19.0"

// Rename shaded Guava classes.
shadeRenames += "com.google.common.**" -> "org.hammerlab.guava.@1"
shadeRenames += "com.google.thirdparty.**" -> "org.hammerlab.guava.@1"

// Publish JAR that includes shaded Guava.
publishThinShadedJar

publishTestJar
