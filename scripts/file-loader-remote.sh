#!/bin/sh

if [ -z "$1" ] || [ -z "$2" ]  ; then
        echo Insufficient parameters !
        exit 1
fi

target_file_path=$1
db_name=$2
table_name=$3
user_name=$4
password=$5
tpdid=$6
delimiter=$7
target_dir_path=$8
mon_dir_name=$9
file_name=${10}

new_date=`date +"%s"`

echo "executing command -> ssh tduser@10.0.0.11 mkdir -p /home/tduser/$target_dir_path" >> /var/log/BDRE/td-file-load-$new_date.log
#ssh tduser@10.0.0.11 "mkdir -p /home/tduser/$target_dir_path" >> /var/log/BDRE/td-file-load-$new_date.log

echo "executing command -> scp $mon_dir_name$file_name tduser@10.0.0.11:/home/tduser/$target_dir_path" >> /var/log/BDRE/td-file-load-$new_date.log
#scp $mon_dir_name$file_name tduser@10.0.0.11:/home/tduser/$target_dir_path >> /var/log/BDRE/td-file-load-$new_date.log

echo "executing command -> ssh tduser@10.0.0.11 file-loader-local.sh $target_file_path $db_name $table_name $user_name $password $tpdid $delimiter" >> /var/log/BDRE/td-file-load-$new_date.log
#ssh tduser@10.0.0.11 "file-loader-local.sh $target_file_path $db_name $table_name $user_name $password $tpdid $delimiter" >> /var/log/BDRE/td-file-load-$new_date.log