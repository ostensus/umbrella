/**
 * Copyright (c) 2014 RelOps Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ostensus.sql;

import com.google.common.collect.ImmutableSet;
import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;
import com.ostensus.scanning.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.jooq.DSLContext;
import org.jooq.impl.SQLDataType;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class SQLSourceIT {
  static Logger log = LoggerFactory.getLogger(SQLSourceIT.class);

  private SQLSource source;


  @Before
  public void setUp() {

    InputStream is = getClass().getClassLoader().getResourceAsStream("hsqldb.conf");
    Yaml yaml = new Yaml();
    SQLParameters params = yaml.loadAs(is, SQLParameters.class);

    BoneCPConfig config = new BoneCPConfig();
    config.setJdbcUrl(params.getUrl());
    config.setUsername(params.getUsername());
    config.setPassword(params.getPassword());

    BoneCPDataSource ds = new BoneCPDataSource(config);

    SourceTable src = new SourceTable("CALL_RECORDS");
    src.includeId("IMSI", SQLDataType.VARCHAR);
    src.includeVersion("CALLING_NUMBER", SQLDataType.VARCHAR);
    src.includePartition("TIMESTAMP", SQLDataType.DATE);

    source = new SQLSource(ds, src, DialectSelector.getDialect(params.getType()));

    Flyway flyway = new Flyway();
    flyway.setLocations("db.migration." + params.getType());
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

  }

  @Before
  public void seed() throws Exception {

    int days = 1300;

    Connection connection = source.getConnection();

    DSLContext ctx = source.getContext(connection);
    ctx.execute("TRUNCATE TABLE call_records;");

    LocalDateTime begin = LocalDateTime.of(2012, 8, 3, 22, 21, 31, 0);

    String sql = "INSERT INTO call_records (imsi, timestamp, duration, calling_number, called_number) VALUES (?,?,?,?,?)";

    for (int i = 0; i < days; i++) {
      String imsi = 230023741299234L + i + "";
      int duration = i;
      Timestamp ts = Timestamp.valueOf(begin.plusDays(i));
      String caller = 220082769234739L + i + "";
      String callee = 275617294783934L + i + "";
      ctx.execute(sql, imsi, ts, duration, caller, callee);
    }

    connection.commit();
    source.closeConnection(connection, true);
  }

  @Test
  public void shouldPartitionByDate() throws Exception {

    ScanPartition datePartition = new DatePartition("timestamp", DateGranularityEnum.YEARLY, null);

    Set<ScanFilter> filters = null;
    Set<ScanPartition> parts = ImmutableSet.of(datePartition);

    BufferedPruningHandler handler = new BufferedPruningHandler();
    source.scan(filters, parts, 127, handler);

    Set<Answer> expectedResults = new LinkedHashSet<>();
    expectedResults.add(new Answer("2012", "5c3ed162a291eb14ba0f6dddef7b29e2"));
    expectedResults.add(new Answer("2013", "a37049e713cc91bbe2509413f87e6526"));
    expectedResults.add(new Answer("2014", "4862eaab3850ee3b9e1bba2bd6f112f9"));
    expectedResults.add(new Answer("2015", "881235598ff2f8278e7e3871565e7b1b"));
    expectedResults.add(new Answer("2016", "1de7e6e918e1e9e47a73edc5fda112a2"));

    assertEquals(expectedResults, handler.getAnswers());
  }



  private static String md5(String msg) {
    return DigestUtils.md5Hex(msg);
  }
}
