/**
 * Copyright (c) 2014 RelOps Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ostensus.scanning;

public class DatePartition extends AbstractScanPartition implements GranularityPartition {

  private final DateGranularityEnum granularity;

  public DatePartition(String name, DateGranularityEnum granularity) {
    this(name, granularity, null);
  }

  public DatePartition(String name, DateGranularityEnum granularity, String parent) {
    super(name, parent);

    this.granularity = granularity;
  }

  @Override
  public String bucket(String attributeVal) {
    return null;
  }

  public DateGranularityEnum getGranularity() {
    return granularity;
  }

  @Override
  public String getGranularityString() {
    return granularity.toString().toLowerCase();
  }

  public static DateGranularityEnum parseGranularity(String granStr) {
    String title =  Character.toUpperCase(granStr.charAt(0)) + granStr.substring(1).toLowerCase();
    return DateGranularityEnum.valueOf(title);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    DatePartition that = (DatePartition) o;

    if (granularity != that.granularity) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (granularity != null ? granularity.hashCode() : 0);
    return result;
  }
}

