package Utils;

import java.util.Map;

/**
 * Created by maxim on 05.11.18.
 */
public interface ScheduledRemovingObserver<K, V> {
    void update(Map.Entry<K, V> removed);
}
