#Tasks4Squares API

Pretty simple API with 3 main parts: Authentication, User account operations and operations under Tasks.
Authentication is token based.

####Application
Simple REST Api services built on top of Play Framework 2. MongoDB used as data storage and the database access library is ReactiveMongo.

####Tests
Tests are implemented using specs2 library and describes the common usage of the API.

Could be started from typesafe-activator console using `test` command, or being run separately by `test-only` command.
