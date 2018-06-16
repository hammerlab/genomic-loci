subgroup("genomics", "loci")
github.repo("genomic-loci")
v"2.2.0"

spark

import genomics.reference

dep(
      case_app,
        htsjdk,
     iterators % "2.2.0",
         paths % "1.5.0",
     reference % "1.5.0" + testtest,
    scalautils,
    spark_util % "3.0.0",
  string_utils % "1.2.0"
)

// Shade Guava due to use of RangeSet classes from 16.0.1 that don't exist in Spark/Hadoop's Guava 11.0.2.
shadedDeps += guava

// Rename shaded Guava classes
shadeRenames += "com.google.common.**"     → "org.hammerlab.guava.@1"
shadeRenames += "com.google.thirdparty.**" → "org.hammerlab.guava.@1"

// Publish JAR that includes shaded Guava.
publishThinShadedJar

publishTestJar

