name := "genomic-loci"
version := "1.4.0"

providedDeps ++= Seq(
  libraries.value('spark),
  "org.apache.hadoop" % "hadoop-client" % "2.6.0" exclude("javax.servlet", "*")
)

libraryDependencies ++= Seq(
  "org.hammerlab" %% "iterator" % "1.0.0",
  "com.github.samtools" % "htsjdk" % "2.6.1",
  "org.bdgenomics.bdg-formats" % "bdg-formats" % "0.9.0",
  "com.esotericsoftware.kryo" % "kryo" % "2.21"
)

testDeps += "org.hammerlab" %% "spark-tests" % "1.1.1"

// Shade Guava due to use of RangeSet classes from 16.0.1 that don't exist in Spark/Hadoop's Guava 11.0.2.
shadedDeps += "com.google.guava" % "guava" % "16.0.1"

// Rename shaded Guava classes.
shadeRenames += "com.google.common.**" -> "org.hammerlab.guava.@1"
shadeRenames += "com.google.thirdparty.**" -> "org.hammerlab.guava.@1"

// Publish JAR that includes shaded Guava.
ParentPlugin.publishThinShadedJar

