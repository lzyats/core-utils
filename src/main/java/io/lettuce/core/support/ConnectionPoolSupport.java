//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.lettuce.core.support;

import io.lettuce.core.RedisConnectionException;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.internal.LettuceAssert;
import io.lettuce.core.support.ConnectionWrapping.HasTargetConnection;
import io.lettuce.core.support.ConnectionWrapping.Origin;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public abstract class ConnectionPoolSupport {
    private ConnectionPoolSupport() {
    }

    public static <T extends StatefulConnection<?, ?>> GenericObjectPool<T> createGenericObjectPool(Supplier<T> connectionSupplier, GenericObjectPoolConfig config) {
        return createGenericObjectPool(connectionSupplier, config, true);
    }

    public static <T extends StatefulConnection<?, ?>> GenericObjectPool<T> createGenericObjectPool(Supplier<T> connectionSupplier, GenericObjectPoolConfig config, final boolean wrapConnections) {
        LettuceAssert.notNull(connectionSupplier, "Connection supplier must not be null");
        LettuceAssert.notNull(config, "GenericObjectPoolConfig must not be null");
        final AtomicReference<Origin<T>> poolRef = new AtomicReference();
        GenericObjectPool<T> pool = new GenericObjectPool<T>(new ConnectionPoolSupport.RedisPooledObjectFactory(connectionSupplier), config) {
            public T borrowObject() throws Exception {
                return (T) (wrapConnections ? (StatefulConnection) ConnectionWrapping.wrapConnection(super.borrowObject(), (Origin) poolRef.get()) : (StatefulConnection) super.borrowObject());
            }

            public void returnObject(T obj) {
                if (wrapConnections && obj instanceof HasTargetConnection) {
                    super.returnObject((T) ((HasTargetConnection) obj).getTargetConnection());
                } else {
                    super.returnObject(obj);
                }
            }
        };
        poolRef.set(new ConnectionPoolSupport.ObjectPoolWrapper(pool));
        return pool;
    }

    public static <T extends StatefulConnection<?, ?>> GenericObjectPool<T> createGenericObjectPool(Supplier<T> connectionSupplier) {
        return createGenericObjectPool(connectionSupplier, true);
    }

    public static <T extends StatefulConnection<?, ?>> GenericObjectPool<T> createGenericObjectPool(Supplier<T> connectionSupplier, final boolean wrapConnections) {
        LettuceAssert.notNull(connectionSupplier, "Connection supplier must not be null");
        final AtomicReference<Origin<T>> poolRef = new AtomicReference();
        GenericObjectPool<T> pool = new GenericObjectPool<T>(new ConnectionPoolSupport.RedisPooledObjectFactory(connectionSupplier)) {
            public T borrowObject() throws Exception {
                return (T) (wrapConnections ? (StatefulConnection) ConnectionWrapping.wrapConnection(super.borrowObject(), (Origin) poolRef.get()) : (StatefulConnection) super.borrowObject());
            }

            public void returnObject(T obj) {
                if (wrapConnections && obj instanceof HasTargetConnection) {
                    super.returnObject((T) ((HasTargetConnection) obj).getTargetConnection());
                } else {
                    super.returnObject(obj);
                }
            }
        };
        //创建好后，调用 preparePool
        try {
            pool.preparePool();
        } catch (Exception e) {
            throw new RedisConnectionException("prepare connection pool failed", e);
        }
        poolRef.set(new ConnectionPoolSupport.ObjectPoolWrapper(pool));
        return pool;
    }

    private static class ObjectPoolWrapper<T> implements Origin<T> {
        private static final CompletableFuture<Void> COMPLETED = CompletableFuture.completedFuture(null);
        private final ObjectPool<T> pool;

        ObjectPoolWrapper(ObjectPool<T> pool) {
            this.pool = pool;
        }

        public void returnObject(T o) throws Exception {
            this.pool.returnObject(o);
        }

        public CompletableFuture<Void> returnObjectAsync(T o) throws Exception {
            this.pool.returnObject(o);
            return COMPLETED;
        }
    }

    private static class RedisPooledObjectFactory<T extends StatefulConnection<?, ?>> extends BasePooledObjectFactory<T> {
        private final Supplier<T> connectionSupplier;

        RedisPooledObjectFactory(Supplier<T> connectionSupplier) {
            this.connectionSupplier = connectionSupplier;
        }

        public T create() {
            return this.connectionSupplier.get();
        }

        public void destroyObject(PooledObject<T> p) {
            (p.getObject()).close();
        }

        public PooledObject<T> wrap(T obj) {
            return new DefaultPooledObject(obj);
        }

        public boolean validateObject(PooledObject<T> p) {
            return (p.getObject()).isOpen();
        }
    }
}
