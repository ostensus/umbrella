/**
 * Copyright (c) 2014 RelOps Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ostensus.sql;

import com.ostensus.scanning.*;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

public class SQLSource implements Scannable {

  private SourceTable src;
  private DataSource ds;
  private SQLDialect dialect;

  public SQLSource(DataSource ds, SourceTable src, SQLDialect dialect) {
    this.ds = ds;
    this.dialect = dialect;
    this.src = src;
  }


  protected DSLContext getContext(Connection c) {
    if (dialect == SQLDialect.POSTGRES) {
      Settings settings = new Settings();
      settings.setRenderNameStyle(RenderNameStyle.LOWER);
      return DSL.using(c, dialect, settings);
    } else {
      return DSL.using(c, dialect);
    }
  }

  protected void closeConnection(Connection connection) {
    closeConnection(connection, false);
  }

  protected void closeConnection(Connection connection, boolean shouldCommit) {
    try {
      if (shouldCommit) {
        connection.commit();
      }
      connection.close();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  protected Connection getConnection() {
    Connection c;

    try {
      c = ds.getConnection();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return c;
  }

  @Override
  public void scan(Set<ScanFilter> filters, Set<ScanPartition> parts, int sliceThreshold, PruningHandler handler) {
    Connection connection = getConnection();
    DSLContext ctx = getContext(connection);

    SQLScanner scanner = new DateAlignedScanner(ctx, src, sliceThreshold, dialect);
    if (parts != null) {
      if (parts.size() == 1) {
        ScanPartition head = parts.iterator().next();
        if (head instanceof DatePartition) {
          scanner = new DateAlignedScanner(ctx, src, sliceThreshold, dialect);
        }
      }
    }

    scanner.scan(filters, parts, handler);

    handler.onCompletion();

    closeConnection(connection);
  }
}
