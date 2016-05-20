import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * User: Poyan Gerami
 * Email: poyan.gerami@eniro.com
 * Date: 20/05/16
 */
public class SharedResource {
    public List<String> eventLog = new CopyOnWriteArrayList<String>();

    /**
     * sensitive resource, can't access this method simultaneous
     * from multiple or single JVM
     */
    public void access() {
        eventLog.add(ID.get());
        doStuff();
    }

    private void doStuff() {
        eventLog.add(ID.get());
    }

    ThreadLocal<String> ID = new ThreadLocal<String>(){
        protected String initialValue()  {
            return UUID.randomUUID().toString();
        }
    };


}
