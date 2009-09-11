//  GParallelizer
//
//  Copyright © 2008-9  The original author or authors
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License. 

package org.gparallelizer.actors.pooledActors

import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit

public class MultiMessageTimeoutTest extends GroovyTestCase {
    public void testReact() {
        CountDownLatch latch = new CountDownLatch(1)
        volatile int result = 0

        def actor = PooledActors.actor {
            react(10, TimeUnit.SECONDS) {a, b, c ->
                result = a + b + c
                latch.countDown()
            }
        }.start()

        actor.send 2
        actor.send 3
        actor.send 4

        latch.await(30, TimeUnit.SECONDS)
        assertEquals 9, result
    }

    public void testReactZeroTimeout() {
        CountDownLatch latch = new CountDownLatch(1)
        CyclicBarrier barrier = new CyclicBarrier(2)
        volatile int result = 0

        def actor = PooledActors.actor {
            barrier.await()
            react(0, TimeUnit.SECONDS) {a, b, c ->
                result = a + b + c
                latch.countDown()
            }
        }.start()

        actor.send 2
        actor.send 3
        actor.send 4
        barrier.await()

        latch.await(30, TimeUnit.SECONDS)
        assertEquals 9, result
    }

    public void testReactPassedZeroTimeout() {
        CountDownLatch latch = new CountDownLatch(1)
        CyclicBarrier barrier = new CyclicBarrier(2)
        volatile List result = []
        volatile boolean flag = false

        def actor = PooledActors.actor {
            barrier.await()
            react(0, TimeUnit.SECONDS) {a, b, c ->
                result << a
                result << b
                result << c
            }
        }.start()

        actor.metaClass.onTimeout = { flag = true }
        actor.metaClass.afterStop = {messages ->
            result.addAll messages
            latch.countDown()
        }
        actor.send 2
        barrier.await()
        latch.await(30, TimeUnit.SECONDS)
        assert flag
        assertEquals([2], result)
    }

    public void testReactPassedNonZeroTimeout() {
        CountDownLatch latch = new CountDownLatch(1)
        CyclicBarrier barrier = new CyclicBarrier(2)
        volatile List result = []
        volatile boolean flag = false

        def actor = PooledActors.actor {
            barrier.await()
            react(1, TimeUnit.SECONDS) {a, b, c ->
                result << a
                result << b
                result << c
            }
        }.start()

        actor.metaClass.onTimeout = { flag = true }
        actor.metaClass.afterStop = {messages ->
            result.addAll messages
            latch.countDown()
        }
        actor.send 2
        barrier.await()
        latch.await(30, TimeUnit.SECONDS)
        assertEquals([2], result)
    }

    public void testReactNonZeroTimeout() {
        CountDownLatch latch = new CountDownLatch(1)
        CyclicBarrier barrier = new CyclicBarrier(2)
        volatile List result = []

        def actor = PooledActors.actor {
            barrier.await()
            react(2, TimeUnit.SECONDS) {a, b, c ->
                result << a
                result << b
                result << c
                latch.countDown()
            }
        }.start()

        actor.send 2
        actor.send 3
        actor.send 4
        barrier.await()
        latch.await(30, TimeUnit.SECONDS)
        assertEquals([2, 3, 4], result)
    }

    public void testNoMessageReact() {
        CountDownLatch latch = new CountDownLatch(1)
        volatile int result = 0

        def actor = PooledActors.actor {
            react(10, TimeUnit.SECONDS) {->
                result = 1
                latch.countDown()
            }
        }.start()

        actor.send 2

        latch.await(30, TimeUnit.SECONDS)
        assertEquals 1, result
    }

    public void testNoMessageReactZeroTimeout() {
        CyclicBarrier barrier = new CyclicBarrier(2)
        CountDownLatch latch = new CountDownLatch(1)
        volatile int result = 0

        def actor = PooledActors.actor {
            barrier.await()
            react(0, TimeUnit.SECONDS) {->
                result = 1
                latch.countDown()
            }
        }.start()

        actor.send 2
        barrier.await()

        latch.await(30, TimeUnit.SECONDS)
        assertEquals 1, result
    }

    public void testNoMessageReactPassedZeroTimeout() {
        CountDownLatch latch = new CountDownLatch(1)
        volatile int result = 0
        volatile boolean flag = false

        def actor = PooledActors.actor {
            react(0, TimeUnit.SECONDS) {->
                result = 2
                latch.countDown()
            }
        }

        actor.metaClass.onTimeout = { flag = true }
        actor.metaClass.afterStop = {messages ->
            latch.countDown()
        }
        actor.start()

        latch.await(30, TimeUnit.SECONDS)
        assert flag
        assertEquals 0, result
    }

    public void testDefaultMessageReact() {
        CountDownLatch latch = new CountDownLatch(1)
        volatile int result = 0

        def actor = PooledActors.actor {
            react(10, TimeUnit.SECONDS) {
                result = 1
                latch.countDown()
            }
        }.start()

        actor.send 2

        latch.await(30, TimeUnit.SECONDS)
        assertEquals 1, result
    }

    public void testArrayReact() {
        CountDownLatch latch = new CountDownLatch(1)
        volatile int result = 0

        def actor = PooledActors.actor {
            react(10, TimeUnit.SECONDS) {a, b, c ->
                result = a[2] + b + c
                latch.countDown()
            }
        }.start()

        actor.send([2, 10, 20])
        actor.send 3
        actor.send 4

        latch.await(30, TimeUnit.SECONDS)
        assertEquals 27, result
    }
}
