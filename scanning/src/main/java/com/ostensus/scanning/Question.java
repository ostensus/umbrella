/**
 * Copyright (c) 2014 RelOps Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ostensus.scanning;

import java.util.Set;

public interface Question {

  Set<ScanFilter> getFilters();
  Set<ScanPartition> getAggregations();
  int getSliceThreshold();

}
