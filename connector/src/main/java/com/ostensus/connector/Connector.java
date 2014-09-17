/**
 * Copyright (c) 2014 RelOps Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ostensus.connector;

import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;
import com.ostensus.connector.plumbing.DatabaseWrapper;
import com.ostensus.sql.SQLParameters;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ostensus.connector.schema.tables.UniqueRepositoryNames.UNIQUE_REPOSITORY_NAMES;
import static com.ostensus.connector.schema.tables.SqlRepositories.SQL_REPOSITORIES;

import java.sql.Timestamp;

public class Connector {

  static Logger log = LoggerFactory.getLogger(Connector.class);

  private DatabaseWrapper db;

  public Connector(ConnectorConfiguration config) {

    String cwd = System.getProperty("user.dir");
    String url = String.format("jdbc:h2:%s/.db/connector", cwd);

    BoneCPConfig bone = new BoneCPConfig();
    bone.setJdbcUrl(url);
    bone.setUsername("sa");
    bone.setPassword("");

    BoneCPDataSource ds = new BoneCPDataSource(bone);

    Flyway flyway = new Flyway();
    flyway.setDataSource(ds);

    flyway.repair();

    try {

      flyway.migrate();

    } catch (FlywayException e) {

      if (e.getMessage().contains("init()")) {

        log.warn("Caught flyway exception that may require the metadata to be initialized first, attempting this now");

        flyway.init();
        flyway.migrate();
      }
      else {
        throw new RuntimeException(e);
      }
    }

    db = new DatabaseWrapper(ds, SQLDialect.H2);

  }

  public void register(String name, SQLParameters params) {

    StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
    encryptor.setPassword("password");

    db.executeVoid(ctx -> {

      ctx.insertInto(UNIQUE_REPOSITORY_NAMES).
          set(UNIQUE_REPOSITORY_NAMES.NAME, name).
          execute();

      ctx.insertInto(SQL_REPOSITORIES).
          set(SQL_REPOSITORIES.NAME, name).
          set(SQL_REPOSITORIES.URL, params.getUrl()).
          set(SQL_REPOSITORIES.DIALECT, SQLDialect.H2.getNameUC()).
          set(SQL_REPOSITORIES.USERNAME, params.getUsername()).
          set(SQL_REPOSITORIES.PASSWORD, encryptor.encrypt(params.getPassword())).
          execute();

    });

    db.executeVoid(ctx -> ctx.select(DSL.md5(SQL_REPOSITORIES.NAME)).from(SQL_REPOSITORIES).fetchMany());

    log.info("Connector started");
  }
}
