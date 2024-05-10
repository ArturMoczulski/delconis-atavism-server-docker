#   _____            _                                      _   
#  | ____|_ ____   _(_)_ __ ___  _ __  _ __ ___   ___ _ __ | |_ 
#  |  _| | '_ \ \ / / | '__/ _ \| '_ \| '_ ` _ \ / _ \ '_ \| __|
#  | |___| | | \ V /| | | | (_) | | | | | | | | |  __/ | | | |_ 
#  |_____|_| |_|\_/ |_|_|  \___/|_| |_|_| |_| |_|\___|_| |_|\__|
                                                              
. /env.sh
. /module_env.sh

#   ____  _             _                         _       _      
#  / ___|| |_ __ _ _ __| |_   _ __ ___   ___   __| |_   _| | ___ 
#  \___ \| __/ _` | '__| __| | '_ ` _ \ / _ \ / _` | | | | |/ _ \
#   ___) | || (_| | |  | |_  | | | | | | (_) | (_| | |_| | |  __/
#  |____/ \__\__,_|_|   \__| |_| |_| |_|\___/ \__,_|\__,_|_|\___|
                                                               
# Capture the first command-line argument
ATAVISM_MODULE_NAME=$1

# Check if the module name was provided
if [ -z "$ATAVISM_MODULE_NAME" ]; then
    echo "Error: No module name provided."
    echo "Usage: $0 <module_name>"
    exit 1  # Exit the script with an error code
fi
                                                  
# Use figlet to display the module name
figlet -c "Start $ATAVISM_MODULE_NAME"

cd /atavism_server/bin/ && ./world.sh -vC $ATAVISM_MODULE_NAME 1

sleep 5

#   _                    
#  | |    ___   __ _ ___ 
#  | |   / _ \ / _` / __|
#  | |__| (_) | (_| \__ \
#  |_____\___/ \__, |___/
#              |___/     
figlet -c "Logs"

tail -F -n 1000 /atavism_server/logs/world/$ATAVISM_MODULE_NAME*.log