package com.xabe.orchestation.common.infrastructure;

import java.io.Serializable;

public interface Entity<T> extends Serializable {

  T getId();

}
