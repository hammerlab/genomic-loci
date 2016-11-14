package org.hammerlab.genomics

package object reference {
  type ContigName = String
  type Locus = Long
  type NumLoci = Long
  type ContigLengths = Map[ContigName, NumLoci]
}
