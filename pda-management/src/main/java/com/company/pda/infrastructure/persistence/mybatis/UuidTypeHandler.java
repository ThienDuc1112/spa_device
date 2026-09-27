package com.company.pda.infrastructure.persistence.mybatis;

import java.sql.*;
import java.util.UUID;
import org.apache.ibatis.type.*;

@MappedTypes(UUID.class)
public class UuidTypeHandler extends BaseTypeHandler<UUID> {
  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, UUID value, JdbcType type)
      throws SQLException {
    ps.setObject(i, value);
  }

  @Override
  public UUID getNullableResult(ResultSet rs, String column) throws SQLException {
    return rs.getObject(column, UUID.class);
  }

  @Override
  public UUID getNullableResult(ResultSet rs, int column) throws SQLException {
    return rs.getObject(column, UUID.class);
  }

  @Override
  public UUID getNullableResult(CallableStatement cs, int column) throws SQLException {
    return cs.getObject(column, UUID.class);
  }
}
