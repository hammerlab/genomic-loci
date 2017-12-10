# genomic-loci

[![Build Status](https://travis-ci.org/hammerlab/genomic-loci.svg?branch=master)](https://travis-ci.org/hammerlab/genomic-loci)
[![Coverage Status](https://coveralls.io/repos/github/hammerlab/genomic-loci/badge.svg?branch=master)](https://coveralls.io/github/hammerlab/genomic-loci?branch=master)
[![Maven Central](https://img.shields.io/maven-central/v/org.hammerlab.genomics/loci_2.11.svg?maxAge=1800)](http://search.maven.org/#search%7Cga%7C1%7Cgenomics%20loci)

Utilities for working with genomic loci.

- [`parsing`](src/main/scala/org/hammerlab/genomics/loci/parsing): utilities for parsing string-representations of loci. 
- [`set`](src/main/scala/org/hammerlab/genomics/loci/set): `LociSet` data-structure: stores genomic-loci ranges and allows for intersection/containment checks.
- [`map`](src/main/scala/org/hammerlab/genomics/loci/map): `LociMap` data-structure: allows for mapping genomic ranges to arbitrary values.
- [`iterator`](src/main/scala/org/hammerlab/genomics/loci/iterator): iterators for traversing genomic loci.
