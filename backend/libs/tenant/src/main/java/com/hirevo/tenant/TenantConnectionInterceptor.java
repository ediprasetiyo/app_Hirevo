package com.hirevo.tenant;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import org.hibernate.HibernateException;
import org.hibernate.MultiTenancyStrategy;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;

/**
 * Sets {@code SET LOCAL app.current_tenant_id} on every acquired JDBC connection
 * so PostgreSQL RLS policies enforce row isolation transparently.
 *
 * <p>This is the DEFENSE-IN-DEPTH layer — even if application code accidentally
 * queries across tenants, the DB refuses.
 */
@Component
public class TenantConnectionInterceptor implements MultiTenantConnectionProvider<UUID> {

  private final DataSource dataSource;

  @Autowired
  public TenantConnectionInterceptor(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Connection getAnyConnection() throws SQLException {
    return DataSourceUtils.getConnection(dataSource);
  }

  @Override
  public void releaseAnyConnection(Connection connection) throws SQLException {
    DataSourceUtils.releaseConnection(connection, dataSource);
  }

  @Override
  public Connection getConnection(UUID tenantId) throws SQLException {
    Connection conn = getAnyConnection();
    try (Statement st = conn.createStatement()) {
      // Use SET LOCAL so the setting is scoped to the current transaction.
      st.execute("SET LOCAL app.current_tenant_id = '" + tenantId + "'");
    }
    return conn;
  }

  @Override
  public void releaseConnection(UUID tenantId, Connection connection) throws SQLException {
    try (Statement st = connection.createStatement()) {
      st.execute("RESET app.current_tenant_id");
    } catch (SQLException ignored) {
      // best effort
    }
    releaseAnyConnection(connection);
  }

  @Override
  public boolean supportsAggressiveRelease() {
    return true;
  }

  @Override
  public boolean isUnwrappableAs(Class<?> unwrapType) {
    return false;
  }

  @Override
  public <T> T unwrap(Class<T> unwrapType) {
    return null;
  }
}
