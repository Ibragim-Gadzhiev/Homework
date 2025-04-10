package Module_1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MyHashMapTest {

    private MyHashMap<String, String> stringMap;
    private MyHashMap<Integer, String> intKeyMap;
    private MyHashMap<String, Double> doubleValueMap;

    @BeforeEach
    void setUp() {
        stringMap = new MyHashMap<>();
        intKeyMap = new MyHashMap<>();
        doubleValueMap = new MyHashMap<>();
    }

    @Test
    void testPutAndGetWithNullKey() {
        assertNull(stringMap.put(null, "🍕"));
        assertEquals("🍕", stringMap.get(null));

        assertEquals("🍕", stringMap.put(null, "🍔"));
        assertEquals("🍔", stringMap.get(null));
    }

    @Test
    void testRemoveNullKey() {
        stringMap.put(null, "🐟");
        assertEquals("🐟", stringMap.remove(null));

        assertNull(stringMap.get(null));
        assertEquals(0, stringMap.size());
    }

    @Test
    void testStringToStringMapping() {
        assertNull(stringMap.put("🐱", "🐶"));
        assertEquals("🐶", stringMap.get("🐱"));

        assertEquals("🐶", stringMap.put("🐱", "🐱🐶"));
        assertEquals("🐱🐶", stringMap.get("🐱"));

        assertEquals("🐱🐶", stringMap.remove("🐱"));
        assertNull(stringMap.get("🐱"));
        assertEquals(0, stringMap.size());
    }

    @Test
    void testStringToDoubleMapping() {
        assertNull(doubleValueMap.put("π", 3.14));
        assertEquals(3.14, doubleValueMap.get("π"));

        assertEquals(3.14, doubleValueMap.put("π", 3.1415));
        assertEquals(3.1415, doubleValueMap.get("π"));

        assertEquals(3.1415, doubleValueMap.remove("π"));
        assertNull(doubleValueMap.get("π"));
        assertEquals(0, doubleValueMap.size());
    }

    @Test
    void testIntegerToStringMapping() {
        assertNull(intKeyMap.put(42, "🐶"));
        assertEquals("🐶", intKeyMap.get(42));

        assertEquals("🐶", intKeyMap.put(42, "🍑"));
        assertEquals("🍑", intKeyMap.get(42));

        assertEquals("🍑", intKeyMap.remove(42));
        assertNull(intKeyMap.get(42));
        assertEquals(0, intKeyMap.size());
    }

    @Test
    void testMassiveInsertionAndRemoval() {
        MyHashMap<Integer, String> map = new MyHashMap<>();
        int count = 1_000_000;

        for (int i = 0; i < count; i++) {
            assertNull(map.put(i, "Персик" + i));
        }

        for (int i = 0; i < count; i++) {
            assertEquals("Персик" + i, map.get(i));
        }

        assertEquals(count, map.size());

        for (int i = 0; i < count; i++) {
            assertEquals("Персик" + i, map.remove(i));
        }

        for (int i = 0; i < count; i++) {
            assertNull(map.get(i));
        }

        assertEquals(0, map.size());
    }

    @Test
    void testHashCollisions() {
        class CollisionKey {
            final int id;
            CollisionKey(int id) { this.id = id; }
            @Override public int hashCode() { return 42; }
            @Override public boolean equals(Object o) {
                return o instanceof CollisionKey && ((CollisionKey)o).id == this.id;
            }
        }

        MyHashMap<CollisionKey, String> map = new MyHashMap<>();
        CollisionKey k1 = new CollisionKey(1);
        CollisionKey k2 = new CollisionKey(2);

        assertNull(map.put(k1, "🍎"));
        assertNull(map.put(k2, "🍌"));

        assertEquals("🍎", map.get(k1));
        assertEquals("🍌", map.get(k2));
        assertEquals(2, map.size());

        assertEquals("🍎", map.remove(k1));
        assertNull(map.get(k1));
        assertEquals("🍌", map.get(k2));
    }

    @Test
    void testReinsertAfterRemoval() {
        stringMap.put("🌧", "☔️");
        assertEquals("☔️", stringMap.remove("🌧"));

        assertNull(stringMap.put("🌧", "🌂"));
        assertEquals("🌂", stringMap.get("🌧"));
        assertEquals(1, stringMap.size());
    }

    @Test
    void testNonExistentKeys() {
        assertNull(stringMap.get("🚀"));
        assertNull(stringMap.remove("🚀"));
        assertEquals(0, stringMap.size());

        stringMap.put("🌟", "✨");
        assertNull(stringMap.get("💫"));
    }

    @Test
    void testValueUpdates() {
        assertNull(intKeyMap.put(7, "🧀"));
        assertEquals("🧀", intKeyMap.put(7, "🥨"));
        assertEquals("🥨", intKeyMap.get(7));
        assertEquals(1, intKeyMap.size());
    }

    @Test
    void testSizeManagement() {
        assertEquals(0, stringMap.size());

        stringMap.put("🍏", "1");
        stringMap.put("🍎", "2");
        assertEquals(2, stringMap.size());

        stringMap.remove("🍏");
        assertEquals(1, stringMap.size());

        stringMap.remove("🚫");
        assertEquals(1, stringMap.size());
    }

    @Test
    void testObjectKeys() {
        class ComplexKey {
            final String part1;
            final int part2;

            ComplexKey(String p1, int p2) {
                this.part1 = p1;
                this.part2 = p2;
            }

            @Override
            public int hashCode() {
                return part1.hashCode() ^ part2;
            }

            @Override
            public boolean equals(Object o) {
                if (!(o instanceof ComplexKey other)) return false;
                return this.part1.equals(other.part1) && this.part2 == other.part2;
            }
        }

        MyHashMap<ComplexKey, String> map = new MyHashMap<>();
        ComplexKey key1 = new ComplexKey("🧩", 42);
        ComplexKey key2 = new ComplexKey("🧩", 42);

        assertNull(map.put(key1, "🥒"));
        assertEquals("🥒", map.get(key2));
        assertEquals("🥒", map.remove(key2));
        assertEquals(0, map.size());
    }

    @Test
    void testNullValues() {
        assertNull(stringMap.put("📦", null));
        assertNull(stringMap.get("📦"));

        assertNull(stringMap.put("📦", "🔓"));
        assertEquals("🔓", stringMap.get("📦"));
    }

}
