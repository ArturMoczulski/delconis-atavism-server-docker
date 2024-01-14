## Script to run in SQL container to initialize the database

## if CORE env var is set, then we are running in a core container
mkdir -p /sql/run

if [ -z "$CORE" ]; then
    echo "Copying NEW Core SQL files to /sql/run"
    cp /sql/New_Install_Core/*.sql /sql/run
## Else we'll default to the demo
else
    echo "Copying NEW Demo SQL files to /sql/run"
    cp /sql/New_Install_with_Demo_Data/*.sql /sql/run
## End if
fi

mv /sql/run/admin.sql /docker-entrypoint-initdb.d/2.sql
mv /sql/run/atavism.sql /docker-entrypoint-initdb.d/3.sql
mv /sql/run/master.sql /docker-entrypoint-initdb.d/4.sql
mv /sql/run/world_content.sql /docker-entrypoint-initdb.d/5.sql

sed -i 's/atavism_user/'"$ATAVISM_USER"'/' /docker-entrypoint-initdb.d/1.sql
sed -i 's/atavism_password/'"$ATAVISM_PASSWORD"'/' /docker-entrypoint-initdb.d/1.sql