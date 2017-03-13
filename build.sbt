
organization := "org.hammerlab.genomics"
name := "loci"
version := "1.5.3"

addSparkDeps

deps ++= Seq(
  libs.value('args4j),
  libs.value('args4s),
  libs.value('htsjdk),
  libs.value('iterators),
  libs.value('scalautils),
  libs.value('string_utils)
)

compileAndTestDeps += libs.value('reference)

// Shade Guava due to use of RangeSet classes from 16.0.1 that don't exist in Spark/Hadoop's Guava 11.0.2.
shadedDeps += "com.google.guava" % "guava" % "16.0.1"

// Rename shaded Guava classes.
shadeRenames += "com.google.common.**" -> "org.hammerlab.guava.@1"
shadeRenames += "com.google.thirdparty.**" -> "org.hammerlab.guava.@1"

// Publish JAR that includes shaded Guava.
publishThinShadedJar

publishTestJar
