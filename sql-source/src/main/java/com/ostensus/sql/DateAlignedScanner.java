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

import java.sql.Date;
import java.time.temporal.ChronoField;
import java.util.Set;

public class DateAlignedScanner extends SQLScanner {

  private static final Field<Date> day = DSL.field("DAY", DSL.getDataType(Date.class));
  private static final Field<Date> month = DSL.field("MONTH", DSL.getDataType(Date.class));
  private static final Field<Date> year = DSL.field("YEAR", DSL.getDataType(Date.class));

  private DatePartition datePartition;

  private Field<Date> alignedByDay;
  private Field<Date> alignedByMonth;
  private Field<Date> alignedByYear;

  public DateAlignedScanner(DSLContext ctx, SourceTable src, int maxSliceSize, SQLDialect dialect) {
    super(ctx, src, maxSliceSize, dialect);
  }

  private SelectFinalStep selectStep(DateGranularityEnum granularity) {
    switch (granularity) {
      case YEARLY: return yearly();
      case MONTHLY: return monthly();
      default: return daily();
    }
  }

  @Override
  protected Cursor<Record> execute() {
    return selectStep(datePartition.getGranularity()).fetchLazy();
  }

  @Override
  protected Answer unmarshal(Record record) {

    String dateComponent = record.getValue(year).toLocalDate().get(ChronoField.YEAR) + "";
    String digestValue = record.getValue(digest).toString();

    return new Answer(dateComponent, digestValue);
  }

  @Override
  protected void configurePartitions() {
    Field<?> underlyingPartition = this.sourceTable.getPartition();
    Field<?> LHS_PARTITION = lhs.field(underlyingPartition);
    this.alignedByDay = truncDate(LHS_PARTITION, "DD");
    this.alignedByMonth = truncDate(day, "MM");
    this.alignedByYear = truncDate(month, "YY");
  }

  @Override
  protected void setDatePartition(Set<ScanPartition> aggregations) {
    if (datePartition == null) {
      Field f = this.sourceTable.getPartition();
      datePartition = new DatePartition(this.sourceTable.getPartition().getName(), DateGranularityEnum.YEARLY);
      if (aggregations != null) {
        for (ScanPartition agg : aggregations) {
          if (agg instanceof DatePartition) {
            datePartition = (DatePartition) agg;
          }
        }
      }
    }
  }

  private Field<Date> truncDate(Field<?> column, String granularity) {
    String truncFunction = "trunc({0}, {1})";
    return DSL.field(truncFunction, SQLDataType.DATE, column, DSL.inline(granularity));
  }

  private <T,U>SelectLimitStep step(Field<T> f1, Field<U> f1a, Field<Object> digest, Field<U> f2a, SelectLimitStep next) {
    return ctx.select(f1.as(f1a.getName()), digest(digest, f2a)).
        from(next).
        groupBy(f1).
        orderBy(f1a);
  }

  private SelectLimitStep yearly() {
    return step(alignedByYear, year, digest, month, monthly());
  }

  private SelectLimitStep monthly() {
    return step(alignedByMonth, month, digest, day, daily());
  }

  private SelectLimitStep daily() {
    return ctx.select(day, digest(digest, bucket)).
        from(aligned()).
        groupBy(day).
        orderBy(day);
  }

  private SelectLimitStep aligned() {
    return ctx.select(day, bucket, digest(version, id)).
        from(sliced()).
        groupBy(day, bucket).
        orderBy(day, bucket);
  }

  private SelectLimitStep sliced() {
    return ctx.select(
        alignedByDay.as(day.getName()),
        lhs_id.as(id.getName()),
        lhs_version.as(version.getName()),
        bucketCount.as(bucket.getName())).
        from(lhs).join(rhs).on(lhs_id.ge(rhs_id)).
        where(filters).
        groupBy(alignedByDay, lhs_id, lhs_version).
        orderBy(alignedByDay, bucket);
  }
}

