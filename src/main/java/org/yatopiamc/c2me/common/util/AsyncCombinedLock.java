package org.yatopiamc.c2me.common.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ibm.asyncutil.locks.AsyncLock;
import com.ibm.asyncutil.locks.AsyncNamedLock;
import net.minecraft.util.math.ChunkPos;
import org.threadly.concurrent.UnfairExecutor;
import org.yatopiamc.c2me.common.config.C2MEConfig;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class AsyncCombinedLock {

    public static final UnfairExecutor lockWorker = new UnfairExecutor(
            C2MEConfig.asyncIoConfig.serializerParallelism,
            new ThreadFactoryBuilder().setDaemon(true).setPriority(Thread.NORM_PRIORITY - 1).setNameFormat("C2ME lock worker #%d").build()
    );

    private final AsyncNamedLock<ChunkPos> lock;
    private final ChunkPos[] names;
    private final CompletableFuture<AsyncLock.LockToken> future = new CompletableFuture<>();

    public AsyncCombinedLock(AsyncNamedLock<ChunkPos> lock, Set<ChunkPos> names) {
        this.lock = lock;
        this.names = names.toArray(new ChunkPos[] {});
        lockWorker.execute(this::tryAcquire);
    }

    private synchronized void tryAcquire() { // TODO optimize logic further
        final LockEntry[] tryLocks = new LockEntry[names.length];
        boolean allAcquired = true;
        for (int i = 0, namesLength = names.length; i < namesLength; i++) {
            ChunkPos name = names[i];
            final LockEntry entry = new LockEntry(name, this.lock.tryLock(name));
            tryLocks[i] = entry;
            if (!entry.lockToken.isPresent()) {
                allAcquired = false;
                break;
            }
        }
        if (allAcquired) {
            future.complete(() -> {
                for (LockEntry entry : tryLocks) {
                    //noinspection OptionalGetWithoutIsPresent
                    entry.lockToken.get().releaseLock(); // if it isn't present then something is really wrong
                }
            });
        } else {
            boolean triedRelock = false;
            for (LockEntry entry : tryLocks) {
                if (entry == null) continue;
                entry.lockToken.ifPresent(AsyncLock.LockToken::releaseLock);
                if (!triedRelock && !entry.lockToken.isPresent()) {
                    this.lock.acquireLock(entry.name).thenCompose(lockToken -> {
                        lockToken.releaseLock();
                        return CompletableFuture.runAsync(this::tryAcquire, lockWorker);
                    });
                    triedRelock = true;
                }
            }
            if (!triedRelock) {
                // shouldn't happen at all...
                lockWorker.execute(this::tryAcquire);
            }
        }
    }

    public CompletableFuture<AsyncLock.LockToken> getFuture() {
        return future.thenApply(Function.identity());
    }

    private static class LockEntry {
        public final ChunkPos name;
        public final Optional<AsyncLock.LockToken> lockToken;

        private LockEntry(ChunkPos name, Optional<AsyncLock.LockToken> lockToken) {
            this.name = name;
            this.lockToken = lockToken;
        }
    }
}
