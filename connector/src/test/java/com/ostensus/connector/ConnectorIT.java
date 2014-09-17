/**
 * Copyright (c) 2014 RelOps Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ostensus.connector;

import com.ostensus.sql.SQLParameters;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

public class ConnectorIT {

  @Test
  public void bootstrap() throws Exception {

    ConnectorConfiguration conf = new ConnectorConfiguration();
    Connector con = new Connector(conf);

    SQLParameters params = new SQLParameters();
    params.setUrl(RandomStringUtils.randomAlphanumeric(10));
    params.setUsername(RandomStringUtils.randomAlphanumeric(10));
    params.setPassword(RandomStringUtils.randomAlphanumeric(10));

    String name = RandomStringUtils.randomAlphanumeric(10);

    con.register(name, params);

  }
}
