import org.apache.curator.framework.recipes.locks.InterProcessLock;

import java.util.concurrent.TimeUnit;

/**
 * User: Poyan Gerami
 * Email: poyan.gerami@eniro.com
 * Date: 20/05/16
 */
public class SharedLock {
    private final SharedResource sharedResource;
    private final InterProcessLock lock;

    public SharedLock(InterProcessLock lock, SharedResource sharedResource) {
        this.lock = lock;
        this.sharedResource = sharedResource;
    }

    public void process(boolean withLocking) throws Exception {
        if (withLocking) {
            processWithLocking();
        } else {
            processWithoutLocking();
        }
    }

    private void processWithoutLocking() {
        sharedResource.access();
    }

    private void processWithLocking() throws Exception {
        try {
            if (lock.acquire(1000, TimeUnit.MILLISECONDS)) {
                sharedResource.access();
                reentrant();
                Thread.sleep(1000);
            }
        } finally {
            lock.release();
        }
    }

    private void reentrant() throws Exception {
        try {
            if (lock.acquire(1000, TimeUnit.MILLISECONDS)) {
                sharedResource.access();
            }
        } finally {
            lock.release();
        }
    }
}
