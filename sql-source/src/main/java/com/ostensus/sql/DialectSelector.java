/**
 * Copyright (c) 2014 RelOps Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ostensus.sql;

import org.jooq.SQLDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DialectSelector {

  static Logger log = LoggerFactory.getLogger(DialectSelector.class);

  public static SQLDialect getDialect(String dialect) {

    if (dialect == null || dialect.isEmpty()) {
      throw new RuntimeException("No SQL dialect was specified");
    }
    if (dialect.equals("hsqldb")) {
      return SQLDialect.HSQLDB;
    }
    else {
      throw new RuntimeException("Unsupported SQL dialect: " + dialect);
    }
  }
}
