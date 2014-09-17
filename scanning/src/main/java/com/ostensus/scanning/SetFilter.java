/**
 * Copyright (c) 2014 RelOps Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ostensus.scanning;

import java.util.Set;

public class SetFilter extends AbstractScanFilter {

  private final Set<String> values;

  public SetFilter(String name, Set<String> values) {
    super(name);

    this.values = values;
  }

  public Set<String> getValues() {
    return values;
  }

  public boolean contains(String value) {
    return values.contains(value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    SetFilter that = (SetFilter) o;

    if (values != null ? !values.equals(that.values) : that.values != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (values != null ? values.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "SetConstraint{" +
        "name=" + getAttributeName() +
        ", values=" + values +
        '}';
  }
}
