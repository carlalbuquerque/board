package br.com.dio.persistence.config;

import lombok.NoArgsConstructor;


import static lombok.AccessLevel.PRIVATE;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


@NoArgsConstructor(access = PRIVATE)
public final class ConnectionConfig {

    public static Connection getConnection()throws SQLException{

        var  url = "jdbc:mysql://localhost/board";
        var  user = "root";
        var password = "456645";
        var connection  = DriverManager.getConnection(url, user ,password);
        connection.setAutoCommit(false);
        return connection;

    }




}

