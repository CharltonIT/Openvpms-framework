
========================
Preparing your workspace
========================
1. Edit/create build.properties and add properties for the database. E.g:

hibernate.dialect=org.hibernate.dialect.MySQL5InnoDBDialect
hibernate.connection.driver_class=com.mysql.jdbc.Driver
hibernate.connection.url=jdbc:mysql://localhost:3306/openvpms
hibernate.connection.username=openvpms
hibernate.connection.password=openvpms

   
======================      
Preparing the database
======================
1. Create a database called 'openvpms'

2. Execute 'maven hibernate:schema-export to create the appropriate tables in
   the database. NOTE: THIS WILL DELETE DROP AND RECREATE THE TABLES
   
   
================
Database Testing
================
The openvpms-framework component has been tested with mysql 5.0.24 using the
Connector/J 5.1.5-bin JDBC driver .

++++++++++++++++++++++++++++++++++++++++++++++++++++++++

Current Build Status:
[![Build Status](https://travis-ci.org/CharltonIT/Openvpms-framework.svg?branch=master)](https://travis-ci.org/CharltonIT/Openvpms-framework)

++++++++++++++++++++++++++++++++++++++++++++++++++++++++
