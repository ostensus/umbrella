/**
 * Copyright (c) 2014 RelOps Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ostensus.scanning;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

public class BufferedPruningHandler implements PruningHandler {

  private final Set<Answer> entries;

  public BufferedPruningHandler() {

    this.entries = new LinkedHashSet<Answer>();
  }

  public BufferedPruningHandler(Comparator<Answer> comparator) {
    this.entries = new TreeSet<Answer>(comparator);
  }

  @Override
  public void onPrune(Answer entry) {
    entries.add(entry);
  }

  @Override
  public void onCompletion() {}

  public Set<Answer> getAnswers() {
    return entries;
  }
}

