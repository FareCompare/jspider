#!/bin/sh

#
# Used to initialize the database
#
if [ "$1X" = "X" ]; then
  echo Database user name is required as parameter 1
  exit 1;
fi

mysql -u$1 -p < jspiderdb.sql