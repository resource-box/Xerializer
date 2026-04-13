package com.hooniegit.Xerializer.Kryo;

import com.esotericsoftware.kryo.Kryo;
import com.hooniegit.Xerializer.Kryo.Common.KryoHolder;

/**
 * ThreadLocal 기반 Kryo Serializer 클래스입니다.
 * Kryo 객체는 Thread-Safe 하지 않으므로, ThreadLocal을 사용하여 각 스레드마다 독립적인 KryoHolder를 유지합니다.
 * Lock 경합 없이 스레드별로 독립적인 KryoHolder를 유지하여 최고의 성능을 제공합니다.
 *
 * @Warning Thread 수가 고정된 환경에 적합합니다.
 * @Warning Thread Pool 환경에서는 Pool 반납 전 cleanUp() 메서드를 반드시 호출하여 ThreadLocal로 인한 메모리 누수를 방지해야 합니다.
 */
public class ThreadLocalSerializer {

    private static final int INIT_BUF = 1024; // KryoHolder의 Output 객체가 처음에 할당하는 버퍼 크기
    private static final int MAX_BUF = 192000000; // KryoHolder의 Output 객체가 최대 할당할 버퍼 크기 - 필요에 따라 조정 가능

    // ThreadLocal을 사용하여 각 스레드마다 독립적인 KryoHolder를 지연 생성(Lazy Initialization)합니다.
    private static final ThreadLocal<KryoHolder> KRYO_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();

        return new KryoHolder(kryo, INIT_BUF, MAX_BUF);
    });

    /**
     * 객체를 바이트 배열로 직렬화합니다.
     * @param object 직렬화할 객체
     * @return 직렬화된 바이트 배열
     */
    public static <T> byte[] serialize(T object) {
        KryoHolder holder = KRYO_THREAD_LOCAL.get();

        try {
            holder.resetForBorrow(); // 안전을 위한 권장 사항
            holder.kryo.setClassLoader(Thread.currentThread().getContextClassLoader());
            holder.kryo.writeClassAndObject(holder.output, object);

            return holder.output.toBytes();
        } catch (Exception e) {
            throw new RuntimeException("Serialization failed", e);
        } finally {
            holder.resetForReturn();
        }
    }

    /**
     * 바이트 배열을 원본 데이터 타입으로 역직렬화합니다.
     * @param bytes 직렬화된 바이트 배열
     * @return 역직렬화된 원본 객체
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(byte[] bytes) {
        KryoHolder holder = KRYO_THREAD_LOCAL.get();

        try {
            holder.resetForBorrow(); // 안전을 위한 권장 사항

            // 현재 스레드의 TCCL로 보정
            holder.kryo.setClassLoader(Thread.currentThread().getContextClassLoader());

            return (T) holder.kryo.readClassAndObject(holder.input);
        } catch (Exception e) {
            throw new RuntimeException("Deserialization failed", e);
        } finally {
            holder.resetForReturn();
        }
    }

    /**
     * [매우 중요] 스레드 풀 환경에서 스레드가 작업을 마치고 풀로 돌아가기 전에 호출해야 합니다.
     * Tomcat 등 WAS 환경에서 ThreadLocal로 인한 메모리 누수를 방지합니다.
     */
    public static void cleanUp() {
        KRYO_THREAD_LOCAL.remove();
    }

}