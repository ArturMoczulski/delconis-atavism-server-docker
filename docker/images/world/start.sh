
#   _____            _                                      _   
#  | ____|_ ____   _(_)_ __ ___  _ __  _ __ ___   ___ _ __ | |_ 
#  |  _| | '_ \ \ / / | '__/ _ \| '_ \| '_ ` _ \ / _ \ '_ \| __|
#  | |___| | | \ V /| | | | (_) | | | | | | | | |  __/ | | | |_ 
#  |_____|_| |_|\_/ |_|_|  \___/|_| |_|_| |_| |_|\___|_| |_|\__|
                                                              
. /env.sh

#   ____  _             _                        _     _ 
#  / ___|| |_ __ _ _ __| |_  __      _____  _ __| | __| |
#  \___ \| __/ _` | '__| __| \ \ /\ / / _ \| '__| |/ _` |
#   ___) | || (_| | |  | |_   \ V  V / (_) | |  | | (_| |
#  |____/ \__\__,_|_|   \__|   \_/\_/ \___/|_|  |_|\__,_|
                                                  
figlet -c "Start world"

YELLOW='\033[0;31m'
echo "${YELLOW}Starting as all_in_one: ${ATAVISM_ALL_IN_ONE_ENABLED}${YELLOW}"

# Change directory to the bin folder of atavism_server
cd /atavism_server/bin/

# Needs the following set up in world.properties
# atavism.all_in_one.enabled=false
./world.sh -vC start server
sleep 5 

#   _                    
#  | |    ___   __ _ ___ 
#  | |   / _ \ / _` / __|
#  | |__| (_) | (_| \__ \
#  |_____\___/ \__, |___/
#              |___/     
figlet -c "Logs"

tail -F /atavism_server/logs/world/*.log