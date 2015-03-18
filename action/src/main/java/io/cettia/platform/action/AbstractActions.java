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

import java.util.Collections;
import java.util.List;

/**
 * Base implementation of {@link Actions}.
 *
 * @author Donghwan Kim
 */
public abstract class AbstractActions<T> implements Actions<T> {

    private final Actions.Options options;
    protected final List<Action<T>> actionList;

    protected AbstractActions() {
        this(new Actions.Options());
    }

    protected AbstractActions(Actions.Options o) {
        this.options = new Actions.Options(o);
        this.actionList = createList();
    }

    protected abstract List<Action<T>> createList();

    @Override
    public Actions<T> add(Action<T> action) {
        if (!disabled()) {
            if (options.memory() && fired()) {
                fireOne(action, cached());
            }
            if (!options.unique()
                    || (options.unique() && !actionList.contains(action))) {
                actionList.add(action);
            }
        }
        return this;
    }

    protected abstract T cached();

    @Override
    public Actions<T> disable() {
        if (setDisabled()) {
            actionList.clear();
        }
        return this;
    }

    protected abstract boolean setDisabled();

    @Override
    public Actions<T> empty() {
        actionList.clear();
        return this;
    }

    @Override
    public Actions<T> fire() {
        return fire(null);
    }

    @Override
    public Actions<T> fire(T data) {
        if (!disabled() && !(options.once() && fired())) {
            setFired();
            if (options.memory()) {
                setCache(data);
            }
            for (int i = 0; i < actionList.size(); i++) {
                fireOne(actionList.get(i), data);
            }
        }
        return this;
    }

    protected abstract void setFired();

    protected abstract void setCache(T data);

    @Override
    public boolean has() {
        return !actionList.isEmpty();
    }

    @Override
    public boolean has(Action<T> action) {
        return actionList.contains(action);
    }

    @Override
    public Actions<T> remove(Action<T> action) {
        actionList.removeAll(Collections.singleton(action));
        return this;
    }

    protected void fireOne(Action<T> action, T data) {
        action.on(data);
    }

}
