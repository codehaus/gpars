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

import groovyx.gpars.actor.AbstractPooledActor
import groovyx.gpars.actor.PooledActorGroup
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

public class LifecycleErrorTest extends GroovyTestCase {

    def group = new PooledActorGroup(3)

    protected void setUp() {
        super.setUp();
    }

    protected void tearDown() {
        super.tearDown();
    }


    public void testOnException() {
        final LFExceptionTestActor actor = new LFExceptionTestActor({->react { throw new LFErrorTestException('Firing off a lifecycle method exception test')}})
        actor.start()
        actor << 1

        actor.latch.await()
        assert actor.flag1
        assert actor.flag2
        assert actor.flag3
    }

    public void testOnInterrupt() {
        final LFExceptionTestActor actor = new LFExceptionTestActor({->react { Thread.currentThread().interrupt();Thread.sleep(10)}})
        actor.start()
        actor << 1

        actor.latch.await()
        assert actor.flag1
        assert actor.flag2
        assert actor.flag3
    }

    public void testOnInterruptWithReact() {
        final LFExceptionTestActor actor = new LFExceptionTestActor({->
            react {
                Thread.currentThread().interrupt();
                react {}
            }
        })
        actor.start()
        actor << 1

        actor.latch.await()
        assert actor.flag1
        assert actor.flag2
        assert actor.flag3
    }

    public void testOnInterruptWithLoop() {
        final LFExceptionTestActor actor = new LFExceptionTestActor({->
            loop {
                react {
                    Thread.currentThread().interrupt();
                }
            }
        })
        actor.start()
        actor << 1

        actor.latch.await()
        assert actor.flag1
        assert actor.flag2
        assert actor.flag3
    }

    public void testOnSilentInterrupt() {
        final LFExceptionTestActor actor = new LFExceptionTestActor({->
            react {
                Thread.currentThread().interrupt();
            }
        })
        actor.start()
        actor << 1

        actor.silentLatch.await()
        assertFalse actor.flag1
        assert actor.flag2
        assertFalse actor.flag3
    }

    public void testOnTimeout() {
        final LFExceptionTestActor actor = new LFExceptionTestActor({->
            react {
                    react(1, TimeUnit.MILLISECONDS){}
            }
        })
        actor.start()
        actor << 1

        actor.latch.await()
        assert actor.flag1
        assert actor.flag2
        assert actor.flag3
    }
}

private final class LFExceptionTestActor extends AbstractPooledActor {
    volatile boolean flag1 = false
    volatile boolean flag2 = false
    volatile boolean flag3 = false
    CountDownLatch latch = new CountDownLatch(1)
    CountDownLatch silentLatch = new CountDownLatch(1)
    private volatile Closure code

    def LFExceptionTestActor(Closure code) {
        this.code = code
        this.code.delegate = this
    }

    def void run() {
        try {
            super.run()
        } catch (LFErrorTestException e) {
            flag3 = true
            latch.countDown()
        }
    }

    protected void act() {
        code.call()
    }

    public def onException(e) {
        if (e instanceof LFErrorTestException) flag1 = true
        throw new LFErrorTestException('Testing exceptions in lifecycle methods - onException')
    }

    public def onInterrupt(e) {
        if (e instanceof InterruptedException) flag1 = true
        throw new LFErrorTestException('Testing exceptions in lifecycle methods - onInterrupt')
    }

    public def onTimeout() {
        flag1 = true
        throw new LFErrorTestException('Testing exceptions in lifecycle methods - onTimeout')
    }

    public def afterStop(messages) {
        flag2 = true
        silentLatch.countDown()
    }
}

private final class LFErrorTestException extends RuntimeException {

    def LFErrorTestException(message) {
        super(message);
    }
}
