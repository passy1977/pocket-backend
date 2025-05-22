#!/bin/bash

#sudo rm -fr mysql_data pocket5_config

NETWORK=pocket5_network

if docker network inspect $NETWORK &> /dev/null; then
  echo "'$NETWORK' ready"
else
  if test -f "/var/lib/docker/network/$(docker network create --format '{{.ID}}' $NETWORK)"; then
    echo "'$NETWORK' ready but non started manually"
  else
    echo "Create network '$NETWORK'"
    docker network create ai_network
  fi
fi


if [ ! -d mariadb_data ]; then
  mkdir -p mariadb_data;

  read -s -p "Insert passwd for MariaDB root user: " MARIADB_ROOT_PWD
  echo
  echo "CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY '$MARIADB_ROOT_PWD';" > create_root_user.sql
  echo "GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;" >> create_root_user.sql
  echo "FLUSH PRIVILEGES;" >> create_root_user.sql

  echo "MARIADB_ROOT_PWD=$MARIADB_ROOT_PWD" > .env
fi

if [ ! -d pocket5_config ]; then
  mkdir -p pocket5_config

  if [ -n "$MARIADB_ROOT_PWD" ]; then
    until docker exec -it db mysqladmin -u root -p$MARIADB_ROOT_PWD ping > /dev/null 2>&1; do
      echo "Waiting for MySQL to start..."
      sleep 5
    done

    # Aggiorna il file YAML con la variabile $MARIADB_ROOT_PWD
    new_yaml_content=$(echo "$yaml_content" | sed -e "s/password: .*/password: \"$MARIADB_ROOT_PWD\"/")
    echo "$new_yaml_content" > scripts/pocket5_config.yaml

    docker exec -i db mysql -u root -p$MARIADB_ROOT_PWD < create_root_user.sql
    rm create_root_user.sql
  else
    echo "ERROR: $MARIADB_ROOT_PWD is not set. Please provide the password."
  fi

fi


docker compose up -d

if [ -n "$MARIADB_ROOT_PWD" ]; then

  until docker exec -it db mysqladmin -u root -p$MARIADB_ROOT_PWD ping > /dev/null 2>&1; do
    echo "Waiting for MySQL to start..."
    sleep 5
  done

  docker exec -i db mysql -u root -p$MARIADB_ROOT_PWD < create_root_user.sql

  rm create_root_user.sql
else
  echo "ERROR: $MARIADB_ROOT_PWD is not set. Please provide the password."
fi

echo "End!"
