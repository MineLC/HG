#!/bin/bash

# Start MongoDB
docker-entrypoint.sh mongod &

# Wait for MongoDB to start
sleep 5

# Authenticate and create databases and users
mongosh -u test -p yourpassword <<EOF
use chg;
db.createCollection("test");
use lccore;
db.createCollection("test");;
db.runCommand({
  createUser: "test",
  pwd: "yourpassword",
  roles: [
    { role: "readWrite", db: "lccore" },
  ]
});
use chg;
db.runCommand({
  createUser: "test",
  pwd: "yourpassword",
  roles: [
    { role: "readWrite", db: "chg" }
  ]
});
db.getMongo().getDBNames().indexOf("chg") >= 0 && db.getMongo().getDBNames().indexOf("lccore") >= 0 ? print("Databases created successfully") : print("Database creation failed");
EOF
