package Utils;


import java.util.Map;

/**
 * Created by maxim on 05.11.18.
 */
public interface ScheduledRemovingObservable<K, V> {
    void addListener(ScheduledRemovingObserver listener);
    void noticeListeners(Map.Entry<K,V> removed);
}
