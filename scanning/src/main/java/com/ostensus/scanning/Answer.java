/**
 * Copyright (c) 2014 RelOps Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ostensus.scanning;

public class Answer {

  private String partition,digest;

  public Answer(String partition, String digest) {
    this.partition = partition;
    this.digest = digest;
  }

  public String getPartition() {
    return partition;
  }

  public void setPartition(String partition) {
    this.partition = partition;
  }

  public String getDigest() {
    return digest;
  }

  public void setDigest(String digest) {
    this.digest = digest;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Answer answer = (Answer) o;

    if (!digest.equals(answer.digest)) return false;
    if (!partition.equals(answer.partition)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = partition.hashCode();
    result = 31 * result + digest.hashCode();
    return result;
  }
}
