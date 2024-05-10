
# Hydrate /atavism_server/bin/*.properties files with values from .env

YELLOW='\033[0;31m'

echo " ${YELLOW}Setting up world.properties for isolated modules${YELLOW}"

## Configure ports and hosts for modules
sed -i 's/atavism.login.bindaddress.*/atavism.login.bindaddress='"login"'/' /atavism_server/bin/world.properties
sed -i 's/atavism.proxy.bindaddress.*/atavism.proxy.bindaddress='"proxy"'/' /atavism_server/bin/world.properties
sed -i 's/atavism.msgsvr_hostname.*/atavism.msgsvr_hostname='"domain"'/' /atavism_server/bin/world.properties
