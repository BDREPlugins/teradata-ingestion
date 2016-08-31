# Fetching Arguments

file_name=$1
db_name=$2
table_name=$3
target_username=$4
target_password=$5
target_tpdid=$6
delimiter=$7
# Removing old files.

rm -f output.txt
rm -f 1st_column_names.txt
rm -f 2nd_column_names.txt
rm -f 3rd_column_names.txt

echo "Old file has removed!!"

# Exporting columns with varchar(1000) to a separate file.

bteq<<EOF
.logon $target_tpdid/$target_username,$target_password;
.export file = 1st_column_names.txt
.set titledashes off
.set width 65531;

LOCK ROW FOR ACCESS select abcdefgh from (select tablename, TRIM(TRAILING ',' FROM (xmlagg(trim(columnname) || ' VARCHAR(250) ,' order by columnid)(varchar(10000)))) as abcdefgh from dbc.columns where Tablename = '${table_name}' and databasename ='${db_name}' group by 1)s;

.LOGOFF
.QUIT
EOF

sed '1d' 1st_column_names.txt > tmp_1st_column_names
mv tmp_1st_column_names 1st_column_names.txt

first_col=`cat 1st_column_names.txt`

# Exporting only columns for insert to a separate file.

bteq<<EOF
.logon $target_tpdid/$target_username,$target_password;
.export file = 2nd_column_names.txt
.set titledashes off
.set width 65531;

LOCK ROW FOR ACCESS select abcdefgh from (select tablename, TRIM(TRAILING ',' FROM (xmlagg(trim(columnname) || ' ,' order by columnid)(varchar(10000)))) as abcdefgh from dbc.columns where Tablename = '${table_name}' and databasename ='${db_name}' group by 1)s;

.LOGOFF
.QUIT
EOF

sed '1d' 2nd_column_names.txt > tmp_2nd_column_names
mv tmp_2nd_column_names 2nd_column_names.txt

second_col=`cat 2nd_column_names.txt`

# Exporting only columns for insert with comma to a separate file.

bteq<<EOF
.logon $target_tpdid/$target_username,$target_password;
.export file = 3rd_column_names.txt
.set titledashes off
.set width 65531;

LOCK ROW FOR ACCESS select abcdefgh from (select tablename, TRIM(TRAILING ',' FROM (xmlagg(':'||trim(columnname) || ' ,' order by columnid)(varchar(10000)))) as abcdefgh from dbc.columns where Tablename = '${table_name}' and databasename ='${db_name}' group by 1)s;

.LOGOFF
.QUIT
EOF

sed '1d' 3rd_column_names.txt > tmp_3rd_column_names
mv tmp_3rd_column_names 3rd_column_names.txt

third_col=`cat 3rd_column_names.txt`

# Code Block to connect the DB and run the TD query. 

echo " Connecting to database $target_username -- $target_tpdid"

bteq<<EOF
.logon $target_tpdid/$target_username,$target_password;
.SET width 64000;
.export file = output.txt;
.PACK 1000
.IMPORT VARTEXT ',' file=$file_name;
.REPEAT *
USING($first_col)

INSERT INTO $db_name.$table_name
(
$second_col
)
VALUES(
$third_col
);

.LOGOFF;
.EXIT;
EOF

exitstatus=$?

if [[ $exitstatus -ne 0 ]] 
then 
echo "Error Occurred."
echo "TD query ran and the output has been saved into output.txt file"
exit 1
fi 


echo "Query got executed successfully"
echo "TD query ran and the output has been saved into output.txt file"

