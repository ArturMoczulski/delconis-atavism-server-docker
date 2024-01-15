## Extract the contents of atavism_server_10_9_0_2023.zip to the current directory
FROM alpine:3.7 as builder
RUN apk add --no-cache unzip
COPY *.zip .
RUN unzip *.zip

## Create a Atavism Server image
FROM openjdk:23-slim-bullseye
RUN apt-get update -y
RUN apt-get install -y procps
COPY --from=builder /atavism_server/ /atavism_server/
COPY all-in-one/start.sh .
COPY private.key /atavism_server/bin
RUN chmod +x start.sh

CMD [ "sh", "start.sh" ]
