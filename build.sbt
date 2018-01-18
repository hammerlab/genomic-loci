organization := "org.hammerlab.genomics"
name := "loci"
v"2.0.2"

github.repo("genomic-loci")

addSparkDeps

dep(
  args4j,
  args4s       % "1.3.0",
  htsjdk,
  iterators    % "2.0.0",
  paths        % "1.4.0",
  reference    % "1.4.1" + testtest,
  scalautils,
  spark_util   % "2.0.1",
  string_utils % "1.2.0"
)

// Shade Guava due to use of RangeSet classes from 16.0.1 that don't exist in Spark/Hadoop's Guava 11.0.2.
shadedDeps += guava

// Rename shaded Guava classes
shadeRenames += "com.google.common.**" → "org.hammerlab.guava.@1"
shadeRenames += "com.google.thirdparty.**" → "org.hammerlab.guava.@1"

// Publish JAR that includes shaded Guava.
publishThinShadedJar

publishTestJar
