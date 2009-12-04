//  GPars (formerly GParallelizer)
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

package groovyx.gpars.actor.nonBlocking

import groovyx.gpars.actor.ActorGroup
import groovyx.gpars.actor.PooledActorGroup
import groovyx.gpars.actor.impl.AbstractPooledActor
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.atomic.AtomicInteger

/**
 *
 * @author Vaclav Pech
 * Date: Feb 20, 2009
 */
public class MessagingTest extends GroovyTestCase {
    ActorGroup group

    protected void setUp() {
        group = new PooledActorGroup(5)
    }

    protected void tearDown() {
        group.shutdown()
    }

    public void testReact() {
        final def barrier = new CyclicBarrier(2)
        final AtomicInteger counter = new AtomicInteger(0)

        final AbstractPooledActor actor = group.actor {
            counter.incrementAndGet()
            barrier.await()
            react {
                counter.incrementAndGet()
                barrier.await()
                react {
                    counter.incrementAndGet()
                    barrier.await()
                }
            }
        }

        barrier.await()
        assertEquals 1, counter.intValue()

        actor.send('message')
        barrier.await()
        assertEquals 2, counter.intValue()

        actor.send('message')
        barrier.await()
        assertEquals 3, counter.intValue()
    }

    public void testLeftShift() {
        final def barrier = new CyclicBarrier(2)
        final AtomicInteger counter = new AtomicInteger(0)

        final AbstractPooledActor actor = group.actor {
            counter.incrementAndGet()
            barrier.await()
            react {
                counter.incrementAndGet()
                barrier.await()
                react {
                    counter.incrementAndGet()
                    barrier.await()
                }
            }
        }

        barrier.await()
        assertEquals 1, counter.intValue()

        actor << 'message'
        barrier.await()
        assertEquals 2, counter.intValue()

        actor << 'message'
        barrier.await()
        assertEquals 3, counter.intValue()
    }

    public void testReactWithBufferedMessages() {
        final def barrier = new CyclicBarrier(2)
        final AtomicInteger counter = new AtomicInteger(0)

        final AbstractPooledActor actor = group.actor {
            barrier.await()
            react {
                counter.incrementAndGet()
                barrier.await()
                barrier.await()
                react {
                    counter.incrementAndGet()
                    barrier.await()
                    barrier.await()
                    react {
                        counter.incrementAndGet()
                        barrier.await()
                    }
                }
            }
        }

        actor.send('message')
        actor.send('message')
        actor.send('message')
        barrier.await()
        barrier.await()
        assertEquals 1, counter.intValue()
        barrier.await()

        barrier.await()
        assertEquals 2, counter.intValue()
        barrier.await()

        barrier.await()
        assertEquals 3, counter.intValue()
    }

    public void testReactWithDelayedMessages() {
        final def barrier = new CyclicBarrier(2)
        final AtomicInteger counter = new AtomicInteger(0)

        final AbstractPooledActor actor = group.actor {
            react {
                counter.incrementAndGet()
                barrier.await()
            }
        }

        Thread.sleep(1000)
        actor.send('message')
        barrier.await()
        assertEquals 1, counter.intValue()
    }
}
