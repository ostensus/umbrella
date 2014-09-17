/**
 * Copyright (c) 2014 RelOps Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ostensus.sql;

import com.ostensus.scanning.Answer;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.CustomTable;
import org.jooq.impl.TableImpl;

public class SourceTable extends CustomTable {

  private String id, version, partition;

  public SourceTable(String name) {
    super(name);
  }

  @Override
  public Class getRecordType() {
    return Answer.class;
  }

  public String getId() {
    return id;
  }

  public String getVersion() {
    return version;
  }

  public Field<?> getPartition() {
    return field(partition);
  }

  public void filterBy(String name, DataType<?> type) {
    createField(name, type, this);
  }

  public void includeId(String name, DataType<?> type) {
    id = name;
    createField(name, type, this);
  }

  public void includeVersion(String name, DataType<?> type) {
    version = name;
    createField(name, type, this);
  }

  public void includePartition(String name, DataType<?> type) {
    partition = name;
    createField(name, type, this);
  }
}
