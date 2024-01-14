# auth.properties
sed -i 's/atavism.db_user=.*/atavism.db_user='"$ATAVISM_DATABASE_USER"'/' /atavism_server/bin/auth.properties
sed -i 's/atavism.db_password=.*/atavism.db_password='"$ATAVISM_DATABASE_PASSWORD"'/' /atavism_server/bin/auth.properties
sed -i 's/atavism.socketpolicy.bindaddress.*/atavism.socketpolicy.bindaddress='"host.docker.internal"'/' /atavism_server/bin/auth.properties

# world.properties
## As Desribed in the Atavism Documentation
sed -i 's/atavism.licence.email.*/atavism.licence.email='"$ATAVISM_EMAIL"'/' /atavism_server/bin/world.properties
sed -i 's/atavism.licence.key.*/atavism.licence.key='"$ATAVISM_LICENCE_KEY"'/' /atavism_server/bin/world.properties
set -i 's/atavism.login.bindaddress.*/atavism.login.bindaddress='"host.docker.internal"'/' /atavism_server/bin/world.properties
sed -i 's/atavism.proxy.bindaddress.*/atavism.proxy.bindaddress='"host.docker.internal"'/' /atavism_server/bin/world.properties
sed -i 's/atavism.proxy.externaladdress.*/atavism.proxy.externaladdress='"host.docker.internal"'/' /atavism_server/bin/world.properties
sed -i 's/atavism.db_user=.*/atavism.db_user='"$ATAVISM_DATABASE_USER"'/' /atavism_server/bin/world.properties
sed -i 's/atavism.db_password=.*/atavism.db_password='"$ATAVISM_DATABASE_PASSWORD"'/' /atavism_server/bin/world.properties
sed -i 's/atavism.db_hostname=.*/atavism.db_hostname='"atavism-sql"'/' /atavism_server/bin/world.properties

## Admin
sed -i 's/atavism.admin.db_user=.*/atavism.admin.db_user='"$ADMIN_DATABASE_USER"'/' /atavism_server/bin/world.properties
sed -i 's/atavism.admin.db_password=.*/atavism.admin.db_password='"$ADMIN_DATABASE_PASSWORD"'/' /atavism_server/bin/world.properties
sed -i 's/atavism.admin.db_hostname=.*/atavism.admin.db_hostname='"admin-sql"'/' /atavism_server/bin/world.properties

## Content
sed -i 's/atavism.content.db_user=.*/atavism.content.db_user='"$CONTENT_DATABASE_USER"'/' /atavism_server/bin/world.properties
sed -i 's/atavism.content.db_password=.*/atavism.content.db_password='"$CONTENT_DATABASE_PASSWORD"'/' /atavism_server/bin/world.properties
sed -i 's/atavism.content.db_hostname=.*/atavism.content.db_hostname='"world-sql"'/' /atavism_server/bin/world.properties

## Auth
sed -i 's/atavism.auth.db_user=.*/atavism.auth.db_user='"$AUTH_DATABASE_USER"'/' /atavism_server/bin/world.properties
sed -i 's/atavism.auth.db_password=.*/atavism.auth.db_password='"$AUTH_DATABASE_PASSWORD"'/' /atavism_server/bin/world.properties
sed -i 's/atavism.auth.db_hostname=.*/atavism.auth.db_hostname='"auth-sql"'/' /atavism_server/bin/world.properties

cd /atavism_server/bin/ && ./auth.sh -vC start && ./world.sh -vC start
