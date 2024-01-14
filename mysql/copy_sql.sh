## Script to run in SQL container to initialize the database

## if CORE env var is set, then we are running in a core container
mkdir -p /sql/run

if [ -z "$CORE" ]; then
    echo "Copying NEW Core SQL files to /sql/run"
    cp /sql/New_Install_Core/*.sql /sql/run
## if DEMO env var is set, then we are running in a demo container
elif [ -z "$DEMO" ]; then
    echo "Copying NEW Demo SQL files to /sql/run"
    cp /sql/New_Install_with_Demo_Data/*.sql /sql/run
## End if
fi
