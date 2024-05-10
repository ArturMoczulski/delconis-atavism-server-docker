#/bin/sh
# Check if the 'world' service is up
if ! docker-compose -f docker/compose/development/single.yml ps world | grep -q 'Up'; then
    echo "Error: The world service is not running. Please run 'docker-compose up' first."
    exit 1
fi

# If the service is up, proceed with the commands
docker-compose -f docker/compose/development/single.yml exec -d world /bin/sh -c "killall java"
docker-compose -f docker/compose/development/single.yml exec -d world /bin/sh -c "sh start.sh"
