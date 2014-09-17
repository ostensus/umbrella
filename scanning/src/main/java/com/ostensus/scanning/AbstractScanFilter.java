/**
 * Copyright (c) 2014 RelOps Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ostensus.scanning;

public abstract class AbstractScanFilter implements ScanFilter {
  private final String attributeName;

  public AbstractScanFilter(String attributeName) {
    this.attributeName = attributeName;
  }

  @Override
  public String getAttributeName() {
    return attributeName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AbstractScanFilter that = (AbstractScanFilter) o;

    if (attributeName != null ? !attributeName.equals(that.attributeName) : that.attributeName != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return attributeName != null ? attributeName.hashCode() : 0;
  }
}
