#      _   _              _                 ____                           
#     / \ | |_ __ ___   _(_)___ _ __ ___   / ___|  ___ _ ____   _____ _ __ 
#    / _ \| __/ _` \ \ / / / __| '_ ` _ \  \___ \ / _ \ '__\ \ / / _ \ '__|
#   / ___ \ || (_| |\ V /| \__ \ | | | | |  ___) |  __/ |   \ V /  __/ |   
#  /_/_  \_\__\__,_| \_/ |_|___/_| |_| |_| |____/ \___|_|    \_/ \___|_|   
#  |_ _|_ __ ___   __ _  __ _  ___                                         
#   | || '_ ` _ \ / _` |/ _` |/ _ \                                        
#   | || | | | | | (_| | (_| |  __/                                        
#  |___|_| |_| |_|\__,_|\__, |\___|                                        
#                       |___/                                              

#   ____                            _                 _           
#  |  _ \  ___ _ __   ___ _ __   __| | ___ _ __   ___(_) ___  ___ 
#  | | | |/ _ \ '_ \ / _ \ '_ \ / _` |/ _ \ '_ \ / __| |/ _ \/ __|
#  | |_| |  __/ |_) |  __/ | | | (_| |  __/ | | | (__| |  __/\__ \
#  |____/ \___| .__/ \___|_| |_|\__,_|\___|_| |_|\___|_|\___||___/
#             |_|                                                 
FROM openjdk:23-slim-bullseye as dependencies

RUN apt-get update -y
RUN apt-get install -y procps figlet

#   ____                 _                                  _   
#  |  _ \  _____   _____| | ___  _ __  _ __ ___   ___ _ __ | |_ 
#  | | | |/ _ \ \ / / _ \ |/ _ \| '_ \| '_ ` _ \ / _ \ '_ \| __|
#  | |_| |  __/\ V /  __/ | (_) | |_) | | | | | |  __/ | | | |_ 
#  |____/ \___| \_/ \___|_|\___/| .__/|_| |_| |_|\___|_| |_|\__|
#                               |_|                             
FROM dependencies as development

ARG ATAVISM_MODULE_NAME
ENV ATAVISM_MODULE_NAME=${ATAVISM_MODULE_NAME}

CMD sh start.sh ${ATAVISM_MODULE_NAME:-server}

#   ____                _            _   _             
#  |  _ \ _ __ ___   __| |_   _  ___| |_(_) ___  _ __  
#  | |_) | '__/ _ \ / _` | | | |/ __| __| |/ _ \| '_ \ 
#  |  __/| | | (_) | (_| | |_| | (__| |_| | (_) | | | |
#  |_|   |_|  \___/ \__,_|\__,_|\___|\__|_|\___/|_| |_|
FROM dependencies as production

ARG ATAVISM_MODULE_NAME
ENV ATAVISM_MODULE_NAME=${ATAVISM_MODULE_NAME}
ARG IMAGE_NAME
ENV IMAGE_NAME=${IMAGE_NAME}

RUN mkdir -p /atavism_server/bin

# Copy server files
COPY atavism_server /atavism_server

# Copy SSL file
COPY ssl/private.key /atavism_server/bin/private.key

# Copy environment setup script
COPY ./docker/images/env.sh /env.sh
RUN chmod +x env.sh

# Copy start script
COPY ./docker/images/${IMAGE_NAME:-world}/start.sh ${ATAVISM_MODULE_NAME:-server}
RUN chmod +x start.sh

CMD sh start.sh ${ATAVISM_MODULE_NAME}
                                                     
