/**
 * Copyright (c) 2014 RelOps Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ostensus.sql;

import com.ostensus.scanning.*;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;

public abstract class SQLScanner {

  public static final String DELIM = "";

  protected static final Field<Object> bucket = DSL.field("BUCKET");
  protected static final Field<Object> version = DSL.field("VERSION");
  protected static final Field<Object> id = DSL.field("ID");
  protected static final Field<Object> digest = DSL.field("DIGEST");

  protected final DSLContext ctx;
  protected final int sliceThreshold;

  protected Table lhs;
  protected Table rhs;
  protected Field<String> lhs_id;
  protected Field<String> rhs_id;
  protected Field<String> lhs_version;
  protected Field<Integer> bucketCount;

  protected SQLDialect dialect;

  protected final List<Condition> filters = new ArrayList<Condition>();

  protected final SourceTable sourceTable;

  public SQLScanner(DSLContext ctx, SourceTable source, int sliceThreshold, SQLDialect dialect) {
    this.ctx = ctx;
    this.sourceTable = source;
    this.sliceThreshold = sliceThreshold;
    this.dialect = dialect;

    this.lhs = sourceTable.as("lhs");
    this.rhs = sourceTable.as("rhs");
    this.lhs_id = this.lhs.field(sourceTable.getId());
    this.rhs_id = this.rhs.field(sourceTable.getId());
    this.lhs_version = lhs.field(sourceTable.getVersion());

    this.bucketCount = cast(ceil(cast(count(), SQLDataType.REAL).div(sliceThreshold)), SQLDataType.INTEGER);
  }

  public void scan(Set<ScanFilter> constraints, Set<ScanPartition> aggregations, PruningHandler handler) {
    configureFields(constraints);
    setDatePartition(aggregations);
    configurePartitions();
    setFilters(constraints);

    Cursor<Record> cursor = execute();

    while (cursor.hasNext()) {
      Record record = cursor.fetchOne();
      Answer answer = unmarshal(record);
      handler.onPrune(answer);
    }
    cursor.close();
  }

  protected abstract void setDatePartition(Set<ScanPartition> aggregations);
  protected abstract Cursor<Record> execute();
  protected abstract void configurePartitions();
  protected abstract Answer unmarshal(Record record);

  protected Field<String> digest(Field<Object> digestee, Field<?> orderBy) {
    return DSL.md5(groupConcat(digestee).orderBy(orderBy.asc()).separator(DELIM)).as(digest.getName());
  }

  protected void configureFields(Set<ScanFilter> filters) {
    if (filters != null) {
      for (ScanFilter filter : filters) {
        sourceTable.filterBy(filter.getAttributeName(), SQLDataType.VARCHAR);
      }
    }
  }

  private void setFilters(Set<ScanFilter> filters) {
    if (filters != null) {
      for (ScanFilter filter : filters) {
        if (filter instanceof SetFilter) {
          SetFilter setFilter = (SetFilter) filter;

          // TODO This shouldn't need to hard coded to be upper-cased
          final String attributeName = setFilter.getAttributeName().toUpperCase();

          Field<String> lhs_set = (Field<String>) lhs.field(attributeName);

          if (lhs_set == null) {
            String msg = String.format("Table (%s) has no field for filter attribute (%s)", sourceTable.getName(), attributeName);
            throw new InvalidMetadataException(msg);
          }

          Condition lhs = getCondition(setFilter, lhs_set);
          this.filters.add(lhs);
        }
      }
    }
  }

  private Condition getCondition(SetFilter setFilter, Field<String> field) {
    return setFilter.getValues().stream().map(field::eq).reduce((a, b) -> a.or(b)).get();
  }
}

