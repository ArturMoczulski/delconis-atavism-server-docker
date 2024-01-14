## Extract the contents of atavism_server_10_9_0_2023.zip to the current directory
FROM alpine:3.7 as builder
RUN apk add --no-cache unzip
COPY *.zip .
RUN unzip *.zip

## MySql Server get the SQL scripts from the Atavism Server
FROM mysql:5.7

RUN yum update -y
COPY --from=builder /atavism_server/sql/ /sql/
COPY mysql-world-content/config_db.sh .
COPY 1.sql /docker-entrypoint-initdb.d/
RUN chmod +x config_db.sh
RUN ./config_db.sh
