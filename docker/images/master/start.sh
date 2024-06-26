
#   _____            _                                      _   
#  | ____|_ ____   _(_)_ __ ___  _ __  _ __ ___   ___ _ __ | |_ 
#  |  _| | '_ \ \ / / | '__/ _ \| '_ \| '_ ` _ \ / _ \ '_ \| __|
#  | |___| | | \ V /| | | | (_) | | | | | | | | |  __/ | | | |_ 
#  |_____|_| |_|\_/ |_|_|  \___/|_| |_|_| |_| |_|\___|_| |_|\__|
                                                              
. /env.sh

#   ____  _             _                         _            
#  / ___|| |_ __ _ _ __| |_   _ __ ___   __ _ ___| |_ ___ _ __ 
#  \___ \| __/ _` | '__| __| | '_ ` _ \ / _` / __| __/ _ \ '__|
#   ___) | || (_| | |  | |_  | | | | | | (_| \__ \ ||  __/ |   
#  |____/ \__\__,_|_|   \__| |_| |_| |_|\__,_|___/\__\___|_|   
                                                             
                                                  
figlet -c "Start master"

cd /atavism_server/bin/
chmod u+x auth.sh
./auth.sh -vC start 
sleep 5 

#   _                    
#  | |    ___   __ _ ___ 
#  | |   / _ \ / _` / __|
#  | |__| (_) | (_| \__ \
#  |_____\___/ \__, |___/
#              |___/     
figlet -c "Logs"

tail -F /atavism_server/logs/master/*.log