name := "genomic-loci"
version := "1.0.0"
libraryDependencies <++= libraries { v => Seq(
  v('spark),
  "org.hammerlab" %% "iterator" % "1.0.0",
  "com.github.samtools" % "htsjdk" % "2.6.1",
  "org.apache.hadoop" % "hadoop-client" % "2.6.0" exclude("javax.servlet", "*"),
  "com.google.guava" % "guava" % "16.0.1",
  "org.bdgenomics.bdg-formats" % "bdg-formats" % "0.9.0",
  "com.esotericsoftware.kryo" % "kryo" % "2.21",
  "org.hammerlab" %% "spark-tests" % "1.1.0" % "test"
)}
