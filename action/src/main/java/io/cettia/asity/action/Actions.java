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

/**
 * A manager for a set of {@link Action}s. Inspired by jQuery's Callbacks.
 * <p/>
 * All operations on an actions are exception-safe.
 *
 * @author Donghwan Kim
 */
public interface Actions<T> {

    /**
     * Adds an action.
     */
    Actions<T> add(Action<T> action);

    /**
     * Disables any operation on the actions. This method is useful when
     * multiple events are mutually exclusive.
     */
    Actions<T> disable();

    /**
     * Determines if the actions has been disabled.
     */
    boolean disabled();

    /**
     * Removes all of the actions.
     */
    Actions<T> empty();

    /**
     * Fire all of the actions.
     */
    Actions<T> fire();

    /**
     * Fire all of the actions with the given value.
     */
    Actions<T> fire(T data);

    /**
     * Determines if the actions have been called at least once.
     */
    boolean fired();

    /**
     * Determines if the actions contains an action.
     */
    boolean has();

    /**
     * Determines whether the actions contains the specified action.
     */
    boolean has(Action<T> action);

    /**
     * Removes an action.
     */
    Actions<T> remove(Action<T> action);

    /**
     * Options to create an Actions. With the default options, an Action will
     * work like a typical event manager.
     *
     * @author Donghwan Kim
     */
    class Options {

        private boolean once;
        private boolean memory;
        private boolean unique;

        public Options() {
        }

        public Options(Options options) {
            once = options.once;
            memory = options.memory;
            unique = options.unique;
        }

        public boolean once() {
            return once;
        }

        /**
         * Ensures the actions can only be fired once. The default value is
         * false.
         */
        public Options once(boolean once) {
            this.once = once;
            return this;
        }

        public boolean memory() {
            return memory;
        }

        /**
         * Keeps track of previous values and will call any action added after
         * the actions has been fired right away with the latest "memorized"
         * values. The default value is false.
         */
        public Options memory(boolean memory) {
            this.memory = memory;
            return this;
        }

        public boolean unique() {
            return unique;
        }

        /**
         * Ensures an action can only be added once. The default value is false.
         */
        public Options unique(boolean unique) {
            this.unique = unique;
            return this;
        }

    }

}