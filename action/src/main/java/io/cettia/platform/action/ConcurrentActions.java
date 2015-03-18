/*
 * Copyright 2015 The Cettia Project
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
package io.cettia.platform.action;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Thread-safe implementation of {@link Actions}.
 *
 * @author Donghwan Kim
 */
public class ConcurrentActions<T> extends AbstractActions<T> {

    private final AtomicBoolean disabled = new AtomicBoolean();
    private final AtomicBoolean fired = new AtomicBoolean();
    private final AtomicReference<T> cached = new AtomicReference<>();

    public ConcurrentActions() {
        super();
    }

    public ConcurrentActions(Actions.Options o) {
        super(o);
    }

    @Override
    protected List<Action<T>> createList() {
        return new CopyOnWriteArrayList<>();
    }

    @Override
    protected void setCache(T data) {
        this.cached.set(data);
    }

    @Override
    protected T cached() {
        return cached.get();
    }

    @Override
    protected boolean setDisabled() {
        return disabled.compareAndSet(false, true);
    }

    @Override
    public boolean disabled() {
        return disabled.get();
    }

    @Override
    protected void setFired() {
        fired.set(true);
    }

    @Override
    public boolean fired() {
        return fired.get();
    }

}
