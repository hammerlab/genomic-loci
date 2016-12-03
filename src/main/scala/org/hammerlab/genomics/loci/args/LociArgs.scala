package org.hammerlab.genomics.loci.args

import org.hammerlab.args4s.StringOptionHandler
import org.kohsuke.args4j.{ Option => Args4jOption }

/** Arguments for accepting a set of loci to restrict variant-calling to. */
trait LociArgs {
  @Args4jOption(
    name = "--loci",
    usage = "If set, loci to include. Either 'all' or 'contig[:start[-end]],contig[:start[-end]],…'",
    forbids = Array("--loci-file"),
    handler = classOf[StringOptionHandler]
  )
  var lociStrOpt: Option[String] = None

  @Args4jOption(
    name = "--loci-file",
    usage = "Path to file giving loci to include.",
    forbids = Array("--loci"),
    handler = classOf[StringOptionHandler]
  )
  var lociFileOpt: Option[String] = None
}
