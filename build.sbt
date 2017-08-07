
organization := "org.hammerlab.genomics"
name := "loci"
version := "2.0.1"

addSparkDeps

deps ++= Seq(
  args4j,
  "org.hammerlab" ^^ "args4s" ^ "1.3.0",
  htsjdk,
  iterators % "1.3.0",
  paths % "1.2.0",
  scalautils,
  string_utils % "1.2.0"
)

compileAndTestDeps += reference % "1.4.0"

// Shade Guava due to use of RangeSet classes from 16.0.1 that don't exist in Spark/Hadoop's Guava 11.0.2.
shadedDeps += guava

// Rename shaded Guava classes
shadeRenames += "com.google.common.**" → "org.hammerlab.guava.@1"
shadeRenames += "com.google.thirdparty.**" → "org.hammerlab.guava.@1"

// Publish JAR that includes shaded Guava.
publishThinShadedJar

publishTestJar
