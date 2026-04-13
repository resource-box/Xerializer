# Xerializer
Kryo 패키지 기반의 단순한 데이터 직렬화 라이브러리입니다.

### Target
- 객체 생성 및 공유 없이 Static 생성자로 정의하여 간편한 사용성을 제공합니다.
- Kryo 객체 자체는 Thread-Safe 하지 않으므로, Wrapper 클래스로 감싸고 이를 Thread Local 또는 Pool 방식으로 관리하여 성능을 향상시킵니다.
- 객체 재사용(Output, Input) 및 버퍼 할당을 통해 GC 부담을 줄이고, 빠른 직렬화/역직렬화를 제공합니다.
- 보수적으로 포인터 및 버퍼를 초기화하여 안정성을 높입니다.

### 라이브러리 사용법
1. Pool Serializer
```java
import com.hooniegit.Xerializer.Kryo.PoolSerializer;

SampleDataClass data = new SampleDataClass();

// Serialize
byte[] b = PoolSerializer.serialize(data);

// Deserialize
SampleDataClass origin = PoolSerializer.deserialize(b);
```
2. Thread Local Serializer
```java
import com.hooniegit.Xerializer.Kryo.ThreadLocalSerializer;

SampleDataClass data = new SampleDataClass();

// Serialize
byte[] b = ThreadLocalSerializer.serialize(data);

// Deserialize
SampleDataClass origin = ThreadLocalSerializer.deserialize(b);

// Thread Pool 환경의 경우, Pool 반납 전에 호출
ThreadLocalSerializer.cleanUp();
```

### Version
- v2.0.0: Wrapper 클래스 추가 / Pool Serializer 및 Thread Local Serializer 구현