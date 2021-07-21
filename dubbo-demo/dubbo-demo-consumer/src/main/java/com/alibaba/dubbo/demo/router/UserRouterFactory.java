package com.alibaba.dubbo.demo.router;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.cluster.Router;
import com.alibaba.dubbo.rpc.cluster.RouterFactory;

import java.lang.reflect.Method;
import java.util.List;

public class UserRouterFactory implements RouterFactory {

    private final static Logger logger = LoggerFactory.getLogger(UserRouterFactory.class);

    public static final String ip = "_ip";

    public static final String target = "_target";

    private RouterFactory routerFactory;

    /**
     * 注入RouterFactory扩展点
     */
    public UserRouterFactory(RouterFactory routerFactory) {
        this.routerFactory = routerFactory;
        logger.info("inject router factory '" + routerFactory.getClass().getName()
                + "' success, wrapper factory: '" + UserRouterFactory.class.getName());
    }


    @Override
    public Router getRouter(URL url) {
        RouterDelegate router = new RouterDelegate(routerFactory.getRouter(url), url);
        return router;
    }

    public class RouterDelegate implements Router {

        private Router router;

        private boolean injectHosts;

        private URL url;

        public RouterDelegate(Router router, URL url) {
            this.url = url;
            this.router = router;

            // inject runtime
            if (url.getParameter("runtime") == null) {
                this.url = url.addParameter("runtime", true);
            }
        }

        @Override
        public <T> List<Invoker<T>> route(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
            List<Invoker<T>> filtered = this.router.route(invokers, url, invocation);
            injectHostsIfRequired(filtered, invocation);
            return filtered;
        }

        <T> void injectHostsIfRequired(List<Invoker<T>> filtered, Invocation invocation) {
            String host;
            StringBuffer buf = new StringBuffer();
            for (Invoker<T> invoker : filtered) {
                host = invoker.getUrl().getParameter(ip, (String) null);
                if (host == null) {
                    // provider host ip
                    host = invoker.getUrl().getHost();
                }
                // Get the actual target IP and pass it to the mesh
                if (host != null) {
                    if (buf.length() > 0) {
                        buf.append(",");
                    }
                    buf.append(host);
                }
            }
            if (invocation instanceof RpcInvocation) {
                ((RpcInvocation) invocation).setAttachment(target, buf.toString());
            }
        }

        @Override
        public URL getUrl() {
            return this.url;
        }

        @Override
        public int compareTo(Router o) {
            return this.router.compareTo(o);
        }

        @Override
        public int getPriority() {
            try {
                Method priority = this.router.getClass().getMethod("getPriority");
                if (priority != null) {
                    return (Integer) priority.invoke(this.router);
                }
            } catch (Throwable ignored) {
            }
            return 0;
        }
    }
}
