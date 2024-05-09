## Extract the contents of atavism_server_10_9_0_2023.zip to the current directory
FROM alpine:3.7 as builder
RUN apk add --no-cache unzip
COPY *.zip .
RUN unzip *.zip

## MySql Server get the SQL scripts from the Atavism Server
FROM mysql:8.2
ARG SQL_FILE=admin.sql
COPY --from=builder /atavism_server/sql/New_Install_with_Demo_Data/${SQL_FILE} /docker-entrypoint-initdb.d/1.sql


