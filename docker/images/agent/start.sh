#   _____            _                                      _   
#  | ____|_ ____   _(_)_ __ ___  _ __  _ __ ___   ___ _ __ | |_ 
#  |  _| | '_ \ \ / / | '__/ _ \| '_ \| '_ ` _ \ / _ \ '_ \| __|
#  | |___| | | \ V /| | | | (_) | | | | | | | | |  __/ | | | |_ 
#  |_____|_| |_|\_/ |_|_|  \___/|_| |_|_| |_| |_|\___|_| |_|\__|
                                                              
. /env.sh

#   ____  _             _                         _          
#  / ___|| |_ __ _ _ __| |_   ___  ___ _ ____   _(_) ___ ___ 
#  \___ \| __/ _` | '__| __| / __|/ _ \ '__\ \ / / |/ __/ _ \
#   ___) | || (_| | |  | |_  \__ \  __/ |   \ V /| | (_|  __/
#  |____/ \__\__,_|_|   \__| |___/\___|_|    \_/ |_|\___\___|
                                                  
figlet -c "Start $1"

cd /atavism_server/bin/ && ./world.sh -vC $1

while /atavism_server/bin/world.sh -vC status | grep -q "RUNNING"; do
    sleep 5
done

# Print logs at exit
cat logs /atavism_server/world/$1.log