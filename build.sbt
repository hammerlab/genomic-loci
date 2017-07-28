
organization := "org.hammerlab.genomics"
name := "loci"
version := "2.0.0-SNAPSHOT"

addSparkDeps

deps ++= Seq(
  args4j,
  args4s % "1.2.3",
  htsjdk,
  iterators % "1.3.0-SNAPSHOT",
  paths % "1.1.1-SNAPSHOT",
  scalautils,
  string_utils % "1.2.0"
)

compileAndTestDeps += reference % "1.4.0-SNAPSHOT"

// Shade Guava due to use of RangeSet classes from 16.0.1 that don't exist in Spark/Hadoop's Guava 11.0.2.
shadedDeps += guava

// Rename shaded Guava classes.
shadeRenames += "com.google.common.**" → "org.hammerlab.guava.@1"
shadeRenames += "com.google.thirdparty.**" → "org.hammerlab.guava.@1"

// Publish JAR that includes shaded Guava.
publishThinShadedJar

publishTestJar
