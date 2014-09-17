/**
 * Copyright (c) 2014 RelOps Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ostensus.scanning;

public abstract class AbstractScanPartition implements ScanPartition {

  protected final String attrName;
  protected final String parent;

  public AbstractScanPartition(String attrName, String parent) {
    this.attrName = attrName;
    this.parent = parent;
  }

  @Override
  public String getAttributeName() {
    return attrName;
  }

  @Override
  public String getParent() {
    return parent;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AbstractScanPartition)) return false;

    AbstractScanPartition that = (AbstractScanPartition) o;

    if (attrName != null ? !attrName.equals(that.attrName) : that.attrName != null) return false;
    if (parent != null ? !parent.equals(that.parent) : that.parent != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = attrName != null ? attrName.hashCode() : 0;
    result = 31 * result + (parent != null ? parent.hashCode() : 0);
    return result;
  }
}
