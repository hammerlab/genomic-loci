package org.hammerlab.genomics.loci

import caseapp.{ HelpMessage ⇒ M }
import org.hammerlab.genomics.loci.parsing.ParsedLoci
import org.hammerlab.genomics.loci.parsing.ParsedLoci.loadFromPath
import org.hammerlab.genomics.reference.ContigName.Factory
import org.hammerlab.paths.Path

/** Arguments for accepting a set of loci to restrict variant-calling to. */
case class Args(
  @M("If set, loci to include. Either 'all' or 'contig[:start[-end]],contig[:start[-end]],…'")
  loci: Option[String] = None,

  @M("Path to file giving loci to include")
  lociFile: Option[Path] = None
) {
  def parse(
      implicit
      factory: Factory
  ): Option[ParsedLoci] =
    (loci, lociFile) match {
      case (Some(str), Some(file)) ⇒
        throw new IllegalArgumentException(
          "Specify a loci string (--loci) xor file (--loci-file)"
        )
      case (Some(str), _) => Some(ParsedLoci(str))
      case (_, Some(path)) => Some(loadFromPath(path))
      case _ =>
        None
    }
}
