# Introduction

This is a simple chat server and client built for learning Cats-effect concurrency functionality. Then functionality is basic where it only supports sending group chats. It has basic support for a queue of messages.

# Tech stack

- Scala
- Cats-effect
- Cats

# Known issues

- The application is assigning 16 threads to the Network IO pool. Thereby only 16 clients can connect. Need to proper investigation into finding out why that is and how to add unlimited connections
- When a client disconnects the connection is closed properly