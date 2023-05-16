package com.ivr.dbstorebatch.datasource;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DatabaseUtil {

    @Value("${datasource.url}")
    private String url;

    @Value("${datasource.name}")
    private String name;

    @Value("${datasource.password}")
    private String password;

    @Value("${datasource.dbname}")
    private String dbname;

    @Value("${datasource.loginTimeout}")
    private int loginTimeout;

    @Value("${datasource.queryTimeout}")
    private int queryTimeout;

    /**
     * 쿼리를 실행
     * 
     * @param params
     */
    public void executeQuery(List<String> params) {

        SQLServerDataSource dataSource = new SQLServerDataSource();
        dataSource.setURL(url);
        dataSource.setUser(name);
        dataSource.setPassword(password);
        dataSource.setDatabaseName(dbname);
        dataSource.setLoginTimeout(loginTimeout);
        dataSource.setQueryTimeout(queryTimeout);
        dataSource.setEncrypt("true");
        dataSource.setTrustServerCertificate(true);

        String insertQuery = null;
        ClassPathResource resource = new ClassPathResource("insert.sql");
        try (FileInputStream fis = new FileInputStream(resource.getFile());) {
            insertQuery = new String(fis.readAllBytes());
        } catch (IOException e) {
            log.error(e.toString());
            return;
        }

        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement();) {

            for (var param : params) {
                stmt.addBatch(new String(insertQuery).replaceAll("\\?", "'" + param + "'"));
                log.debug(new String(insertQuery).replaceAll("\\?", "'" + param + "'"));
            }

            stmt.executeBatch();
        } catch (Exception e) {
            log.error(e.toString());
        }
    }
}
