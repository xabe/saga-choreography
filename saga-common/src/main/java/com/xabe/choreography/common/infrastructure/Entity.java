package com.xabe.choreography.common.infrastructure;

import java.io.Serializable;

public interface Entity<T> extends Serializable {

  T getId();

}
