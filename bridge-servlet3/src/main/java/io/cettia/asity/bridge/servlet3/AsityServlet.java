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
package io.cettia.asity.bridge.servlet3;

import io.cettia.asity.action.Action;
import io.cettia.asity.action.Actions;
import io.cettia.asity.action.ConcurrentActions;
import io.cettia.asity.http.ServerHttpExchange;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to process {@link HttpServletRequest} and {@link HttpServletResponse}
 * into {@link ServerHttpExchange}. When you configure servlet, you must set
 * <strong><code>asyncSupported</code></strong> to <strong><code>true</code>
 * </strong>.
 * <p>
 * 
 * <pre>
 * Servlet servlet = new AsityServlet().onhttp(http -&gt {});
 * ServletRegistration.Dynamic reg = context.addServlet(AsityServlet.class.getName(), servlet);
 * <strong>reg.setAsyncSupported(true);</strong>
 * reg.addMapping("/cettia");
 * </pre>
 *
 * @author Donghwan Kim
 */
@SuppressWarnings("serial")
public class AsityServlet extends HttpServlet {

    private Actions<ServerHttpExchange> httpActions = new ConcurrentActions<>();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        httpActions.fire(new ServletServerHttpExchange(req, resp));
    }

    /**
     * Registers an action to be called when {@link ServerHttpExchange} is
     * available.
     */
    public AsityServlet onhttp(Action<ServerHttpExchange> action) {
        httpActions.add(action);
        return this;
    }
}
