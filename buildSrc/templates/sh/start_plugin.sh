function start_{{agentType}} () {

    AGENT_TYPE={{agentType}}
    AGENT_NAME={{agentName}}

    if [ $verbose -gt 0 ]; then
        echo -en "Starting $AGENT_NAME server: \t"
    fi
  java \
        {{javaPluginFlagsVar}} \
        -Datavism.loggername=$AGENT_NAME \
        -Dlog4j.configurationFile=${AO_BIN}/worldlog.xml \
        -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector \
        atavism.server.engine.Engine \
        -i "${AO_COMMON_CONFIG}"/aomessages.py \
        -i "${AO_WORLD_CONFIG}"/worldmessages.py \
        -t "${AO_COMMON_CONFIG}"/typenumbers.txt \
        "${AO_COMMON_CONFIG}"/global_props.py \
        "${AO_WORLD_CONFIG}"/global_props.py \
        "${AO_WORLD_CONFIG}"/{{agentType}}.py \
        &

  
    write_pid $AGENT_NAME $!

    if [ $verbose -gt 0 ]; then
        check_process $(cat "${AO_RUN}"/"$AGENT_NAME".pid)
     fi
	
}