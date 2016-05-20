import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.curator.retry.RetryForever;
import org.apache.curator.test.TestingServer;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * User: Poyan Gerami
 * Email: poyan.gerami@eniro.com
 * Date: 20/05/16
 */
public class CuratorTest {

    // you can also think of number of threads as number om jvm:s
    // TestingServer does not like to have more than 60 concurrent clients
    private static final int NUMBER_THREADS = 50;

    @Test
    public void testSharedLock() throws Exception {
        SharedResource sharedResource = new SharedResource();
        ExecutorService executorService = Executors.newWorkStealingPool();
        TestingServer testingServer = new TestingServer();
        for (int i = 0; i < NUMBER_THREADS; i++) {
            executorService.execute(new Runnable() {
                public void run() {
                    try {
                        // you could create the curator framework outside, but I have it hear for simulating multiple jvm
                        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(testingServer.getConnectString(), new RetryForever(1000));
                        curatorFramework.start();
                        InterProcessSemaphoreMutex interProcessSemaphoreMutex = new InterProcessSemaphoreMutex(curatorFramework, "/demo-shared-lock");
                        SharedLock sharedLock = new SharedLock(interProcessSemaphoreMutex, sharedResource);
                        // you can send in false if you whant to test without locking and see that the test fails
                        sharedLock.process(true);
                    } catch (Exception e ) {}
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
        List<String> eventLog = sharedResource.eventLog;

        // couples shouled have same value if locking have worked
        List<List<String>> partition = Lists.partition(eventLog, 2);
        for (List<String> couple : partition) {
            assertTrue(couple.size() == 2);
            assertEquals(couple.get(0), couple.get(1));
        }
    }

    @Test
    public void testReentrantSharedLock() throws Exception {
        SharedResource sharedResource = new SharedResource();
        ExecutorService executorService = Executors.newWorkStealingPool();
        TestingServer testingServer = new TestingServer();
        for (int i = 0; i < NUMBER_THREADS; i++) {
            executorService.execute(new Runnable() {
                public void run() {
                    try {
                        // you could create the curator framework outside, but I have it hear for simulating multiple jvm
                        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(testingServer.getConnectString(), new RetryForever(1000));
                        curatorFramework.start();
                        InterProcessMutex interProcessSemaphoreMutex = new InterProcessMutex(curatorFramework, "/demo-shared-lock");
                        SharedLock sharedLock = new SharedLock(interProcessSemaphoreMutex, sharedResource);
                        // you can send in false if you want to test without locking and see that the test fails
                        sharedLock.process(true);
                    } catch (Exception e ) {}
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
        List<String> eventLog = sharedResource.eventLog;

        // couples shouled have same value if locking have worked
        List<List<String>> partition = Lists.partition(eventLog, 2);
        for (List<String> couple : partition) {
            assertTrue(couple.size() == 2);
            assertEquals(couple.get(0), couple.get(1));
        }
    }

}