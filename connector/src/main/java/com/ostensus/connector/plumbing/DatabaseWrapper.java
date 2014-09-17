/**
 * Copyright (c) 2014 RelOps Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ostensus.connector.plumbing;

import org.jooq.*;
import org.jooq.conf.Settings;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;

public class DatabaseWrapper {

  static Logger log = LoggerFactory.getLogger(DatabaseWrapper.class);

  private Settings settings = new Settings();

  private DataSource dataSource;
  private SQLDialect dialect;

  public DatabaseWrapper(DataSource dataSource, SQLDialect dialect) {
    this.dataSource = dataSource;
    this.dialect = dialect;
  }

  public <U extends UpdatableRecord> boolean updateNonNullFields(Table<U> t, Object o) {
    return execute(ctx -> {
          UpdatableRecord<?> record = ctx.newRecord(t);
          record.from(o);
          for (Field<?> f : record.fields()) {
            if (record.getValue(f) == null)
              record.changed(f, false);
          }
          return record.update() == 1;
        }
    );
  }

  public void executeVoid(Consumer<DSLContext> fun) {
    execute((DSLContext ctx) -> {fun.accept(ctx); return 0;});
  }

  public <T> T execute(Function<DSLContext,T> fun) {

    T result = null;
    Connection connection = null;
    DatabaseException userException = null;

    try {

      connection = dataSource.getConnection();
      connection.setAutoCommit(false);
      DSLContext ctx = DSL.using(connection, dialect, settings);

      try {

        result = fun.apply(ctx);

      } catch (Exception e) {

        String msg;

        if (e instanceof DataAccessException) {

          msg = e.getCause().getMessage();

        } else {
          msg = e.getMessage();
        }

        log.error(msg);

        if (userException == null) {
          userException = new DatabaseException(msg);
        }

        if (!connection.isClosed()) {
          connection.rollback();
        }

      }

      if (null == userException) {
        connection.commit();
      }

    } catch (SQLException e) {
      throw new DatabaseException(e.getMessage());
    } finally {

      try {

        // Release the connection back to the pool

        if (null != connection && !connection.isClosed()) {
          connection.close();
        }

      } catch (SQLException e) {
        throw new DatabaseException(e.getMessage());
      }

    }

    if (userException != null) {
      throw userException;
    } else {
      return result;
    }
  }
}
