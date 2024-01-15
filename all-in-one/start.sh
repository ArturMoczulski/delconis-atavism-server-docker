# auth.properties
sed -i 's/atavism.db_user=.*/atavism.db_user='"$MASTER_DATABASE_USER"'/' /atavism_server/bin/auth.properties
sed -i 's/atavism.db_password=.*/atavism.db_password='"$MASTER_DATABASE_PASSWORD"'/' /atavism_server/bin/auth.properties
sed -i 's/atavism.socketpolicy.bindaddress.*/atavism.socketpolicy.bindaddress='"localhost"'/' /atavism_server/bin/auth.properties
sed -i 's/atavism.db_hostname=.*/atavism.db_hostname='"mysql-master"'/' /atavism_server/bin/auth.properties

# world.properties
## As Desribed in the Atavism Documentation
sed -i 's/atavism.licence.email.*/atavism.licence.email='"$ATAVISM_EMAIL"'/' /atavism_server/bin/world.properties
sed -i 's/atavism.licence.key.*/atavism.licence.key='"$ATAVISM_LICENCE_KEY"'/' /atavism_server/bin/world.properties
sed -i 's/atavism.login.bindaddress.*/atavism.login.bindaddress='"localhost"'/' /atavism_server/bin/world.properties
sed -i 's/atavism.proxy.bindaddress.*/atavism.proxy.bindaddress='"localhost"'/' /atavism_server/bin/world.properties
sed -i 's/atavism.proxy.externaladdress.*/atavism.proxy.externaladdress='"host.docker.internal"'/' /atavism_server/bin/world.properties

## Atavism DB
sed -i 's/atavism.db_user=.*/atavism.db_user='"$ATAVISM_DATABASE_USER"'/' /atavism_server/bin/world.properties
sed -i 's/atavism.db_password=.*/atavism.db_password='"$ATAVISM_DATABASE_PASSWORD"'/' /atavism_server/bin/world.properties
sed -i 's/atavism.db_hostname=.*/atavism.db_hostname='"mysql-atavism"'/' /atavism_server/bin/world.properties

## Admin DB
sed -i 's/atavism.admin.db_user=.*/atavism.admin.db_user='"$ADMIN_DATABASE_USER"'/' /atavism_server/bin/world.properties
sed -i 's/atavism.admin.db_password=.*/atavism.admin.db_password='"$ADMIN_DATABASE_PASSWORD"'/' /atavism_server/bin/world.properties
sed -i 's/atavism.admin.db_hostname=.*/atavism.admin.db_hostname='"mysql-admin"'/' /atavism_server/bin/world.properties
sed -i 's/atavism.admin.db_differentsettings.*/atavism.admin.db_differentsettings=true/' /atavism_server/bin/world.properties

## World Content DB
sed -i 's/atavism.content.db_differentsettings=.*/atavism.content.db_differentsettings=true/' /atavism_server/bin/world.properties
sed -i 's/atavism.content.db_user=.*/atavism.content.db_user='"$WORLD_DATABASE_USER"'/' /atavism_server/bin/world.properties
sed -i 's/atavism.content.db_password=.*/atavism.content.db_password='"$WORLD_DATABASE_PASSWORD"'/' /atavism_server/bin/world.properties
sed -i 's/atavism.content.db_hostname=.*/atavism.content.db_hostname='"mysql-world"'/' /atavism_server/bin/world.properties

## Master DB
sed -i 's/atavism.auth.db_differentsettings=.*/atavism.auth.db_differentsettings=true/' /atavism_server/bin/world.properties
sed -i 's/atavism.auth.db_user=.*/atavism.auth.db_user='"$MASTER_DATABASE_USER"'/' /atavism_server/bin/world.properties
sed -i 's/atavism.auth.db_password=.*/atavism.auth.db_password='"$MASTER_DATABASE_PASSWORD"'/' /atavism_server/bin/world.properties
sed -i 's/atavism.auth.db_hostname=.*/atavism.auth.db_hostname='"mysql-master"'/' /atavism_server/bin/world.properties


sed -i 's/atavism.log_level.*/atavism.log_level=0/' /atavism_server/bin/world.properties

cd /atavism_server/bin/ && ./auth.sh -vC start && ./world.sh -vC start

while /atavism_server/bin/auth.sh -vC status | grep -q "RUNNING"; do
    sleep 5
done

cat logs /atavism_server/logs/master/auth.log