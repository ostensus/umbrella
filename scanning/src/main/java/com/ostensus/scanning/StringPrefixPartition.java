/**
 * Copyright (c) 2014 RelOps Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ostensus.scanning;

import java.util.NavigableSet;
import java.util.TreeSet;

public class StringPrefixPartition extends AbstractScanPartition {

  public static final NavigableSet<Integer> DEFAULT_OFFSETS = new TreeSet<Integer>();

  static {
    DEFAULT_OFFSETS.add(1);
  }

  private final NavigableSet<Integer> offsets;


  public StringPrefixPartition(String name, String parent, String... offsets) {
    this(name, parent, parseOffsets(offsets));
  }

  public StringPrefixPartition(String name, String parent, NavigableSet<Integer> offsets) {
    super(name, parent);

    this.offsets = offsets;
  }

  public NavigableSet<Integer> getOffsets() {
    return offsets;
  }

  @Override
  public String bucket(String attributeVal) {
    int length = (parent == null || parent.length() == 0) ? offsets.first() : offsets.higher(parent.length());
    if (attributeVal.length() <= length) return attributeVal;
    return attributeVal.substring(0, length);
  }



  public static NavigableSet<Integer> parseOffsets(String ... args) {

    if (args == null || args.length == 0) {
      return DEFAULT_OFFSETS;
    } else {

      TreeSet<Integer> offsets = new TreeSet<Integer>();

      for (String arg : args) {
        offsets.add(Integer.parseInt(arg));
      }

      return offsets;
    }

  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof StringPrefixPartition)) return false;
    if (!super.equals(o)) return false;

    StringPrefixPartition that = (StringPrefixPartition) o;

    if (offsets != null ? !offsets.equals(that.offsets) : that.offsets != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (offsets != null ? offsets.hashCode() : 0);
    return result;
  }
}

