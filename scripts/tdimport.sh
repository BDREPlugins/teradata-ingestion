#!/bin/sh
. $(dirname $0)/../env.properties
BDRE_HOME=~/bdre
BDRE_APPS_HOME=~/bdre_app
if [ -z "$1" ] || [ -z "$2" ] || [ -z "$3" ] || [ -z "$4" ] ; then
        echo Insufficient parameters !
        exit 1
fi
busDomainId=$1
processTypeId=$2
processId=$3
userName=$4
new_date=`date +"%s"`

echo $0
#creating flume command for
java -cp "$BDRE_HOME/lib/teradata-ingestion/*" com.wipro.ats.bdre.tdimport.TDImportRunnableMain -p $processId -u $userName>> /var/log/BDRE/exec-td-import$new_date.log
