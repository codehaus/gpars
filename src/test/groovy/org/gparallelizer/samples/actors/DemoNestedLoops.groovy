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

package org.gparallelizer.samples.actors

import org.gparallelizer.actors.pooledActors.AbstractPooledActor
import org.gparallelizer.actors.pooledActors.PooledActors

/**
 * Demonstrates a way to do continuation-style loops with PooledActors.
 * @author Vaclav Pech
 */
class MyLoopActor extends AbstractPooledActor {

    protected void act() {
        loop {
            outerLoop()
        }
    }

    private void outerLoop() {
        react {a ->
            println 'Outer: ' + a
            if (a!=0) innerLoop()
            else println 'Done'
        }
    }

    private void innerLoop() {
        react {b ->
            println 'Inner ' + b
            if (b == 0) outerLoop()
            else innerLoop()
        }
    }
}

MyLoopActor actor = new MyLoopActor()

actor.start()

actor.send 1
actor.send 1
actor.send 1
actor.send 1
actor.send 1
actor.send 0
actor.send 2
actor.send 2
actor.send 2
actor.send 2
actor.send 2
actor.send 0
actor.send 3
actor.send 3
actor.send 3
actor.send 3
actor.send 0
actor.send 0



Thread.sleep 3000
actor.send 4
Thread.sleep 3000

PooledActors.defaultPooledActorGroup.shutdown()

