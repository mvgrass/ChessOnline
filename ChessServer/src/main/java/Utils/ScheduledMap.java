package Utils;

import java.util.*;

/**
 * Created by maxim on 05.11.18.
 */
public class ScheduledMap<K, V> implements Map<K,V>, ScheduledRemovingObservable<K, V> {
    private Map<K, V> map;

    private Map<K, Long> timeMap = new HashMap<>();

    private Long timeout;

    private List<ScheduledRemovingObserver<K, V>> listeners = new LinkedList<>();

    public ScheduledMap(HashMap<K, V> map, Long timeout) throws RuntimeException{
        if(map == null)
            throw new RuntimeException("Wrong parameter, map shouldn't be null");

        this.map = map;

        this.timeout = timeout;

        Worker worker = new Worker(this, timeout);
        worker.setDaemon(true);
        worker.start();
    }

    @Override
    public void addListener(ScheduledRemovingObserver listener) {
        listeners.add(listener);
    }

    @Override
    public void noticeListeners(Map.Entry<K,V> removed) {
        for(ScheduledRemovingObserver listener : listeners)
            listener.update(removed);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return map.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return map.containsValue(o);
    }

    @Override
    public V get(Object o) {
        return map.get(o);
    }

    @Override
    public V put(K k, V v) {
        timeMap.put(k, System.currentTimeMillis());
        return map.put(k, v);
    }

    @Override
    public V remove(Object o) {
        timeMap.remove(o);
        return map.remove(o);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        Long time = System.currentTimeMillis();
        map.forEach((K key, V value)->{timeMap.put(key, time);});
        this.map.putAll(map);
    }

    @Override
    public void clear() {
        timeMap.clear();
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    class Worker extends Thread{
        Long timeout;

        ScheduledMap<K, V> scheduledMap;

        Worker(ScheduledMap<K, V> map, Long timeout){
            this.timeout = timeout;
            this.scheduledMap = map;
        }

        public void run(){
            try {
                while (true) {
                    this.sleep(timeout);
                    synchronized (scheduledMap){
                        Long time = System.currentTimeMillis();
                        Iterator<Map.Entry<K,Long>> iterator = scheduledMap.timeMap.entrySet().iterator();
                        while (iterator.hasNext()){
                            Entry<K,Long> pair = iterator.next();;
                            if(time - pair.getValue() >= timeout){
                                V removed = scheduledMap.remove(pair.getKey());
                                scheduledMap.noticeListeners(new AbstractMap.SimpleEntry<K, V>(pair.getKey(), removed));
                            }
                        }
                    }
                }
            }catch (InterruptedException exc){
                exc.printStackTrace();
            }
        }
    }
}
