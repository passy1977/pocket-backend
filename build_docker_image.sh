#!/bin/bash

sudo rm -fr docker_data

NETWORK=pocket5_network

if docker network inspect $NETWORK &> /dev/null; then
  echo "'$NETWORK' ready"
else
  #if test -f "$(sudo docker network ls --format "{{ .Name }}" | grep  pocket5_network)"; then
  if [ "$(sudo docker network ls --format "{{ .Name }}" | grep  $NETWORK)" = $NETWORK ]; then
    echo "'$NETWORK' ready but non started manually"
  else
    echo "Create network '$NETWORK'"
    sudo docker network create $NETWORK
  fi
fi

if [ ! -d docker_data ]; then
  mkdir -p docker_data
fi

if [ ! -d docker_data/mariadb ]; then
  mkdir -p docker_data/mariadb;

  read -s -p "Insert passwd for MariaDB root user: " MARIADB_ROOT_PWD
  echo
  echo "CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY '$MARIADB_ROOT_PWD';" > docker_data/mariadb/create_root_user.sql
  echo "GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;" >> docker_data/mariadb/create_root_user.sql
  echo "FLUSH PRIVILEGES;" >> docker_data/mariadb/create_root_user.sql

  echo "MARIADB_ROOT_PWD=$MARIADB_ROOT_PWD" > .env
fi

if [ ! -d docker_data/pocket5 ]; then
  if [ -z "$MARIADB_ROOT_PWD" ]; then
      echo "MARIADB_ROOT_PWD empty"
      exit 1
  fi
  read -p "Set URL for remote sonnection (es http://xxxxx:8081): " URL
  read -p "AES CBC IV (16 char are mandatory): " AES_CBC_IV
  if [[ ${#AES_CBC_IV} -ne 16 ]]; then
      echo "AES CBC IV don't contain a string of 16 chars" 1>&2
      exit 1
  fi

  read -p "Auth email: " AUTH_USER
  read -s -p "Auth passwd (32 char are mandatory): " AUTH_PASSWD
  echo

  if [[ ${#AUTH_PASSWD} -ne 32 ]]; then
      echo "Auth passwd don't contain a string of 32 chars" 1>&2
      exit 1
  fi

  mkdir -p docker_data/pocket5

  sed -e "s/MARIADB_ROOT_PWD/$MARIADB_ROOT_PWD/g" \
      -e "s#URL#$URL#g" \
      -e "s/AES_CBC_IV/$AES_CBC_IV/g" \
      -e "s/AUTH_USER/$AUTH_USER/g" \
      -e "s/AUTH_PASSWD/$AUTH_PASSWD/g" scripts/pocket5-config.yaml > docker_data/pocket5/pocket5-config.yaml
  
  cp docker_data/pocket5/pocket5-config.yaml src/main/resources/application.yaml
fi


sudo docker compose up -d

if [ -n "$MARIADB_ROOT_PWD" ]; then

  echo "Waiting for MariaDB to start..."
  sleep 15

#  sudo docker exec -i db mariadb -u root -p$MARIADB_ROOT_PWD < docker_data/mariadb/create_root_user.sql
  sleep 1
  sudo docker exec -i db mariadb -u root -p$MARIADB_ROOT_PWD < scripts/pocket5.sql

  sudo rm -f docker_data/mariadb/create_root_user.sql
else
  echo "ERROR: $MARIADB_ROOT_PWD is not set. Please provide the password" 1>&2
fi

echo "End!"
