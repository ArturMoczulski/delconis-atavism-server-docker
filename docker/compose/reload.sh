#/bin/sh
docker-compose -f docker/compose/development/single.yml exec -d master /bin/sh -c "killall java"
docker-compose -f docker/compose/development/single.yml exec -d master /bin/sh -c "sh start.sh"