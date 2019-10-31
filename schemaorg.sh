#!/bin/sh

# Wrapper script around the hw1.jar, used for abbreviating the big java -jar ... command
# to a more readable and concise one.

# USAGE: schemaorg.sh myquery
# Where myquery is the name of the query without the file extension.
# Queries must be put inside queries folder.

java -jar target/hw1.jar "queries/$1.ql" data/schema.nt
