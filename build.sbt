name := "genomic-loci"
version := "1.4.3"

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
  libraries.value('kryo),
  "org.hammerlab" %% "iterator" % "1.0.0",
  "com.github.samtools" % "htsjdk" % "2.6.1"
)

testDeps += libraries.value('spark_tests)

// Shade Guava due to use of RangeSet classes from 16.0.1 that don't exist in Spark/Hadoop's Guava 11.0.2.
shadedDeps += "com.google.guava" % "guava" % "16.0.1"

// Rename shaded Guava classes.
shadeRenames += "com.google.common.**" -> "org.hammerlab.guava.@1"
shadeRenames += "com.google.thirdparty.**" -> "org.hammerlab.guava.@1"

// Publish JAR that includes shaded Guava.
ParentPlugin.publishThinShadedJar
