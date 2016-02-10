/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.cettia.asity.action;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple implementation of {@link Actions}.
 *
 * @author Donghwan Kim
 */
public class SimpleActions<T> extends AbstractActions<T> {

  private boolean disabled;
  private boolean fired;
  private T cached;

  public SimpleActions() {
    super();
  }

  public SimpleActions(Actions.Options o) {
    super(o);
  }

  @Override
  protected List<Action<T>> createList() {
    return new ArrayList<>();
  }

  @Override
  protected void setCache(T data) {
    this.cached = data;
  }

  @Override
  protected T cached() {
    return cached;
  }

  @Override
  protected boolean setDisabled() {
    boolean answer = !disabled;
    if (answer) {
      disabled = true;
    }
    return answer;
  }

  @Override
  public boolean disabled() {
    return disabled;
  }

  @Override
  protected void setFired() {
    fired = true;
  }

  @Override
  public boolean fired() {
    return fired;
  }

}
