## Extract the contents of atavism_server_10_9_0_2023.zip to the current directory

FROM alpine:3.7 as builder
RUN apk add --no-cache unzip
COPY *.zip .
RUN unzip *.zip

## MySql Server get the SQL scripts from the Atavism Server
ARG NEW_INSTALL
FROM mysql:5.7
COPY --from=builder /atavism_server/sql/ /sql/
COPY mysql/copy_sql.sh .
RUN chmod +x copy_sql.sh
RUN ./copy_sql.sh





