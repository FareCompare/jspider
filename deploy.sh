#!/bin/sh

SERVER=$1
if [ "X$SERVER" = "X" ]; then
  echo Server name required as parameter 1
  exit 1
fi

echo "==========================================="
echo " Deploy jspider to $SERVER                 "
echo "==========================================="

ssh root@$SERVER 'mkdir -p /usr/local/jspider/output'
ssh root@$SERVER 'rm /usr/local/jspider/lib/*'

rsync -a --progress --exclude-from="exclude-list.txt" * root@$SERVER:/usr/local/jspider/.
rsync -a --progress source/crawler/target/jspider-crawler-*-exe.jar root@$SERVER:/usr/local/jspider/lib/.
rsync -a --progress source/crawler/src/main/ddl/jspiderdb.sql root@$SERVER:/usr/local/jspider/bin/.
