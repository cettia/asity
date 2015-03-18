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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import io.cettia.platform.action.Actions.Options;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public abstract class ActionsTest {

    @Test
    public void options() {
        Actions.Options options = new Actions.Options().unique(true);
        Actions<Void> actions = createActions(options);
        final AtomicInteger count = new AtomicInteger();
        VoidAction action = new VoidAction() {
            @Override
            public void on() {
                assertThat(count.getAndIncrement(), is(0));
            }
        };
        options.unique(false);
        actions.add(action).add(action).fire();
    }

    @Test
    public void add() {
        // default, unique, memory
        Actions<String> actions = null;
        MemoryAction<String> action = null;

        actions = createActions();
        action = new MemoryAction<>();
        actions.add(action).add(action).fire("A");
        assertThat(action.memory(), contains("A", "A"));
        actions.add(action);
        assertThat(action.memory(), contains("A", "A"));

        actions = createActions(new Actions.Options().unique(true));
        action = new MemoryAction<>();
        actions.add(action).add(action).fire("A");
        assertThat(action.memory(), contains("A"));
        actions.add(action);
        assertThat(action.memory(), contains("A"));

        actions = createActions(new Actions.Options().memory(true));
        action = new MemoryAction<>();
        actions.add(action).fire("A");
        assertThat(action.memory(), contains("A"));
        actions.add(action);
        assertThat(action.memory(), contains("A", "A"));
    }

    @Test
    public void fire() {
        // default, once
        Actions<String> actions = null;
        MemoryAction<String> action = null;

        actions = createActions();
        action = new MemoryAction<>();
        actions.add(action);
        assertThat(actions.fired(), is(false));
        assertThat(action.memory(), is(empty()));
        actions.fire("H");
        assertThat(actions.fired(), is(true));
        assertThat(action.memory(), contains("H"));
        actions.fire("A");
        assertThat(actions.fired(), is(true));
        assertThat(action.memory(), contains("H", "A"));

        actions = createActions(new Actions.Options().once(true));
        action = new MemoryAction<>();
        actions.add(action);
        assertThat(actions.fired(), is(false));
        assertThat(action.memory(), is(empty()));
        actions.fire("H");
        assertThat(actions.fired(), is(true));
        assertThat(action.memory(), contains("H"));
        actions.fire("A");
        assertThat(actions.fired(), is(true));
        assertThat(action.memory(), contains("H"));
    }

    @Test
    public void disable() {
        Actions<Void> actions = createActions();
        Action<Void> action = new EmptyAction<>();

        actions.add(action);
        assertThat(actions.disabled(), is(false));
        actions.disable();
        assertThat(actions.disabled(), is(true));

        actions = createActions();
        actions.disable().add(action);
        assertThat(actions.has(action), is(false));

        actions = createActions();
        actions.disable().disable();

        actions = createActions();
        actions.disable().fire();

        final Actions<String> actions2 = createActions();
        MemoryAction<String> action2 = new MemoryAction<>();
        actions2.add(action2).add(new Action<String>() {
            @Override
            public void on(String _) {
                actions2.disable();
            }
        })
                .add(action2).fire("A");
        assertThat(action2.memory(), contains("A"));
    }

    @Test
    public void emptyMethod() {
        Actions<Void> actions = createActions();

        assertThat(actions.has(), is(false));
        actions.add(new EmptyAction<Void>());
        assertThat(actions.has(), is(true));
        actions.empty();
        assertThat(actions.has(), is(false));
    }

    @Test
    public void remove() {
        Actions<Void> actions = createActions();
        Action<Void> actionA = new EmptyAction<>();
        Action<Void> actionB = new EmptyAction<>();

        actions.add(actionA).add(actionB);
        assertThat(actions.has(actionA), is(true));
        assertThat(actions.has(actionB), is(true));
        actions.remove(actionA);
        assertThat(actions.has(actionA), is(false));
        assertThat(actions.has(actionB), is(true));
    }

    @Test
    public void has() {
        Actions<Void> actions = createActions();
        Action<Void> actionA = new EmptyAction<>();
        Action<Void> actionB = new EmptyAction<>();

        assertThat(actions.has(), is(false));
        assertThat(actions.has(actionA), is(false));
        assertThat(actions.has(actionB), is(false));
        actions.add(actionA);
        assertThat(actions.has(), is(true));
        assertThat(actions.has(actionA), is(true));
        assertThat(actions.has(actionB), is(false));
    }

    protected abstract <T> Actions<T> createActions();

    protected abstract <T> Actions<T> createActions(Options options);

    static class EmptyAction<A> implements Action<A> {
        @Override
        public void on(A object) {
        }
    }

    static class MemoryAction<T> implements Action<T> {
        private List<T> list = new ArrayList<>();

        @Override
        public void on(T object) {
            list.add(object);
        }

        public List<T> memory() {
            return Collections.unmodifiableList(new ArrayList<T>(list));
        }
    }

}
