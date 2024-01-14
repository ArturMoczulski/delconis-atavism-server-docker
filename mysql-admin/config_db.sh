if [ -z "$CORE" ]; then
    echo "Copying NEW Core SQL files to /docker-entrypoint-initdb.d/2.sql"
    cp /sql/New_Install_Core/admin.sql /docker-entrypoint-initdb.d/2.sql
## Else we'll default to the demo
else
    echo "Copying NEW Demo SQL files to  /docker-entrypoint-initdb.d/2.sql"
    cp /sql/New_Install_with_Demo_Data/admin.sql /docker-entrypoint-initdb.d/2.sql
## End if
fi

sed -i 's/atavism_user/'"$ADMIN_DATABASE_USER"'/' /docker-entrypoint-initdb.d/1.sql
sed -i 's/atavism_password/'"$ADMIN_DATABASE_PASSWORD"'/' /docker-entrypoint-initdb.d/1.sql

rm -rf /sql